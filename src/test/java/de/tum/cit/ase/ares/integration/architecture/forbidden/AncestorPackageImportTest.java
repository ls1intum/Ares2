package de.tum.cit.ase.ares.integration.architecture.forbidden;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;

import net.bytebuddy.ByteBuddy;

import de.tum.cit.ase.AncestorPackageAresImport;
import de.tum.cit.ase.AncestorPackageAspectJImport;
import de.tum.cit.ase.AncestorPackageByteBuddyImport;
import de.tum.cit.ase.AncestorPackageExercise;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * An exercise in the package {@code de.tum.cit.ase}, above Ares' own trusted
 * namespace, in all four mode combinations. Each runner copies one student
 * class into its own output folder and starts the matching probe. This shows
 * that the tests are created and that the import rule applies, not that the
 * WALA call-graph rules do.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(ExecutionMode.SAME_THREAD)
public class AncestorPackageImportTest {

	/**
	 * Why the probes are disabled: their runners start them through the test kit.
	 */
	private static final String PROBE_REASON = "Probe: started by the matching *_test method through EngineTestKit.";

	/**
	 * The policy of each mode combination that names no import. Ares' essential
	 * packages are permitted regardless.
	 */
	private static final String ARCHUNIT_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyAncestorPackage.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_POLICY}.
	 */
	private static final String ARCHUNIT_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyAncestorPackage.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_POLICY}.
	 */
	private static final String WALA_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyAncestorPackage.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_POLICY}.
	 */
	private static final String WALA_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyAncestorPackage.yaml";

	/**
	 * The policy of each mode combination that also names a package of Ares' API.
	 */
	private static final String ARCHUNIT_ASPECTJ_PERMITTED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyAncestorPackageAresApiPermitted.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_PERMITTED_POLICY}.
	 */
	private static final String ARCHUNIT_INSTRUMENTATION_PERMITTED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyAncestorPackageAresApiPermitted.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_PERMITTED_POLICY}.
	 */
	private static final String WALA_ASPECTJ_PERMITTED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyAncestorPackageAresApiPermitted.yaml";

	/**
	 * See {@link #ARCHUNIT_ASPECTJ_PERMITTED_POLICY}.
	 */
	private static final String WALA_INSTRUMENTATION_PERMITTED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyAncestorPackageAresApiPermitted.yaml";

	/**
	 * A call-graph violation by an Ares test class, as the rule words it: the class
	 * and method, then that it tried something.
	 */
	private static final Pattern UNRELATED_VIOLATION = Pattern.compile(
			"Student-Code;[^)]*\\): de\\.tum\\.cit\\.ase\\.ares\\.integration\\.[\\w.]+\\(\\) (tried|hat versucht)");

	/**
	 * The folder, below the test output, that holds the copied student classes.
	 */
	private static final String FIXTURE_ROOT = "ancestorpackagefixture";

	/**
	 * The analysed path of the probes for the student class without a forbidden
	 * import.
	 */
	private static final String EXERCISE_PATH = "test-classes/ancestorpackagefixture/exercise/target/classes/de/tum/cit/ase";

	/**
	 * The analysed path of the probes for the student class importing Ares' API.
	 */
	private static final String ARES_IMPORT_PATH = "test-classes/ancestorpackagefixture/aresimport/target/classes/de/tum/cit/ase";

	/**
	 * The analysed path of the probes for the student class importing Byte Buddy.
	 */
	private static final String BYTEBUDDY_IMPORT_PATH = "test-classes/ancestorpackagefixture/bytebuddyimport/target/classes/de/tum/cit/ase";

	/**
	 * The analysed path of the probes for the student class importing AspectJ.
	 */
	private static final String ASPECTJ_IMPORT_PATH = "test-classes/ancestorpackagefixture/aspectjimport/target/classes/de/tum/cit/ase";

	// <editor-fold desc="Probes: exercise without a forbidden import">
	/**
	 * Probe, ArchUnit and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = EXERCISE_PATH)
	void test_exerciseMavenArchunitAspectJ() {
	}

	/**
	 * Probe, ArchUnit and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = EXERCISE_PATH)
	void test_exerciseMavenArchunitInstrumentation() {
	}

	/**
	 * Probe, WALA and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = EXERCISE_PATH)
	void test_exerciseMavenWalaAspectJ() {
	}

	/**
	 * Probe, WALA and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = EXERCISE_PATH)
	void test_exerciseMavenWalaInstrumentation() {
	}
	// </editor-fold>

	// <editor-fold desc="Probes: exercise importing Ares' API">
	/**
	 * Probe, ArchUnit and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_aresImportMavenArchunitAspectJ() {
	}

	/**
	 * Probe, ArchUnit and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_aresImportMavenArchunitInstrumentation() {
	}

	/**
	 * Probe, WALA and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_aresImportMavenWalaAspectJ() {
	}

	/**
	 * Probe, WALA and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_aresImportMavenWalaInstrumentation() {
	}
	// </editor-fold>

	// <editor-fold desc="Probes: exercise importing Ares' API that the policy
	// names">
	/**
	 * Probe, ArchUnit and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_PERMITTED_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_permittedAresImportMavenArchunitAspectJ() {
	}

	/**
	 * Probe, ArchUnit and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_PERMITTED_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_permittedAresImportMavenArchunitInstrumentation() {
	}

	/**
	 * Probe, WALA and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_PERMITTED_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_permittedAresImportMavenWalaAspectJ() {
	}

	/**
	 * Probe, WALA and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_PERMITTED_POLICY, withinPath = ARES_IMPORT_PATH)
	void test_permittedAresImportMavenWalaInstrumentation() {
	}
	// </editor-fold>

	// <editor-fold desc="Probes: exercise importing Byte Buddy">
	/**
	 * Probe, ArchUnit and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = BYTEBUDDY_IMPORT_PATH)
	void test_byteBuddyImportMavenArchunitAspectJ() {
	}

	/**
	 * Probe, ArchUnit and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = BYTEBUDDY_IMPORT_PATH)
	void test_byteBuddyImportMavenArchunitInstrumentation() {
	}

	/**
	 * Probe, WALA and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = BYTEBUDDY_IMPORT_PATH)
	void test_byteBuddyImportMavenWalaAspectJ() {
	}

	/**
	 * Probe, WALA and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = BYTEBUDDY_IMPORT_PATH)
	void test_byteBuddyImportMavenWalaInstrumentation() {
	}
	// </editor-fold>

	// <editor-fold desc="Probes: exercise importing AspectJ">
	/**
	 * Probe, ArchUnit and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = ASPECTJ_IMPORT_PATH)
	void test_aspectJImportMavenArchunitAspectJ() {
	}

	/**
	 * Probe, ArchUnit and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = ASPECTJ_IMPORT_PATH)
	void test_aspectJImportMavenArchunitInstrumentation() {
	}

	/**
	 * Probe, WALA and AspectJ.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = ASPECTJ_IMPORT_PATH)
	void test_aspectJImportMavenWalaAspectJ() {
	}

	/**
	 * Probe, WALA and instrumentation.
	 */
	@Disabled(PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = ASPECTJ_IMPORT_PATH)
	void test_aspectJImportMavenWalaInstrumentation() {
	}
	// </editor-fold>

	// <editor-fold desc="Runners: exercise without a forbidden import">
	/**
	 * The exercise passes the import rule, ArchUnit and AspectJ.
	 */
	@Test
	void test_exerciseMavenArchunitAspectJ_test() {
		expectSuccess("test_exerciseMavenArchunitAspectJ", "exercise", AncestorPackageExercise.class);
	}

	/**
	 * The exercise passes the import rule, ArchUnit and instrumentation.
	 */
	@Test
	void test_exerciseMavenArchunitInstrumentation_test() {
		expectSuccess("test_exerciseMavenArchunitInstrumentation", "exercise", AncestorPackageExercise.class);
	}

	/**
	 * The exercise passes the import rule, WALA and AspectJ.
	 */
	@Test
	void test_exerciseMavenWalaAspectJ_test() {
		expectNoViolationInFixture("test_exerciseMavenWalaAspectJ", "exercise", AncestorPackageExercise.class);
	}

	/**
	 * The exercise passes the import rule, WALA and instrumentation.
	 */
	@Test
	void test_exerciseMavenWalaInstrumentation_test() {
		expectNoViolationInFixture("test_exerciseMavenWalaInstrumentation", "exercise", AncestorPackageExercise.class);
	}
	// </editor-fold>

	// <editor-fold desc="Runners: exercise importing Ares' API">
	/**
	 * The import of Ares' API is refused, ArchUnit and AspectJ.
	 */
	@Test
	void test_aresImportMavenArchunitAspectJ_test() {
		expectImportRefused("test_aresImportMavenArchunitAspectJ", "aresimport", AncestorPackageAresImport.class);
	}

	/**
	 * The import of Ares' API is refused, ArchUnit and instrumentation.
	 */
	@Test
	void test_aresImportMavenArchunitInstrumentation_test() {
		expectImportRefused("test_aresImportMavenArchunitInstrumentation", "aresimport",
				AncestorPackageAresImport.class);
	}

	/**
	 * The import of Ares' API is refused, WALA and AspectJ.
	 */
	@Test
	void test_aresImportMavenWalaAspectJ_test() {
		expectImportRefused("test_aresImportMavenWalaAspectJ", "aresimport", AncestorPackageAresImport.class);
	}

	/**
	 * The import of Ares' API is refused, WALA and instrumentation.
	 */
	@Test
	void test_aresImportMavenWalaInstrumentation_test() {
		expectImportRefused("test_aresImportMavenWalaInstrumentation", "aresimport", AncestorPackageAresImport.class);
	}
	// </editor-fold>

	// <editor-fold desc="Runners: exercise importing Ares' API that the policy
	// names">
	/**
	 * The named import of Ares' API is permitted, ArchUnit and AspectJ.
	 */
	@Test
	void test_permittedAresImportMavenArchunitAspectJ_test() {
		expectSuccess("test_permittedAresImportMavenArchunitAspectJ", "aresimport", AncestorPackageAresImport.class);
	}

	/**
	 * The named import of Ares' API is permitted, ArchUnit and instrumentation.
	 */
	@Test
	void test_permittedAresImportMavenArchunitInstrumentation_test() {
		expectSuccess("test_permittedAresImportMavenArchunitInstrumentation", "aresimport",
				AncestorPackageAresImport.class);
	}

	/**
	 * The named import of Ares' API is permitted, WALA and AspectJ.
	 */
	@Test
	void test_permittedAresImportMavenWalaAspectJ_test() {
		expectNoViolationInFixture("test_permittedAresImportMavenWalaAspectJ", "aresimport",
				AncestorPackageAresImport.class);
	}

	/**
	 * The named import of Ares' API is permitted, WALA and instrumentation.
	 */
	@Test
	void test_permittedAresImportMavenWalaInstrumentation_test() {
		expectNoViolationInFixture("test_permittedAresImportMavenWalaInstrumentation", "aresimport",
				AncestorPackageAresImport.class);
	}
	// </editor-fold>

	// <editor-fold desc="Runners: exercise importing Byte Buddy">
	/**
	 * The import of Byte Buddy is refused, ArchUnit and AspectJ.
	 */
	@Test
	void test_byteBuddyImportMavenArchunitAspectJ_test() {
		expectImportRefused("test_byteBuddyImportMavenArchunitAspectJ", "bytebuddyimport",
				AncestorPackageByteBuddyImport.class);
	}

	/**
	 * The import of Byte Buddy is refused, ArchUnit and instrumentation.
	 */
	@Test
	void test_byteBuddyImportMavenArchunitInstrumentation_test() {
		expectImportRefused("test_byteBuddyImportMavenArchunitInstrumentation", "bytebuddyimport",
				AncestorPackageByteBuddyImport.class);
	}

	/**
	 * The import of Byte Buddy is refused, WALA and AspectJ.
	 */
	@Test
	void test_byteBuddyImportMavenWalaAspectJ_test() {
		expectImportRefused("test_byteBuddyImportMavenWalaAspectJ", "bytebuddyimport",
				AncestorPackageByteBuddyImport.class);
	}

	/**
	 * The import of Byte Buddy is refused, WALA and instrumentation.
	 */
	@Test
	void test_byteBuddyImportMavenWalaInstrumentation_test() {
		expectImportRefused("test_byteBuddyImportMavenWalaInstrumentation", "bytebuddyimport",
				AncestorPackageByteBuddyImport.class);
	}
	// </editor-fold>

	// <editor-fold desc="Runners: exercise importing AspectJ">
	/**
	 * AspectJ stays importable because Ares itself needs it, ArchUnit and AspectJ.
	 */
	@Test
	void test_aspectJImportMavenArchunitAspectJ_test() {
		expectSuccess("test_aspectJImportMavenArchunitAspectJ", "aspectjimport", AncestorPackageAspectJImport.class);
	}

	/**
	 * AspectJ stays importable because Ares itself needs it, ArchUnit and
	 * instrumentation.
	 */
	@Test
	void test_aspectJImportMavenArchunitInstrumentation_test() {
		expectSuccess("test_aspectJImportMavenArchunitInstrumentation", "aspectjimport",
				AncestorPackageAspectJImport.class);
	}

	/**
	 * AspectJ stays importable because Ares itself needs it, WALA and AspectJ.
	 */
	@Test
	void test_aspectJImportMavenWalaAspectJ_test() {
		expectNoViolationInFixture("test_aspectJImportMavenWalaAspectJ", "aspectjimport",
				AncestorPackageAspectJImport.class);
	}

	/**
	 * AspectJ stays importable because Ares itself needs it, WALA and
	 * instrumentation.
	 */
	@Test
	void test_aspectJImportMavenWalaInstrumentation_test() {
		expectNoViolationInFixture("test_aspectJImportMavenWalaInstrumentation", "aspectjimport",
				AncestorPackageAspectJImport.class);
	}
	// </editor-fold>

	// <editor-fold desc="Helpers">
	/**
	 * Runs a probe over the given student class and expects it to pass.
	 *
	 * @param probe        the probe method
	 * @param fixture      the folder of the copied student class
	 * @param studentClass the student class
	 */
	private static void expectSuccess(String probe, String fixture, Class<?> studentClass) {
		Events events = runProbe(probe, fixture, studentClass);
		Assertions.assertTrue(failureOf(events).isEmpty(),
				() -> "The probe should pass, but failed with: " + failureOf(events).orElse(null));
		events.assertStatistics(stats -> stats.started(1).succeeded(1).failed(0).aborted(0).skipped(0));
	}

	/**
	 * Runs a probe and expects it to pass, or to fail only on an Ares test class.
	 * <p>
	 * WALA also loads the JVM's own class path, with Ares' test classes below
	 * {@code de.tum.cit.ase}, and its call-graph rules may refuse one of those.
	 *
	 * @param probe        the probe method
	 * @param fixture      the folder of the copied student class
	 * @param studentClass the student class
	 */
	private static void expectNoViolationInFixture(String probe, String fixture, Class<?> studentClass) {
		Events events = runProbe(probe, fixture, studentClass);
		events.assertStatistics(stats -> stats.started(1).aborted(0).skipped(0));
		failureOf(events).ifPresent(failure -> {
			String message = String.valueOf(failure.getMessage());
			Assertions.assertInstanceOf(SecurityException.class, failure, message);
			Assertions.assertTrue(UNRELATED_VIOLATION.matcher(message).find(),
					() -> "Not a violation by an Ares test class:\n" + message);
			Assertions.assertFalse(message.contains(studentClass.getName()),
					() -> "The student class should not be refused, but was:\n" + message);
		});
	}

	/**
	 * The throwable a probe failed with, if it failed.
	 *
	 * @param events the events of the probe
	 * @return the throwable, or empty
	 */
	private static java.util.Optional<Throwable> failureOf(Events events) {
		return events.failed().stream().map(Event::getPayload).flatMap(java.util.Optional::stream)
				.map(org.junit.platform.engine.TestExecutionResult.class::cast)
				.map(org.junit.platform.engine.TestExecutionResult::getThrowable).flatMap(java.util.Optional::stream)
				.findFirst();
	}

	/**
	 * Runs a probe over the given student class and expects the import rule to
	 * refuse it, naming that class.
	 *
	 * @param probe        the probe method
	 * @param fixture      the folder of the copied student class
	 * @param studentClass the student class
	 */
	private static void expectImportRefused(String probe, String fixture, Class<?> studentClass) {
		Events events = runProbe(probe, fixture, studentClass);
		events.assertStatistics(stats -> stats.started(1).succeeded(0).failed(1).aborted(0).skipped(0));
		Throwable failure = failureOf(events).orElseThrow();
		Assertions.assertInstanceOf(SecurityException.class, failure);
		String message = failure.getMessage();
		String forbiddenPackage = forbiddenPackageOf(studentClass);
		Assertions.assertTrue(message.contains(forbiddenPackage),
				() -> "The refusal should name " + forbiddenPackage + ", but was:\n" + message);
		Assertions.assertTrue(message.contains("illegally import the forbidden package")
				|| message.contains("illegal die verbotenen Pakete") || message.contains("Importiert verbotene Pakete"),
				() -> "The refusal should come from the import rule, but was:\n" + message);
	}

	/**
	 * The package a student class imports that the import rule must refuse.
	 *
	 * @param studentClass the student class
	 * @return the name of that package
	 */
	private static String forbiddenPackageOf(Class<?> studentClass) {
		if (studentClass == AncestorPackageAresImport.class) {
			return PackagePermission.class.getPackageName();
		}
		return ByteBuddy.class.getPackageName();
	}

	/**
	 * Copies the student class into its own output folder, runs the probe and
	 * deletes the folder again.
	 *
	 * @param probe        the probe method
	 * @param fixture      the folder of the copied student class
	 * @param studentClass the student class
	 * @return the events of the probe
	 */
	private static Events runProbe(String probe, String fixture, Class<?> studentClass) {
		Path root = testOutput().resolve(FIXTURE_ROOT).resolve(fixture);
		try {
			copyClass(studentClass, root.resolve("target/classes/de/tum/cit/ase"));
			return EngineTestKit.engine("junit-jupiter")
					.configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
					.selectors(selectMethod(AncestorPackageImportTest.class, probe)).execute().testEvents();
		} finally {
			deleteRecursively(root);
		}
	}

	/**
	 * The test output folder this class was loaded from.
	 *
	 * @return the test output folder
	 */
	private static Path testOutput() {
		try {
			return Path.of(AncestorPackageImportTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * Copies the bytecode of a class into a folder.
	 *
	 * @param studentClass the class to copy
	 * @param folder       the target folder
	 */
	private static void copyClass(Class<?> studentClass, Path folder) {
		String fileName = studentClass.getSimpleName() + ".class";
		try (InputStream bytecode = studentClass.getResourceAsStream(fileName)) {
			Files.createDirectories(folder);
			Files.copy(Assertions.assertInstanceOf(InputStream.class, bytecode), folder.resolve(fileName));
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Deletes a folder and everything in it, if it exists.
	 *
	 * @param folder the folder to delete
	 */
	private static void deleteRecursively(Path folder) {
		if (!Files.exists(folder)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(folder)) {
			List<Path> deepestFirst = paths.sorted(Comparator.reverseOrder()).toList();
			for (Path path : deepestFirst) {
				Files.delete(path);
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
	// </editor-fold>
}
