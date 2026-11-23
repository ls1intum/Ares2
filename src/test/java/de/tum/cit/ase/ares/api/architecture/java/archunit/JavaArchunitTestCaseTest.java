package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

public class JavaArchunitTestCaseTest {

	private JavaArchunitTestCase.Builder builder;

	@BeforeEach
	void setUp() {
		builder = JavaArchunitTestCase.archunitBuilder();
	}

	@Test
	void builder_missingParameters_throwsNullPointerException() {
		// Missing all parameters
		assertThrows(NullPointerException.class, () -> builder.build());

		// Only supported set
		builder.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION);
		assertThrows(NullPointerException.class, () -> builder.build());

		// Supported and allowedPackages
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		builder.allowedPackages(allowed);
		assertThrows(NullPointerException.class, () -> builder.build());
	}

	@Test
	void builder_allParameters_buildsSuccessfully() {
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");

		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		assertNotNull(testCase);
	}

	@Test
	void allowedPackagesAsCode_emptySet_returnsSetOf() throws Exception {
		Set<PackagePermission> allowed = Collections.emptySet();
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
		method.setAccessible(true);
		String result = (String) method.invoke(testCase);
		assertEquals("Set.of()", result);
	}

	@Test
	void allowedPackagesAsCode_nonEmptySet_returnsCorrectLiteral() throws Exception {
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.test.pkg"));
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
		method.setAccessible(true);
		String result = (String) method.invoke(testCase);
		assertTrue(result.startsWith("Set.of("));
		assertTrue(result.contains("PackagePermission"));
	}

	/**
	 * The emitted code used to carry the packages seen when the file was written.
	 * That is exactly what made a precompiled suite vacuous: during generation
	 * nothing is compiled, so the set was empty and the emission was
	 * importPackages() with no arguments, which every rule then checked and passed.
	 * What is emitted now is a question asked at the generated test's own runtime.
	 */
	@Test
	void javaClassesAsCode_derivedScope_asksForTheValidatedInventoryAtRuntime() throws Exception {
		JavaClasses emptyClasses = new ClassFileImporter().importPackages("non.existent.package");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(emptyClasses).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(true).build();

		String result = javaClassesAsCode(testCase);

		assertEquals("JavaArchunitSupervisedClasses.validated(\"de.tum.cit.aet\")", result,
				"a derived scope has to be checked against the whole compiled output before it is used");
		assertFalse(result.contains("importPackages"),
				"nothing may be baked in at generation time, since that is what an empty precompile emitted");
	}

	/**
	 * A pinned scope is the instructor's statement rather than a reading of the
	 * project, so it imports its own subtree and is not held to whole-project
	 * coverage: a policy may deliberately supervise part of an output.
	 */
	@Test
	void javaClassesAsCode_pinnedScope_asksOnlyForItsOwnSubtree() throws Exception {
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(false).build();

		String result = javaClassesAsCode(testCase);

		assertEquals("JavaArchunitSupervisedClasses.pinned(\"de.tum.cit.aet\")", result);
	}

	/**
	 * Generating a file with no scope would generate one that analyses nothing, so
	 * it is refused where it is noticed rather than written out and discovered
	 * green.
	 */
	@Test
	void javaClassesAsCode_withoutAScope_refusesToGenerate() throws Exception {
		JavaClasses javaClasses = new ClassFileImporter().importPackages("non.existent.package");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("javaClassesAsCode");
		method.setAccessible(true);
		InvocationTargetException failure = assertThrows(InvocationTargetException.class,
				() -> method.invoke(testCase));

		assertInstanceOf(SecurityException.class, failure.getCause());
	}

	private static String javaClassesAsCode(JavaArchunitTestCase testCase) throws Exception {
		Method method = JavaArchunitTestCase.class.getDeclaredMethod("javaClassesAsCode");
		method.setAccessible(true);
		return (String) method.invoke(testCase);
	}

	@Test
	void archunitBuilder_returnsBuilderInstance() {
		assertNotNull(JavaArchunitTestCase.archunitBuilder());
	}

	/** A class with no dependencies beyond its own nest and java.lang. */
	static final class Tiny {
	}

	/** The package of {@link Tiny}, which it depends on through its nest host. */
	private static final String OWN_PACKAGE = "de.tum.cit.ase.ares.api.architecture.java.archunit";

	/**
	 * Runs the package-import rule over the given classes with the given
	 * permissions.
	 *
	 * @param javaClasses the analysed classes, shared between runs
	 * @param packages    the permitted package names
	 * @param classes     the exempted class names
	 */
	private static void runPackageImport(JavaClasses javaClasses, Set<String> packages, Set<String> classes) {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		packages.forEach(name -> allowedPackages.add(new PackagePermission(name)));
		Set<ClassPermission> allowedClasses = new HashSet<>();
		classes.forEach(name -> allowedClasses.add(new ClassPermission(name)));
		JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(allowedPackages).allowedClasses(allowedClasses).javaClasses(javaClasses).build()
				.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ");
	}

	/**
	 * A single permitted package named "a,b" is not served the cached pass of the
	 * two packages a and b.
	 */
	@Test
	void packageImport_commaJoinedPackageAfterTheSplitOnes_isNotServedTheirCachedPass() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		runPackageImport(javaClasses, Set.of("java", OWN_PACKAGE), Set.of());
		assertThrows(SecurityException.class,
				() -> runPackageImport(javaClasses, Set.of(OWN_PACKAGE + ",java"), Set.of()));
	}

	/**
	 * The two packages a and b are not served the cached failure of the single
	 * package named "a,b".
	 */
	@Test
	void packageImport_splitPackagesAfterTheCommaJoinedOne_areNotServedItsCachedFailure() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		assertThrows(SecurityException.class,
				() -> runPackageImport(javaClasses, Set.of(OWN_PACKAGE + ",java"), Set.of()));
		assertDoesNotThrow(() -> runPackageImport(javaClasses, Set.of("java", OWN_PACKAGE), Set.of()));
	}

	/**
	 * A single exempted class named "a,b" is not served the cached pass of the two
	 * exempted classes a and b.
	 */
	@Test
	void packageImport_commaJoinedExemptionAfterTheSplitOnes_isNotServedTheirCachedPass() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		String tiny = Tiny.class.getName();
		runPackageImport(javaClasses, Set.of(), Set.of(tiny, "x.Other"));
		assertThrows(SecurityException.class, () -> runPackageImport(javaClasses, Set.of(), Set.of(tiny + ",x.Other")));
	}

	/**
	 * The two exempted classes a and b are not served the cached failure of the
	 * single exempted class named "a,b".
	 */
	@Test
	void packageImport_splitExemptionsAfterTheCommaJoinedOne_areNotServedItsCachedFailure() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		String tiny = Tiny.class.getName();
		assertThrows(SecurityException.class, () -> runPackageImport(javaClasses, Set.of(), Set.of(tiny + ",x.Other")));
		assertDoesNotThrow(() -> runPackageImport(javaClasses, Set.of(), Set.of(tiny, "x.Other")));
	}

	/**
	 * Waiting for a rule evaluation that is interrupted fails closed with the Ares
	 * error template and keeps the thread's interrupt flag set.
	 *
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	@Test
	void anInterruptedWaitFailsClosedAndKeepsTheInterrupt() throws ReflectiveOperationException {
		FutureTask<Optional<SecurityException>> unfinished = new FutureTask<>(Optional::empty);
		Thread.currentThread().interrupt();
		try {
			SecurityException refused = awaitOutcomeFailure(unfinished);
			assertEquals(Messages.localized("security.architecture.rule.evaluation.interrupted"), refused.getMessage());
			assertInstanceOf(InterruptedException.class, refused.getCause());
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
		}
	}

	/**
	 * A rule evaluation that ends with a checked exception fails closed with the
	 * Ares error template and keeps the failure as its cause.
	 *
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	@Test
	void aCheckedEvaluationFailureFailsClosed() throws ReflectiveOperationException {
		FutureTask<Optional<SecurityException>> failing = new FutureTask<>(() -> {
			throw new java.io.IOException("checked");
		});
		failing.run();
		SecurityException refused = awaitOutcomeFailure(failing);
		assertEquals(Messages.localized("security.architecture.rule.evaluation.failed"), refused.getMessage());
		assertInstanceOf(ExecutionException.class, refused.getCause());
	}

	/**
	 * Calls the private wait for a rule outcome and returns what it threw.
	 *
	 * @param task the rule evaluation to wait for
	 * @return the exception the wait threw
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	private static SecurityException awaitOutcomeFailure(FutureTask<Optional<SecurityException>> task)
			throws ReflectiveOperationException {
		Method await = JavaArchunitTestCase.class.getDeclaredMethod("awaitOutcome", FutureTask.class);
		await.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
				() -> await.invoke(null, task));
		return assertInstanceOf(SecurityException.class, thrown.getCause());
	}

	/**
	 * A class that deletes a file, which the file system rule refuses.
	 */
	private static final String DELETING_FIXTURE = """
			package p;
			public class DeletingFixture {
				public boolean run(java.io.File f) {
					return f.delete();
				}
			}
			""";

	/**
	 * A class that calls nothing forbidden.
	 */
	private static final String CLEAN_FIXTURE = """
			package p;
			public class CleanFixture {
				public int run() {
					return 1;
				}
			}
			""";

	/**
	 * A class whose only dependencies are {@code java.lang} and
	 * {@code java.util.List}.
	 */
	private static final String LIST_FIXTURE = """
			package p;
			public class ListFixture {
				public java.util.List<String> items() {
					return null;
				}
			}
			""";

	/**
	 * Compiles one source file with {@code javac} and imports the result, so each
	 * test owns an import that no other test has cached a verdict for.
	 *
	 * @param directory the directory to work in
	 * @param path      the path of the source file below the source folder
	 * @param source    the source text
	 * @return the imported classes
	 * @throws IOException if the source file cannot be written
	 */
	private static JavaClasses compileAndImport(Path directory, String path, String source) throws IOException {
		Path file = directory.resolve("src").resolve(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
		Path classRoot = Files.createDirectories(directory.resolve("classes"));
		List<Path> files = new ArrayList<>();
		files.add(file);
		JavacFixtures.compileSources(JavacFixtures.testClasses().toString(), classRoot, files);
		return new ClassFileImporter().importPath(classRoot);
	}

	/**
	 * Builds a test case for one category over the given classes.
	 *
	 * @param supported the category
	 * @param packages  the permitted package names
	 * @param classes   the analysed classes
	 * @return the test case
	 */
	private static JavaArchunitTestCase testCase(JavaArchitectureTestCaseSupported supported, Set<String> packages,
			JavaClasses classes) {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		packages.forEach(name -> allowedPackages.add(new PackagePermission(name)));
		return JavaArchunitTestCase.archunitBuilder().javaArchitectureTestCaseSupported(supported)
				.allowedPackages(allowedPackages).javaClasses(classes).build();
	}

	/**
	 * A method rule refuses a class that deletes a file and passes a clean one, and
	 * a second run over the same classes gives the same verdict.
	 *
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@Test
	void executesAMethodRuleAndCachesItsVerdict(@TempDir Path directory) throws IOException {
		JavaClasses deleting = compileAndImport(directory.resolve("deleting"), "p/DeletingFixture.java",
				DELETING_FIXTURE);
		JavaClasses clean = compileAndImport(directory.resolve("clean"), "p/CleanFixture.java", CLEAN_FIXTURE);
		JavaArchunitTestCase refused = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(),
				deleting);
		JavaArchunitTestCase passed = testCase(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, Set.of(),
				clean);

		SecurityException first = assertThrows(SecurityException.class,
				() -> refused.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
		SecurityException second = assertThrows(SecurityException.class,
				() -> refused.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));

		assertSame(first, second);
		assertDoesNotThrow(() -> passed.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
		assertDoesNotThrow(() -> passed.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
	}

	/**
	 * The verdict of the package rule is kept per set of permitted names, so the
	 * same classes are refused once the permitted name no longer covers them.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void keysThePackageVerdictByThePermittedNames(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, "p/ListFixture.java", LIST_FIXTURE);
		JavaArchunitTestCase covered = testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				Set.of("java.util", "java.lang"), classes);
		JavaArchunitTestCase uncovered = testCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				Set.of("java.utilx", "java.lang"), classes);

		assertDoesNotThrow(() -> covered.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
		assertThrows(SecurityException.class, () -> uncovered.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
	}

	/**
	 * A supervised package of blanks is refused when the test file is written.
	 */
	@Test
	void refusesABlankScope() {
		JavaClasses classes = new ClassFileImporter().importClasses(Tiny.class);
		JavaArchunitTestCase testCase = JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(Set.of()).javaClasses(classes).supervisedPackage("  ").build();

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> testCase.writeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));

		assertEquals(Messages.localized("security.architecture.scope.missing"), refusal.getMessage());
	}

	/**
	 * An unchecked failure of the rule evaluation is rethrown as the same object.
	 *
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	@Test
	void anUncheckedEvaluationFailureIsRethrownAsItIs() throws ReflectiveOperationException {
		IllegalStateException failure = new IllegalStateException("unchecked");
		FutureTask<Optional<SecurityException>> failing = new FutureTask<>(() -> {
			throw failure;
		});
		failing.run();

		assertSame(failure, awaitOutcomeCause(failing));
	}

	/**
	 * An error raised by the rule evaluation is rethrown as the same object.
	 *
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	@Test
	void anErrorInTheEvaluationIsRethrownAsItIs() throws ReflectiveOperationException {
		LinkageError failure = new LinkageError("linkage");
		FutureTask<Optional<SecurityException>> failing = new FutureTask<>(() -> {
			throw failure;
		});
		failing.run();

		assertSame(failure, awaitOutcomeCause(failing));
	}

	/**
	 * A plain checked exception from the rule evaluation ends as a failed
	 * evaluation.
	 *
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	@Test
	void aPlainExceptionInTheEvaluationFailsClosed() throws ReflectiveOperationException {
		Callable<Optional<SecurityException>> failing = () -> {
			throw new Exception("checked");
		};
		FutureTask<Optional<SecurityException>> task = new FutureTask<>(failing);
		task.run();

		Throwable thrown = awaitOutcomeCause(task);

		assertEquals(Messages.localized("security.architecture.rule.evaluation.failed"), thrown.getMessage());
	}

	/**
	 * Calls the private wait for a rule outcome and returns what it threw.
	 *
	 * @param task the rule evaluation to wait for
	 * @return the throwable the wait threw
	 * @throws ReflectiveOperationException if the private method cannot be found
	 */
	private static Throwable awaitOutcomeCause(FutureTask<Optional<SecurityException>> task)
			throws ReflectiveOperationException {
		Method await = JavaArchunitTestCase.class.getDeclaredMethod("awaitOutcome", FutureTask.class);
		await.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
				() -> await.invoke(null, task));
		return thrown.getCause();
	}
}
