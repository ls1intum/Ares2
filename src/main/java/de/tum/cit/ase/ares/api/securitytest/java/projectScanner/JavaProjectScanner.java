package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG = LoggerFactory.getLogger(JavaProjectScanner.class);
	// The test annotations Ares recognises, as fully-qualified type names. An
	// annotation use counts only when it resolves to one of these types: a
	// fully-qualified use names the type outright, and a bare simple name is
	// resolved through the using compilation unit's own declarations, its
	// imports and its package, in the order Java itself applies. A locally
	// declared look-alike therefore never marks a class as a test in the
	// no-policy scan.
	//
	// Maintenance: an annotation absent from this set is silently not
	// recognised, so its tests are no longer detected. Add new JUnit, jqwik
	// or Ares test annotation types here when they are introduced, for
	// example a future de.tum.cit.ase.ares.api.jqwik.PublicTest (only the
	// Jupiter variants of PublicTest/HiddenTest exist today; jqwik supplies
	// Public/Hidden, which accompany net.jqwik.api.Property/Example).
	private static final Set<String> TEST_ANNOTATIONS = Set.of("org.junit.jupiter.api.Test", "org.junit.Test",
			"org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest",
			"org.junit.jupiter.api.TestFactory", "org.junit.jupiter.api.TestTemplate", "net.jqwik.api.Property",
			"net.jqwik.api.Example", "de.tum.cit.ase.ares.api.jupiter.PublicTest",
			"de.tum.cit.ase.ares.api.jupiter.HiddenTest");
	// The simple names of those types are reserved: a project-defined annotation
	// may not claim one. Were it allowed to, a look-alike meta-annotated with a
	// genuine test annotation would re-enter through the composed-annotation set
	// and defeat the resolution check.
	private static final Set<String> RESERVED_ANNOTATION_NAMES = TEST_ANNOTATIONS.stream()
			.map(JavaProjectScanner::simpleNameOf).collect(Collectors.toUnmodifiableSet());
	// The superclass JUnit 3 marks a test class with, recognised on the same terms.
	private static final String JUNIT_THREE_TEST_CASE = "junit.framework.TestCase";
	private final BuildToolConfiguration buildConfiguration;
	private final JavaParser parser = new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));

	public JavaProjectScanner() {
		this.buildConfiguration = null;
	}

	public JavaProjectScanner(BuildToolConfiguration buildConfiguration) {
		this.buildConfiguration = Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
	}

	/**
	 * Returns the scanner to use once the build configuration has been discovered.
	 * <p>
	 * A framework-default scanner is created without a build configuration and is
	 * rebound here to the one discovered for the run. A caller-supplied subclass is
	 * returned unchanged, so a custom scanner is never silently replaced; a
	 * subclass that does want to be rebound overrides this method (as
	 * {@link JavaProgrammingExerciseProjectScanner} does). This lets the director
	 * ask each collaborator to configure itself, rather than inspecting its
	 * concrete type.
	 * <p>
	 * Compatibility note: a custom scanner subclass that does not override this
	 * method is now kept as injected rather than being replaced by a plain scanner
	 * bound to the discovered configuration. It therefore keeps whatever
	 * configuration it was built with (or the legacy source-discovery fallback of a
	 * no-argument subclass); such a subclass should override this method if it
	 * needs the discovered configuration.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param buildConfiguration the discovered build configuration; must not be
	 *                           null.
	 * @return the scanner bound to the configuration, or this instance when it is a
	 *         custom subclass.
	 */
	@Nonnull
	public JavaProjectScanner withBuildConfiguration(@Nonnull BuildToolConfiguration buildConfiguration) {
		Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
		return getClass() == JavaProjectScanner.class ? new JavaProjectScanner(buildConfiguration) : this;
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

	private static String packageName(CompilationUnit unit) {
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
		AnnotationResolver resolver = new AnnotationResolver(units);
		Set<String> classes = new HashSet<>();
		for (CompilationUnit unit : units) {
			String packageName = packageName(unit);
			for (TypeDeclaration<?> type : unit.findAll(TypeDeclaration.class)) {
				if (type.isAnnotationDeclaration()) {
					continue;
				}
				boolean junitThree = type.isClassOrInterfaceDeclaration()
						&& type.asClassOrInterfaceDeclaration().getExtendedTypes().stream()
								.anyMatch(parent -> resolver.extendsTestCase(unit, parent.getNameAsString()));
				boolean annotatedTest = resolver.marksATest(unit, type.getAnnotations()) || type.getMethods().stream()
						.anyMatch(method -> resolver.marksATest(unit, method.getAnnotations()));
				if (junitThree || annotatedTest) {
					classes.add(qualifiedTypeName(packageName, type));
				}
			}
		}
		String[] recognised = classes.stream().sorted().toArray(String[]::new);
		reportRecognisedTestClasses(recognised);
		return recognised;
	}

	/**
	 * Reports what the no-policy scan recognised as test classes.
	 * <p>
	 * Only this path derives the exempt test classes from project code; with a
	 * policy present they come solely from
	 * {@code theFollowingClassesAreTestClasses} and this scanner is never
	 * consulted. Which classes the scan recognised is therefore the one thing an
	 * exercise author needs to see when a test is unexpectedly supervised, or
	 * unexpectedly exempt, and there was previously no way to observe it.
	 * </p>
	 * <p>
	 * The count is safe to surface anywhere. The names are not: a build log that
	 * reaches students would disclose the hidden-test structure. Hence the count at
	 * INFO and the names at TRACE, which the shipped {@code logback.xml} does not
	 * enable, its root level being {@code debug}. An instructor who needs the names
	 * raises the level for this logger alone.
	 * </p>
	 *
	 * @param recognised the recognised test classes, sorted.
	 */
	private static void reportRecognisedTestClasses(@Nonnull String[] recognised) {
		LOG.info("No-policy scan recognised {} test class(es).", recognised.length);
		if (LOG.isTraceEnabled()) {
			LOG.trace("No-policy scan recognised these test classes: {}", String.join(", ", recognised));
		}
	}

	/**
	 * Decides which annotation uses in a scanned test tree mark a test, on the
	 * identity of the annotation type rather than on its simple name.
	 * <p>
	 * A use is resolved to the fully-qualified type(s) it may denote, in the order
	 * Java itself applies: a declaration in the using compilation unit shadows
	 * every import, a single-type import outranks the using unit's package, that
	 * package outranks a package wildcard, and a wildcard can only contribute a
	 * type the scan already knows. The use marks a test when every candidate is a
	 * trusted type, so an ambiguous use fails closed.
	 * </p>
	 */
	private static final class AnnotationResolver {
		// The annotation types each scanned compilation unit declares, by simple name.
		// Keyed by identity: JavaParser nodes compare structurally, so two files with
		// equal contents would otherwise share one entry.
		private final Map<CompilationUnit, Map<String, Set<String>>> declaredInUnit = new IdentityHashMap<>();
		// The same declarations, grouped by the package that contains them.
		private final Map<String, Map<String, Set<String>>> declaredInPackage = new HashMap<>();
		// The top-level types each unit declares, and the same grouped by package. A
		// qualified name's leftmost segment is looked up here, because Java reads that
		// segment as a type whenever one is in scope and only as a package otherwise.
		private final Map<CompilationUnit, Map<String, Set<String>>> topLevelTypesInUnit = new IdentityHashMap<>();
		private final Map<String, Map<String, Set<String>>> topLevelTypesInPackage = new HashMap<>();
		// Every annotation type the scan declares, whether trusted or not. A wildcard
		// candidate naming one of these is a type that really exists and really could
		// be what the use denotes, so it has to be weighed rather than discarded.
		private final Set<String> declaredAnnotations = new HashSet<>();
		// The fully-qualified types that mark a test: the reserved ones, plus the
		// project-defined ones composed from them.
		private final Set<String> trusted = new HashSet<>(TEST_ANNOTATIONS);

		private AnnotationResolver(List<CompilationUnit> units) {
			for (CompilationUnit unit : units) {
				String packageName = packageName(unit);
				Map<String, Set<String>> inUnit = declaredInUnit.computeIfAbsent(unit, key -> new HashMap<>());
				Map<String, Set<String>> inPackage = declaredInPackage.computeIfAbsent(packageName,
						key -> new HashMap<>());
				for (AnnotationDeclaration declaration : unit.findAll(AnnotationDeclaration.class)) {
					String qualifiedName = qualifiedTypeName(packageName, declaration);
					index(inUnit, inPackage, declaration.getNameAsString(), qualifiedName);
					declaredAnnotations.add(qualifiedName);
				}
				Map<String, Set<String>> typesInUnit = topLevelTypesInUnit.computeIfAbsent(unit,
						key -> new HashMap<>());
				Map<String, Set<String>> typesInPackage = topLevelTypesInPackage.computeIfAbsent(packageName,
						key -> new HashMap<>());
				for (TypeDeclaration<?> type : unit.getTypes()) {
					index(typesInUnit, typesInPackage, type.getNameAsString(), qualifiedTypeName(packageName, type));
				}
			}
			trustComposedAnnotations(units);
		}

		private static void index(Map<String, Set<String>> inUnit, Map<String, Set<String>> inPackage,
				String simpleName, String qualifiedName) {
			inUnit.computeIfAbsent(simpleName, key -> new HashSet<>()).add(qualifiedName);
			inPackage.computeIfAbsent(simpleName, key -> new HashSet<>()).add(qualifiedName);
		}

		// A project-defined annotation meta-annotated with a trusted one marks a test
		// too, and so does one composed from such an annotation, hence the fixed point.
		// It is recorded by its own fully-qualified name, so an unrelated look-alike of
		// the same simple name elsewhere in the tree does not inherit that trust.
		// Reserved simple names never enter the set, or a local look-alike carrying a
		// genuine @Test would trust itself.
		private void trustComposedAnnotations(List<CompilationUnit> units) {
			boolean changed;
			do {
				changed = false;
				for (CompilationUnit unit : units) {
					String packageName = packageName(unit);
					for (AnnotationDeclaration declaration : unit.findAll(AnnotationDeclaration.class)) {
						if (RESERVED_ANNOTATION_NAMES.contains(declaration.getNameAsString())) {
							continue;
						}
						if (marksATest(unit, declaration.getAnnotations())) {
							changed |= trusted.add(qualifiedTypeName(packageName, declaration));
						}
					}
				}
			} while (changed);
		}

		// JUnit 3 marks a test class by inheritance rather than by annotation, so the
		// superclass has to meet the same trust rule as an annotation use. Written out
		// in full the name has to denote junit.framework.TestCase; as a simple name it
		// counts only while it resolves to no type this scan declares, which leaves the
		// TestCase the project depends on. A source declaring its own is thus no way
		// in.
		private boolean extendsTestCase(CompilationUnit unit, String name) {
			if (!simpleNameOf(name).equals("TestCase")) {
				return false;
			}
			Set<String> candidates = name.contains(".") ? resolveQualified(unit, name)
					: resolveTopLevelType(unit, name);
			return candidates.isEmpty() || candidates.equals(Set.of(JUNIT_THREE_TEST_CASE));
		}

		private boolean marksATest(CompilationUnit unit, List<AnnotationExpr> annotations) {
			return annotations.stream().map(annotation -> annotation.getNameAsString())
					.anyMatch(name -> isTrusted(unit, name));
		}

		private boolean isTrusted(CompilationUnit unit, String name) {
			Set<String> candidates = resolve(unit, name);
			return !candidates.isEmpty() && trusted.containsAll(candidates);
		}

		private Set<String> resolve(CompilationUnit unit, String name) {
			if (name.contains(".")) {
				return resolveQualified(unit, name);
			}
			Set<String> inUnit = declaredInUnit.getOrDefault(unit, Map.of()).getOrDefault(name, Set.of());
			if (!inUnit.isEmpty()) {
				return inUnit;
			}
			Set<String> imported = new HashSet<>();
			Set<String> wildcarded = new HashSet<>();
			for (ImportDeclaration importDeclaration : unit.getImports()) {
				String importedName = importDeclaration.getNameAsString();
				if (importDeclaration.isAsterisk()) {
					wildcarded.add(importedName + "." + name);
				} else if (simpleNameOf(importedName).equals(name)) {
					imported.add(importedName);
				}
			}
			if (!imported.isEmpty()) {
				return imported;
			}
			Set<String> inPackage = declaredInPackage.getOrDefault(packageName(unit), Map.of()).getOrDefault(name,
					Set.of());
			if (!inPackage.isEmpty()) {
				return inPackage;
			}
			// A wildcard names a package, not a type, so it can only stand for a type this
			// scan knows of; one naming an unknown type stands for nothing and drops out.
			// Every known candidate stays, including the untrusted ones: where two
			// wildcards both offer the simple name, the use is ambiguous between them and
			// isTrusted then refuses it rather than picking the trusted one.
			wildcarded.removeIf(candidate -> !trusted.contains(candidate) && !declaredAnnotations.contains(candidate));
			return wildcarded;
		}

		// A dotted name is a fully-qualified type name only while its leftmost segment
		// names no type in scope. Java reclassifies that segment as a type as soon as
		// one is (JLS 6.5.4), so a unit declaring "class org" binds
		// @org.junit.jupiter.api.Test to its own nest rather than to JUnit. Reading the
		// prefix the same way also keeps a nested annotation reached as @Outer.Inner
		// recognised.
		private Set<String> resolveQualified(CompilationUnit unit, String name) {
			int separator = name.indexOf('.');
			Set<String> prefixes = resolveTopLevelType(unit, name.substring(0, separator));
			if (prefixes.isEmpty()) {
				return Set.of(name);
			}
			String remainder = name.substring(separator + 1);
			return prefixes.stream().map(prefix -> prefix + "." + remainder).collect(Collectors.toSet());
		}

		// A wildcard counts here too. It cannot introduce a segment this scan has never
		// seen, but it can bring a scanned type into scope under the segment's name,
		// and
		// that type then obscures the package the segment would otherwise have named.
		private Set<String> resolveTopLevelType(CompilationUnit unit, String name) {
			Set<String> inUnit = topLevelTypesInUnit.getOrDefault(unit, Map.of()).getOrDefault(name, Set.of());
			if (!inUnit.isEmpty()) {
				return inUnit;
			}
			Set<String> imported = new HashSet<>();
			Set<String> wildcarded = new HashSet<>();
			for (ImportDeclaration importDeclaration : unit.getImports()) {
				String importedName = importDeclaration.getNameAsString();
				if (importDeclaration.isAsterisk()) {
					wildcarded.addAll(
							topLevelTypesInPackage.getOrDefault(importedName, Map.of()).getOrDefault(name, Set.of()));
				} else if (simpleNameOf(importedName).equals(name)) {
					imported.add(importedName);
				}
			}
			if (!imported.isEmpty()) {
				return imported;
			}
			Set<String> inPackage = topLevelTypesInPackage.getOrDefault(packageName(unit), Map.of()).getOrDefault(name,
					Set.of());
			return inPackage.isEmpty() ? wildcarded : inPackage;
		}
	}

	private static String simpleNameOf(String name) {
		int separator = name.lastIndexOf('.');
		return separator < 0 ? name : name.substring(separator + 1);
	}

	private static String qualifiedTypeName(String packageName, TypeDeclaration<?> type) {
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
