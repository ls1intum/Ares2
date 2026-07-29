package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ArrayType;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.securitytest.ReservedPackageGuard;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/** JavaParser-backed, deterministic Java project scanner. */
public class JavaProjectScanner implements ProjectScanner {
	// The test annotations Ares recognises, each mapped to the fully-qualified
	// type(s) it
	// may denote. A bare simple name is trusted only when the compilation unit
	// imports the
	// matching type (directly or by package wildcard); a fully-qualified use is
	// always
	// trusted. This keeps a locally declared look-alike @interface from marking a
	// class as
	// a test in the no-policy scan.
	private static final Map<String, Set<String>> TEST_ANNOTATIONS = Map.ofEntries(
			Map.entry("Test", Set.of("org.junit.jupiter.api.Test", "org.junit.Test")),
			Map.entry("ParameterizedTest", Set.of("org.junit.jupiter.params.ParameterizedTest")),
			Map.entry("RepeatedTest", Set.of("org.junit.jupiter.api.RepeatedTest")),
			Map.entry("TestFactory", Set.of("org.junit.jupiter.api.TestFactory")),
			Map.entry("TestTemplate", Set.of("org.junit.jupiter.api.TestTemplate")),
			Map.entry("Property", Set.of("net.jqwik.api.Property")),
			Map.entry("Example", Set.of("net.jqwik.api.Example")),
			Map.entry("PublicTest", Set.of("de.tum.cit.ase.ares.api.jupiter.PublicTest")),
			Map.entry("HiddenTest", Set.of("de.tum.cit.ase.ares.api.jupiter.HiddenTest")));
	private final BuildToolConfiguration buildConfiguration;
	private final JavaParser parser = new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));

	public JavaProjectScanner() {
		this.buildConfiguration = null;
	}

	public JavaProjectScanner(BuildToolConfiguration buildConfiguration) {
		this.buildConfiguration = Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
	}

	@Nonnull
	protected String getDefaultPackage() {
		return "";
	}

	@Nonnull
	protected String getDefaultMainClass() {
		return "Main";
	}

	private List<Path> productionRoots() {
		if (buildConfiguration != null) {
			return buildConfiguration.productionSourceRoots();
		}
		return ProjectSourcesFinder.findProjectSourcesPath().map(List::of).orElse(List.of());
	}

	private List<Path> testRoots() {
		if (buildConfiguration != null) {
			return buildConfiguration.testSourceRoots();
		}
		boolean maven = ProjectSourcesFinder.isMavenProject();
		boolean gradle = ProjectSourcesFinder.isGradleProject();
		Path conventional = Path.of("src", "test", "java");
		if ((maven || gradle) && Files.isDirectory(conventional)) {
			return List.of(conventional);
		}
		if (gradle) {
			Path artemis = Path.of("test");
			if (Files.isDirectory(artemis)) {
				return List.of(artemis);
			}
		}
		return List.of();
	}

	private List<Path> javaFiles(List<Path> roots) {
		List<Path> files = new ArrayList<>();
		for (Path root : roots) {
			if (!Files.isDirectory(root) || !Files.isReadable(root)) {
				throw new IllegalStateException("Unreadable Java source root: " + root);
			}
			try (Stream<Path> stream = Files.walk(root)) {
				stream.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).sorted()
						.forEach(files::add);
			} catch (IOException exception) {
				throw new IllegalStateException("Cannot scan Java source root: " + root, exception);
			}
		}
		return List.copyOf(files);
	}

	private CompilationUnit parse(Path file) {
		try {
			var result = parser.parse(file);
			if (!result.isSuccessful()) {
				throw new ParseProblemException(result.getProblems());
			}
			return result.getResult().orElseThrow(() -> new ParseProblemException(result.getProblems()));
		} catch (IOException | ParseProblemException exception) {
			throw new IllegalStateException("Cannot parse Java source file: " + file, exception);
		}
	}

	private String packageName(CompilationUnit unit) {
		return unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse("");
	}

	@Override
	@Nonnull
	public BuildMode scanForBuildMode() {
		if (buildConfiguration != null) {
			return buildConfiguration.buildMode();
		}
		return ProjectSourcesFinder.isGradleProject() ? BuildMode.GRADLE : BuildMode.MAVEN;
	}

	@Override
	@Nonnull
	public String[] scanForTestClasses() {
		List<CompilationUnit> units = javaFiles(testRoots()).stream().map(this::parse).toList();
		Map<CompilationUnit, Set<String>> importsByUnit = new HashMap<>();
		for (CompilationUnit unit : units) {
			importsByUnit.put(unit, importedTestAnnotations(unit));
		}
		// Project-defined annotations composed from a trusted one (meta-annotations).
		// They
		// are matched by simple name across the whole test tree; reserved names are
		// excluded
		// so a local look-alike can never join this set.
		Set<String> customAnnotations = new HashSet<>();
		boolean changed;
		do {
			changed = false;
			for (CompilationUnit unit : units) {
				Set<String> imports = importsByUnit.get(unit);
				for (AnnotationDeclaration declaration : unit.findAll(AnnotationDeclaration.class)) {
					String declarationName = declaration.getNameAsString();
					if (TEST_ANNOTATIONS.containsKey(declarationName)) {
						continue;
					}
					if (isTestAnnotated(declaration.getAnnotations(), imports, customAnnotations)) {
						changed |= customAnnotations.add(declarationName);
					}
				}
			}
		} while (changed);
		Set<String> classes = new HashSet<>();
		for (CompilationUnit unit : units) {
			Set<String> imports = importsByUnit.get(unit);
			String packageName = packageName(unit);
			for (TypeDeclaration<?> type : unit.findAll(TypeDeclaration.class)) {
				if (type.isAnnotationDeclaration()) {
					continue;
				}
				boolean junitThree = type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration()
						.getExtendedTypes().stream().anyMatch(parent -> parent.getNameAsString().equals("TestCase"));
				boolean annotatedTest = isTestAnnotated(type.getAnnotations(), imports, customAnnotations)
						|| type.getMethods().stream().anyMatch(
								method -> isTestAnnotated(method.getAnnotations(), imports, customAnnotations));
				if (junitThree || annotatedTest) {
					classes.add(qualifiedTypeName(packageName, type));
				}
			}
		}
		return classes.stream().sorted().toArray(String[]::new);
	}

	// The simple names of the reserved test annotations this unit imports, directly
	// or by
	// package wildcard.
	private Set<String> importedTestAnnotations(CompilationUnit unit) {
		Set<String> imported = new HashSet<>();
		for (ImportDeclaration importDeclaration : unit.getImports()) {
			String importedName = importDeclaration.getNameAsString();
			for (Map.Entry<String, Set<String>> annotation : TEST_ANNOTATIONS.entrySet()) {
				for (String qualifiedName : annotation.getValue()) {
					String annotationPackage = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
					boolean directImport = !importDeclaration.isAsterisk() && importedName.equals(qualifiedName);
					boolean wildcardImport = importDeclaration.isAsterisk() && importedName.equals(annotationPackage);
					if (directImport || wildcardImport) {
						imported.add(annotation.getKey());
					}
				}
			}
		}
		return imported;
	}

	private boolean isTestAnnotated(List<AnnotationExpr> annotations, Set<String> imports,
			Set<String> customAnnotations) {
		return annotations.stream().map(annotation -> annotation.getNameAsString())
				.anyMatch(name -> isTrustedAnnotation(name, imports) || customAnnotations.contains(simpleName(name)));
	}

	// A fully-qualified use is trusted only when it names a known type; a bare
	// simple name
	// only when the unit imports the matching type. A locally declared look-alike,
	// imported
	// from nowhere, is therefore rejected.
	private boolean isTrustedAnnotation(String name, Set<String> imports) {
		if (name.contains(".")) {
			return TEST_ANNOTATIONS.values().stream().anyMatch(qualifiedNames -> qualifiedNames.contains(name));
		}
		return imports.contains(name);
	}

	private String simpleName(String name) {
		int separator = name.lastIndexOf('.');
		return separator < 0 ? name : name.substring(separator + 1);
	}

	private String qualifiedTypeName(String packageName, TypeDeclaration<?> type) {
		List<String> names = new ArrayList<>();
		TypeDeclaration<?> current = type;
		names.add(current.getNameAsString());
		while (current.getParentNode().orElse(null) instanceof TypeDeclaration<?> parent) {
			current = parent;
			names.add(0, current.getNameAsString());
		}
		String nestedName = String.join(".", names);
		return packageName.isEmpty() ? nestedName : packageName + "." + nestedName;
	}

	@Override
	@Nonnull
	public String scanForPackageName() {
		Map<String, Long> counts = new HashMap<>();
		for (Path file : javaFiles(productionRoots())) {
			String name = packageName(parse(file));
			if (!name.isBlank() && ReservedPackageGuard.reservedPrefixOf(name) == null) {
				counts.merge(name, 1L, Long::sum);
			}
		}
		return counts.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
						.thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey).findFirst().orElse(getDefaultPackage());
	}

	@Override
	@Nonnull
	public String scanForMainClassInPackage() {
		List<String> names = new ArrayList<>();
		for (Path file : javaFiles(productionRoots())) {
			CompilationUnit unit = parse(file);
			for (MethodDeclaration method : unit.findAll(MethodDeclaration.class)) {
				if (isMainMethod(method)) {
					method.findAncestor(TypeDeclaration.class).ifPresent(type -> names.add(type.getNameAsString()));
				}
			}
		}
		return names.stream().distinct()
				.sorted(Comparator.comparing((String name) -> !"Main".equals(name))
						.thenComparing(name -> !"Application".equals(name)).thenComparing(String::compareTo))
				.findFirst().orElse(getDefaultMainClass());
	}

	private boolean isMainMethod(MethodDeclaration method) {
		if (!"main".equals(method.getNameAsString()) || !method.isPublic() || !method.isStatic()
				|| !method.getType().isVoidType() || method.getParameters().size() != 1) {
			return false;
		}
		var parameter = method.getParameter(0);
		if (parameter.isVarArgs()) {
			return "String".equals(parameter.getType().asString())
					|| "java.lang.String".equals(parameter.getType().asString());
		}
		if (!(parameter.getType() instanceof ArrayType arrayType)) {
			return false;
		}
		String component = arrayType.getComponentType().asString();
		return "String".equals(component) || "java.lang.String".equals(component);
	}

	@Override
	@Nonnull
	public Path scanForTestPath() {
		return testRoots().stream().findFirst().orElse(Path.of(DEFAULT_TEST_PATH));
	}

	private static final String DEFAULT_TEST_PATH = "src/test/java";
}
