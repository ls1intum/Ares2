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
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;
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
	 * Derives the supervised package from what the project declares, and falls back
	 * to a configured default only where it declares nothing.
	 * <p>
	 * Resolution runs in three steps, and each falls through only when it finds
	 * nothing at all:
	 * <ol>
	 * <li>the most frequent non-reserved package declared by the production
	 * <em>sources</em>;</li>
	 * <li>otherwise the most frequent non-reserved package declared by the compiled
	 * production <em>output</em>. This covers a project whose build descriptor the
	 * source-root discovery cannot parse, because the build tool writes its output
	 * to the conventional location this scanner reads;</li>
	 * <li>otherwise {@link #getDefaultPackage()}, with a warning naming the roots
	 * that were searched. Reaching this step means nothing eligible was declared,
	 * and a default the project does not contain mis-scopes enforcement silently,
	 * so the warning is the only signal a reader gets.</li>
	 * </ol>
	 * <b>What the result is not.</b> It is a heuristic, and the steps above narrow
	 * the ways it goes wrong rather than closing them:
	 * <ul>
	 * <li>The vote is influenceable by whoever can add files to the project.
	 * Reserved prefixes are filtered out, so a trusted namespace cannot win, but no
	 * other namespace is protected: enough classes under a package of the
	 * submitter's choosing make that package the derived scope, and enforcement
	 * then covers it instead of the assignment. Step 2 changes the unit that is
	 * counted, not who controls it.</li>
	 * <li>Step 2 reads the build tool's conventional output directory rather than
	 * one taken from the descriptor, so a build that writes elsewhere is not
	 * followed there and the step finds nothing.</li>
	 * <li>Nothing here asserts that the derived package covers every production
	 * class, so a scope that omits part of the project passes unremarked.</li>
	 * </ul>
	 * An exercise that needs a scope it can rely on pins
	 * {@code theSupervisedCodeUsesTheFollowingPackage} in a policy; this method is
	 * then not consulted at all.
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
		if (counts.isEmpty()) {
			String defaultPackage = getDefaultPackage();
			LOG.warn(
					"No supervised package could be detected: neither a production source under {} nor a compiled class under {} declared an eligible package, "
							+ "that is one that is neither the default package nor a reserved one. "
							+ "Falling back to the configured default \"{}\", which enforces nothing if the project does not contain it.",
					productionRoots(), productionOutputRoot(), defaultPackage);
			return defaultPackage;
		}
		return counts.entrySet().stream().sorted(
				Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
				.map(Map.Entry::getKey).findFirst().orElseThrow();
	}

	/**
	 * Refuses a derived supervised scope that the compiled project contradicts.
	 * <p>
	 * {@link #scanForPackageName()} answers a heuristic. This is the check that
	 * turns it into a boundary, and it is deliberately separate: derivation runs
	 * while test cases are still being written, when nothing is compiled yet, while
	 * this runs immediately before enforcement is armed, when the compiled output
	 * is the whole truth about what will execute.
	 * <p>
	 * The invariant: every executable top-level class in the production output
	 * declares a non-blank, non-reserved package that is the derived scope or lies
	 * below it, compared on segment boundaries. Anything else is refused, because
	 * each alternative is a way for supervised code to sit outside the boundary
	 * drawn around it:
	 * <ul>
	 * <li>a class in a package the scope does not cover is simply unsupervised, and
	 * that is what a decoy package buys whoever adds it;</li>
	 * <li>a class in the default package cannot be covered by any scope at
	 * all.</li>
	 * </ul>
	 * A class no scope could cover is left out of the inventory rather than
	 * refused, and where that leaves nothing at all the check passes with a
	 * warning: there is then no supervisable code, so enforcement is vacuous rather
	 * than mis-scoped. Refusing is the point. A mis-scoped run reports nothing,
	 * passes, and enforces nothing; a refusal names the offending package and tells
	 * the instructor to pin {@code theSupervisedCodeUsesTheFollowingPackage}, which
	 * is the one form of the scope that cannot be steered from the submission.
	 * <p>
	 * Only the policy-free path calls this. A pinned policy may deliberately
	 * supervise part of the output, and narrowing it is then the instructor's
	 * decision rather than a heuristic's mistake.
	 *
	 * @param derivedPackage the scope {@link #scanForPackageName()} answered
	 * @throws SecurityException when the compiled project contradicts it
	 */
	public void requireDerivedScopeToCoverTheProject(@Nonnull String derivedPackage) {
		Path outputRoot = productionOutputRoot();
		if (Files.exists(outputRoot) && !Files.isReadable(outputRoot)) {
			// Present but unreadable is a failure to verify, which is not the same as
			// nothing being there, and only the second of those is safe to wave through.
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()));
		}
		long compiledFiles = countCompiledFiles(outputRoot);
		if (compiledFiles == 0) {
			// Nothing was compiled, so nothing can be verified. Enforcement over an
			// unverified scope is enforcement nobody has checked, and this runs
			// immediately before it is armed.
			throw new SecurityException(Messages.localized("security.scope.nothing.compiled", outputRoot.toString()));
		}
		List<JavaClass> imported = importedClassesIn(outputRoot);
		if (imported.isEmpty()) {
			// Class files are present but none of them could be read. A non-empty tree
			// that yields no class is a failure to verify rather than an empty project.
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()));
		}
		List<JavaClass> executable = new ArrayList<>();
		for (JavaClass javaClass : imported) {
			if (javaClass.isTopLevelClass() && !isCompilationMetadata(javaClass)) {
				executable.add(javaClass);
			}
		}
		if (executable.isEmpty()) {
			// Only package or module descriptors, which cannot run. There is nothing to
			// supervise and nothing that says what the scope should have been.
			throw new SecurityException(Messages.localized("security.scope.nothing.compiled", outputRoot.toString()));
		}
		// The default package is checked before the reserved one so that the more
		// specific diagnostic wins: a class with no package at all cannot lie within
		// any scope, whereas a reserved one is a naming problem with its own remedy.
		for (JavaClass javaClass : executable) {
			if (javaClass.getPackageName().isBlank()) {
				throw new SecurityException(Messages.localized("security.scope.default.package", javaClass.getName(),
						outputRoot.toString()));
			}
		}
		for (JavaClass javaClass : executable) {
			String reserved = ReservedPackageGuard.reservedPrefixOf(javaClass.getPackageName());
			if (reserved != null) {
				// Refused rather than filtered out. Leaving these to one side used to be
				// justified as sparing a project whose production output is infrastructure,
				// but "every class is reserved" is a state a submission can produce, and one
				// that then passed unenforced. Ares' own build reaches it too, which is why
				// its self-tests declare their scope in a policy instead of deriving one.
				throw new SecurityException(Messages.localized("security.scope.reserved.package", javaClass.getName(),
						reserved));
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
	 * How many compiled class files the output root holds.
	 * <p>
	 * Counted on disk rather than taken from the import, because the two answer
	 * different questions. A non-empty import proves that something was read, not
	 * that everything was: files the importer skipped would otherwise leave the
	 * inventory looking complete. Where the tree cannot be walked at all the count
	 * is reported as unreadable rather than as zero, since not knowing and knowing
	 * there is nothing are the two states this check exists to tell apart.
	 *
	 * @param outputRoot the production output root
	 * @return the number of class files found
	 */
	private long countCompiledFiles(Path outputRoot) {
		if (!Files.isDirectory(outputRoot)) {
			return 0L;
		}
		try (Stream<Path> tree = Files.walk(outputRoot)) {
			return tree.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".class")).count();
		} catch (IOException exception) {
			throw new SecurityException(Messages.localized("security.scope.output.unreadable", outputRoot.toString()),
					exception);
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
	 * The compiled production classes that any scope could cover.
	 * <p>
	 * Package and module descriptors are excluded because they cannot run, and
	 * reserved-package classes because no scope may name them: the guard refuses a
	 * reserved scope, and the runtime exempts those frames by name whatever the
	 * scope is. Demanding that the derived scope cover them would demand the
	 * impossible, and would refuse every project whose production output is
	 * infrastructure, which is what Ares' own build is. Keeping supervised code out
	 * of that namespace is the reserved-package build boundary's job, and this
	 * check neither performs nor replaces it.
	 */
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
	 * Resolves the compiled production output root for the discovered build tool.
	 * <p>
	 * Without a build configuration this mirrors
	 * {@link BuildMode#getClasspath(Path, String)}, which resolves the build-tool
	 * directory against the working directory of the test run.
	 *
	 * @return the production output root; never null
	 */
	/**
	 * Whether the discovered production source roots are the whole of the main
	 * source set, rather than as much of it as the build descriptor could be read
	 * for.
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
