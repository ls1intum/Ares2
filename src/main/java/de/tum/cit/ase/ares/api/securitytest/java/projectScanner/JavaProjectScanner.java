package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PolicyValueValidator;
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
	// recognised, so its tests are no longer detected. Add new JUnit or Ares
	// test annotation types here when they are introduced.
	private static final Set<String> TEST_ANNOTATIONS = Set.of("org.junit.jupiter.api.Test", "org.junit.Test",
			"org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest",
			"org.junit.jupiter.api.TestFactory", "org.junit.jupiter.api.TestTemplate",
			"de.tum.cit.ase.ares.api.jupiter.PublicTest", "de.tum.cit.ase.ares.api.jupiter.HiddenTest");
	// The simple names of those types are reserved: a project-defined annotation
	// may not claim one. Were it allowed to, a look-alike meta-annotated with a
	// genuine test annotation would re-enter through the composed-annotation set
	// and defeat the resolution check.
	private static final Set<String> RESERVED_ANNOTATION_NAMES = TEST_ANNOTATIONS.stream()
			.map(JavaProjectScanner::simpleNameOf).collect(Collectors.toUnmodifiableSet());
	// The superclass JUnit 3 marks a test class with, recognised on the same terms.
	private static final String JUNIT_THREE_TEST_CASE = "junit.framework.TestCase";
	private final BuildToolConfiguration buildConfiguration;
	/**
	 * Reads the project's Java files, set to the newest Java version this
	 * JavaParser knows. A file it reports a problem for is refused outright, so a
	 * lower version would refuse valid code written for a newer Java. Raise it when
	 * JavaParser is updated.
	 */
	private final JavaParser parser = new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_26));

	public JavaProjectScanner() {
		this.buildConfiguration = null;
	}

	public JavaProjectScanner(BuildToolConfiguration buildConfiguration) {
		this.buildConfiguration = Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
	}

	/**
	 * The last-resort supervised package, used only when neither the production
	 * sources nor the compiled production output declares one.
	 * <p>
	 * Prefer detection over this hook. A default that the project does not contain
	 * mis-scopes enforcement silently: the analysis path resolves to a directory
	 * that does not exist, no class is imported, and no resource domain is
	 * enforced, while nothing fails. {@link #scanForPackageName()} therefore
	 * reaches this value only when the project offers nothing to detect.
	 *
	 * @return the default package name
	 */
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

	/**
	 * Derives the supervised package when no policy pins one: the build
	 * descriptor's group if it covers every declared package, else the topmost
	 * declared package, the heaviest between disjoint ones, else
	 * {@link #getDefaultPackage()}.
	 * <p>
	 * Declarations come from the sources, or from the compiled output where the
	 * sources are incomplete or declare nothing.
	 *
	 * @return the supervised package name, possibly empty; never null
	 */
	@Override
	@Nonnull
	public String scanForPackageName() {
		Map<String, Long> counts = new HashMap<>();
		if (productionRootsAreComplete()) {
			for (Path file : javaFiles(productionRoots())) {
				String name = packageName(parse(file));
				if (!name.isBlank() && ReservedPackageGuard.reservedPrefixOf(name) == null) {
					counts.merge(name, 1L, Long::sum);
				}
			}
		} else {
			LOG.warn("The build descriptor declares a production source root that could not be resolved, so the "
					+ "discovered roots {} are not known to be the whole project. The sources are not counted "
					+ "at all, because a vote taken over part of a project answers confidently and wrongly; "
					+ "the compiled output is read instead.", productionRoots());
		}
		if (counts.isEmpty()) {
			counts = compiledPackageCounts();
		}
		Optional<String> group = declaredGroupCovering(counts.keySet());
		if (group.isPresent()) {
			return group.get();
		}
		if (counts.isEmpty()) {
			String defaultPackage = getDefaultPackage();
			LOG.warn(
					"No supervised package could be detected: neither a production source under {} nor a compiled class under {} declared an eligible package, "
							+ "that is one that is neither the default package nor a reserved one. "
							+ "Falling back to the configured default \"{}\", which enforces nothing if the project does not contain it.",
					productionRoots(), productionOutputRoot(), defaultPackage);
			return defaultPackage;
		}
		return heaviestTopmostPackage(counts);
	}

	/**
	 * The group the build descriptor declares, if it can be the supervised package:
	 * a valid package name, neither inside nor above a reserved namespace, and
	 * containing every declared package. Anything else is passed over, with the
	 * reason logged, so that the next rule decides.
	 *
	 * @param declared the eligible declared packages
	 * @return the group, or empty when it is absent or does not fit
	 */
	@Nonnull
	private Optional<String> declaredGroupCovering(@Nonnull Set<String> declared) {
		Optional<String> group = ProjectSourcesFinder.declaredGroup(projectRoot(), scanForBuildMode());
		if (group.isEmpty()) {
			return group;
		}
		String candidate = group.get();
		String reason = whyTheGroupDoesNotFit(candidate, declared);
		if (reason != null) {
			LOG.info("The build descriptor declares the group \"{}\", which is not used as the supervised package "
					+ "because {}.", candidate, reason);
			return Optional.empty();
		}
		LOG.info("The supervised package is the build descriptor's group \"{}\".", candidate);
		return group;
	}

	/**
	 * Why the group cannot be the supervised package, or null if it can.
	 *
	 * @param group    the declared group
	 * @param declared the eligible declared packages
	 * @return the reason, or null
	 */
	private static String whyTheGroupDoesNotFit(@Nonnull String group, @Nonnull Set<String> declared) {
		if (!PolicyValueValidator.JAVA_PACKAGE_PATTERN.matcher(group).matches()) {
			return "it is not a Java package name";
		}
		if (ReservedPackageGuard.reservedPrefixOf(group) != null
				|| ReservedPackageGuard.ancestorOfReservedPrefix(group) != null) {
			return "it lies inside or above a reserved namespace";
		}
		return declared.stream().filter(name -> !isWithinScope(name, group)).sorted().findFirst()
				.map(outside -> "the declared package " + outside + " lies outside it").orElse(null);
	}

	/**
	 * The declared package nothing else declared lies above, weighed with
	 * everything below it; between several such packages the heaviest wins, ties by
	 * name. Nested packages therefore always yield their parent.
	 *
	 * @param counts the declarations per eligible package
	 * @return the chosen package
	 */
	@Nonnull
	private static String heaviestTopmostPackage(@Nonnull Map<String, Long> counts) {
		Map<String, Long> weights = new HashMap<>();
		for (String candidate : counts.keySet()) {
			boolean nested = counts.keySet().stream()
					.anyMatch(other -> !other.equals(candidate) && isWithinScope(candidate, other));
			if (!nested) {
				weights.put(candidate,
						counts.entrySet().stream().filter(entry -> isWithinScope(entry.getKey(), candidate))
								.mapToLong(Map.Entry::getValue).sum());
			}
		}
		return weights.entrySet().stream().sorted(
				Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey).findFirst().orElseThrow();
	}

	/** The project root the build descriptor sits in. */
	@Nonnull
	private Path projectRoot() {
		return buildConfiguration != null ? buildConfiguration.projectRoot() : Path.of("").toAbsolutePath();
	}

	/**
	 * Refuses a derived supervised package the compiled output contradicts, before
	 * enforcement is armed.
	 * <p>
	 * Every class file in the output root must yield a class of its own, and every
	 * executable class must declare a non-reserved package equal to the scope or
	 * below it. Nothing executable passes with a warning. Only this root is read.
	 *
	 * @param derivedPackage the derived scope
	 * @throws SecurityException when the output contradicts it
	 */
	public void requireDerivedScopeToCoverTheProject(@Nonnull String derivedPackage) {
		Path outputRoot = productionOutputRoot();
		if (Files.exists(outputRoot) && !Files.isReadable(outputRoot)) {
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()));
		}
		String reservedBelow = ReservedPackageGuard.ancestorOfReservedPrefix(derivedPackage);
		if (reservedBelow != null) {
			throw new SecurityException(
					Messages.localized("security.scope.contains.reserved", derivedPackage, reservedBelow));
		}
		List<Path> classFiles = compiledFilesIn(outputRoot);
		if (classFiles.isEmpty()) {
			LOG.warn("No compiled production class was found under {}, so the derived supervised scope \"{}\" has "
					+ "nothing to cover and enforcement is vacuous rather than mis-scoped. This is expected "
					+ "while the supervised package is still empty.", outputRoot, derivedPackage);
			return;
		}
		List<JavaClass> imported = importedClassesIn(outputRoot);
		requireEveryClassFileToBeImported(classFiles, imported, outputRoot);
		List<JavaClass> executable = new ArrayList<>();
		for (JavaClass javaClass : imported) {
			if (!isCompilationMetadata(javaClass)) {
				executable.add(javaClass);
			}
		}
		executable.sort(Comparator.comparing(JavaClass::getName));
		if (executable.isEmpty()) {
			LOG.warn(
					"Only package or module descriptors were found under {}, so the derived supervised scope \"{}\" "
							+ "has no executable class to cover and enforcement is vacuous rather than mis-scoped.",
					outputRoot, derivedPackage);
			return;
		}
		for (JavaClass javaClass : executable) {
			if (javaClass.getPackageName().isBlank()) {
				throw new SecurityException(Messages.localized("security.scope.default.package", javaClass.getName(),
						outputRoot.toString()));
			}
		}
		for (JavaClass javaClass : executable) {
			String reserved = ReservedPackageGuard.reservedPrefixOf(javaClass.getPackageName());
			if (reserved != null) {
				throw new SecurityException(
						Messages.localized("security.scope.reserved.package", javaClass.getName(), reserved));
			}
		}
		if (derivedPackage.isBlank()) {
			throw new SecurityException(Messages.localized("security.scope.blank", outputRoot.toString()));
		}
		for (JavaClass javaClass : executable) {
			String declared = javaClass.getPackageName();
			if (!isWithinScope(declared, derivedPackage)) {
				throw new SecurityException(Messages.localized("security.scope.not.covered", derivedPackage, declared,
						javaClass.getName(), outputRoot.toString()));
			}
		}
	}

	/**
	 * The class files the output root holds, sorted, without
	 * {@code module-info.class}, which the importer never reads.
	 * <p>
	 * Listed on disk rather than taken from the import, because the importer skips
	 * a file it cannot read with only a warning. A tree that cannot be walked is
	 * reported as unreadable rather than as empty, since not knowing and knowing
	 * there is nothing are the two states this check exists to tell apart.
	 *
	 * @param outputRoot the production output root
	 * @return the class files found
	 */
	private static List<Path> compiledFilesIn(Path outputRoot) {
		if (!Files.isDirectory(outputRoot)) {
			return List.of();
		}
		try (Stream<Path> tree = Files.walk(outputRoot)) {
			return tree.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".class"))
					.filter(path -> !"module-info.class".equals(String.valueOf(path.getFileName())))
					.map(JavaProjectScanner::realPathOf).sorted().toList();
		} catch (IOException | UncheckedIOException exception) {
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()),
					exception);
		}
	}

	/**
	 * Refuses a class file that yields no imported class of its own, or lies under
	 * {@code META-INF/versions}.
	 * <p>
	 * The importer skips unreadable files and keeps one class per name, choosing
	 * unstably, so each such file is read again alone to name the defect.
	 *
	 * @param classFiles the class files, sorted
	 * @param imported   the imported classes
	 * @param outputRoot the output root
	 */
	private static void requireEveryClassFileToBeImported(List<Path> classFiles, List<JavaClass> imported,
			Path outputRoot) {
		try {
			Path root = realPathOf(outputRoot);
			for (Path classFile : classFiles) {
				if (root.relativize(classFile).startsWith(Path.of("META-INF", "versions"))) {
					throw new SecurityException(Messages.localized("security.scope.class.versioned",
							classFile.toString(), outputRoot.toString()));
				}
			}
			Map<String, List<Path>> filesByClass = filesByClass(imported);
			Set<Path> importedFiles = new HashSet<>();
			filesByClass.values().forEach(importedFiles::addAll);
			for (Path classFile : classFiles) {
				if (!importedFiles.contains(classFile)) {
					filesByClass.computeIfAbsent(classNameIn(classFile, outputRoot), key -> new ArrayList<>())
							.add(classFile);
				}
			}
			requireOneFilePerClass(filesByClass, outputRoot);
		} catch (UncheckedIOException exception) {
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()),
					exception);
		}
	}

	/**
	 * The files the imported classes were read from, with links resolved, keyed by
	 * class name.
	 */
	private static Map<String, List<Path>> filesByClass(List<JavaClass> imported) {
		Map<String, List<Path>> filesByClass = new HashMap<>();
		for (JavaClass javaClass : imported) {
			javaClass.getSource().map(source -> source.getUri()).filter(uri -> "file".equals(uri.getScheme()))
					.map(uri -> realPathOf(Path.of(uri))).ifPresent(file -> filesByClass
							.computeIfAbsent(javaClass.getName(), key -> new ArrayList<>()).add(file));
		}
		return filesByClass;
	}

	/**
	 * The name of the one class a file defines, read from that file alone, or a
	 * refusal when the importer cannot read it.
	 */
	private static String classNameIn(Path classFile, Path outputRoot) {
		Iterator<JavaClass> classes = new ClassFileImporter().importPath(classFile).iterator();
		if (!classes.hasNext()) {
			throw new SecurityException(
					Messages.localized("security.scope.class.unreadable", classFile.toString(), outputRoot.toString()));
		}
		return classes.next().getName();
	}

	/**
	 * Refuses a class defined by more than one file, naming the alphabetically
	 * first such class and its first two files, so the diagnostic does not depend
	 * on which file the importer kept.
	 */
	private static void requireOneFilePerClass(Map<String, List<Path>> filesByClass, Path outputRoot) {
		filesByClass.entrySet().stream().filter(entry -> entry.getValue().size() > 1).min(Map.Entry.comparingByKey())
				.ifPresent(entry -> {
					List<Path> files = entry.getValue().stream().sorted().toList();
					throw new SecurityException(Messages.localized("security.scope.class.duplicate", entry.getKey(),
							files.get(0).toString(), files.get(1).toString(), outputRoot.toString()));
				});
	}

	/**
	 * The path with links resolved, so that a file found by walking the tree and
	 * the same file named by the importer compare equal.
	 */
	private static Path realPathOf(Path path) {
		try {
			return path.toRealPath();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Every class the compiled production output yields, reserved and metadata
	 * included.
	 * <p>
	 * Nothing is filtered here. The caller decides what each kind of class means,
	 * because a class that cannot be supervised is not the same as one that is
	 * absent, and quietly dropping the first produced the second.
	 */
	@Nonnull
	private List<JavaClass> importedClassesIn(Path outputRoot) {
		List<JavaClass> imported = new ArrayList<>();
		for (JavaClass javaClass : new ClassFileImporter().importPath(outputRoot)) {
			imported.add(javaClass);
		}
		return imported;
	}

	/**
	 * Whether the declared package is the scope or lies below it.
	 * <p>
	 * Compared on a segment boundary, so that a scope of {@code de.tum.cit.aet}
	 * does not swallow the unrelated {@code de.tum.cit.aetevil}, which a bare
	 * prefix test would.
	 */
	private static boolean isWithinScope(String declared, String scope) {
		return declared.equals(scope) || declared.startsWith(scope + ".");
	}

	/**
	 * Whether the class file describes a package or a module rather than code that
	 * can run. Neither can be supervised and neither says anything about the scope.
	 */
	private static boolean isCompilationMetadata(JavaClass javaClass) {
		String simpleName = javaClass.getName().substring(javaClass.getName().lastIndexOf('.') + 1);
		return "package-info".equals(simpleName) || "module-info".equals(simpleName);
	}

	/**
	 * Counts the non-reserved packages declared by the compiled production classes.
	 * <p>
	 * Only top-level classes are counted: a nested or anonymous class produces its
	 * own class file, so counting every file would weight a package by how many
	 * inner classes it happens to contain. Blank package names are skipped, which
	 * also disposes of {@code module-info.class} at the root of the output tree.
	 * <p>
	 * Nesting is read from the class file, not from the binary name. A {@code '$'}
	 * in the name does not mean nested: it is a legal identifier character, so a
	 * top-level {@code Payload$Hidden} carries one and a name-based test drops it
	 * from the count.
	 *
	 * @return the package counts, empty when nothing is compiled or readable
	 */
	@Nonnull
	private Map<String, Long> compiledPackageCounts() {
		Path outputRoot = productionOutputRoot();
		if (!Files.isDirectory(outputRoot) || !Files.isReadable(outputRoot)) {
			return Map.of();
		}
		Map<String, Long> counts = new HashMap<>();
		for (JavaClass javaClass : new ClassFileImporter().importPath(outputRoot)) {
			if (!javaClass.isTopLevelClass()) {
				continue;
			}
			String name = javaClass.getPackageName();
			if (!name.isBlank() && ReservedPackageGuard.reservedPrefixOf(name) == null) {
				counts.merge(name, 1L, Long::sum);
			}
		}
		return counts;
	}

	/**
	 * Whether the discovered production source roots are the whole of the main
	 * source set, rather than as much of it as the build descriptor could be read
	 * for and found on disk.
	 * <p>
	 * Only step one of {@link #scanForPackageName()} depends on this. Counting
	 * declarations across part of a project produces an answer that looks exactly
	 * like an answer taken across all of it, so a partial set is not counted at
	 * all: the compiled output, which the build tool writes whatever the descriptor
	 * says, is read instead. Without a build configuration nothing has reported a
	 * gap, so there is none to report.
	 *
	 * @return whether the production roots are known to be complete
	 */
	private boolean productionRootsAreComplete() {
		return buildConfiguration == null || buildConfiguration.productionRootsComplete();
	}

	@Nonnull
	private Path productionOutputRoot() {
		if (buildConfiguration != null) {
			return buildConfiguration.productionOutputRoot();
		}
		return Path.of(scanForBuildMode().getBuildDirectory()).toAbsolutePath();
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
