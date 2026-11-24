package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle.LingeringReader;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle.ProviderReader;

/**
 * Student code must stay supervised outside the test method too: a thread a
 * test leaves running is refused once that test has ended, and the arguments of
 * a parameterised test are built under its policy. Each case runs a fixture
 * through the JUnit engine, so this class can act after the fixture's tests.
 */
class SupervisionLifecycleTest {

	/** The package of the student classes. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/lifecycle";
	/** The folder that holds the policies of all four mode combinations. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";
	/** The policy file name, the same in every mode folder. */
	private static final String POLICY = "/PolicySupervisionLifecycle.yaml";
	/** The file the policy allows to be read. */
	private static final String TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";
	/** A file the policy does not allow to be read. */
	private static final String FORBIDDEN_FILE = "pom.xml";
	/** How long a fixture waits for its own thread, in nanoseconds. */
	private static final long WAIT_LIMIT_NANOS = 60_000_000_000L;

	/**
	 * Restores the read fixture, which an earlier allowed delete test may remove.
	 *
	 * @throws IOException if it cannot be created
	 */
	@BeforeAll
	static void restoreReadFixture() throws IOException {
		Path trusted = Path.of(TRUSTED_FILE);
		if (Files.notExists(trusted)) {
			Files.createFile(trusted);
		}
	}

	/**
	 * Runs a fixture and checks that both of its tests passed, naming what failed
	 * otherwise.
	 *
	 * @param fixture the fixture to run
	 */
	private static void assertBothTestsPass(Class<?> fixture) {
		Events tests = EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture)).execute().testEvents();
		List<String> failures = tests.failed().stream()
				.map(event -> event.getPayload(TestExecutionResult.class).flatMap(TestExecutionResult::getThrowable)
						.map(String::valueOf).orElse(event.getTestDescriptor().getDisplayName()))
				.toList();
		assertEquals(List.of(), failures);
		tests.assertStatistics(statistics -> statistics.started(2).succeeded(2));
	}

	/**
	 * Its first test lets a thread read while the test runs, which the policy
	 * allows; its second test ends while a thread still waits to read.
	 */
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	abstract static class LingeringFixture {

		/** The positive control: during its test, the thread may read. */
		@PublicTest
		@Order(1)
		void threadReadsWhileItsTestRuns() {
			LingeringReader.startWaiting();
			LingeringReader.release();
			long deadline = System.nanoTime() + WAIT_LIMIT_NANOS;
			while ("not run".equals(LingeringReader.outcome()) && System.nanoTime() < deadline) {
				Math.sqrt(deadline);
			}
			assertEquals("read", LingeringReader.outcome());
		}

		/** Leaves a thread waiting when the test ends. */
		@PublicTest
		@Order(2)
		void testEndsWhileItsThreadStillWaits() {
			LingeringReader.startWaiting();
		}
	}

	/** Builds student objects in the argument source of a parameterised test. */
	abstract static class ProviderFixture {

		/**
		 * The argument source, which JUnit calls before any test method runs.
		 *
		 * @return a student object built from each file, with the expected outcome
		 */
		static Stream<Arguments> readers() {
			return Stream.of(Arguments.of(new ProviderReader(TRUSTED_FILE), "read"),
					Arguments.of(new ProviderReader(FORBIDDEN_FILE), "refused"));
		}

		/**
		 * The allowed read succeeded and the forbidden one was refused.
		 *
		 * @param reader   the student object
		 * @param expected its expected outcome
		 */
		@ParameterizedTest
		@MethodSource("readers")
		@Public
		void argumentWasBuiltUnderThePolicy(ProviderReader reader, String expected) {
			assertEquals(expected, reader.outcome());
		}
	}

	/** The lingering case under ArchUnit and AspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class LingeringArchunitAspectJ extends LingeringFixture {
	}

	/** The lingering case under ArchUnit and instrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class LingeringArchunitInstrumentation extends LingeringFixture {
	}

	/** The lingering case under WALA and AspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class LingeringWalaAspectJ extends LingeringFixture {
	}

	/** The lingering case under WALA and instrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class LingeringWalaInstrumentation extends LingeringFixture {
	}

	/** The argument case under ArchUnit and AspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class ProviderArchunitAspectJ extends ProviderFixture {
	}

	/** The argument case under ArchUnit and instrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class ProviderArchunitInstrumentation extends ProviderFixture {
	}

	/** The argument case under WALA and AspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class ProviderWalaAspectJ extends ProviderFixture {
	}

	/** The argument case under WALA and instrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class ProviderWalaInstrumentation extends ProviderFixture {
	}

	/**
	 * Runs a lingering fixture, then releases the thread its last test left waiting
	 * and checks that the read the policy allowed during the test is now refused.
	 *
	 * @param fixture the fixture to run
	 * @throws InterruptedException if waiting for the thread is interrupted
	 */
	private static void assertLingeringThreadIsRefused(Class<?> fixture) throws InterruptedException {
		assertBothTestsPass(fixture);
		LingeringReader.release();
		LingeringReader.awaitFinished();
		String outcome = LingeringReader.outcome();
		assertTrue(outcome.startsWith("refused: ") && outcome.contains("trusted.txt"), outcome);
	}

	/** A thread left running is refused under ArchUnit and AspectJ. */
	@Test
	void lingeringThreadIsRefusedMavenArchunitAspectJ() throws InterruptedException {
		assertLingeringThreadIsRefused(LingeringArchunitAspectJ.class);
	}

	/** A thread left running is refused under ArchUnit and instrumentation. */
	@Test
	void lingeringThreadIsRefusedMavenArchunitInstrumentation() throws InterruptedException {
		assertLingeringThreadIsRefused(LingeringArchunitInstrumentation.class);
	}

	/** A thread left running is refused under WALA and AspectJ. */
	@Test
	void lingeringThreadIsRefusedMavenWalaAspectJ() throws InterruptedException {
		assertLingeringThreadIsRefused(LingeringWalaAspectJ.class);
	}

	/** A thread left running is refused under WALA and instrumentation. */
	@Test
	void lingeringThreadIsRefusedMavenWalaInstrumentation() throws InterruptedException {
		assertLingeringThreadIsRefused(LingeringWalaInstrumentation.class);
	}

	/** Argument sources are supervised under ArchUnit and AspectJ. */
	@Test
	void argumentSourceIsSupervisedMavenArchunitAspectJ() {
		assertBothTestsPass(ProviderArchunitAspectJ.class);
	}

	/** Argument sources are supervised under ArchUnit and instrumentation. */
	@Test
	void argumentSourceIsSupervisedMavenArchunitInstrumentation() {
		assertBothTestsPass(ProviderArchunitInstrumentation.class);
	}

	/** Argument sources are supervised under WALA and AspectJ. */
	@Test
	void argumentSourceIsSupervisedMavenWalaAspectJ() {
		assertBothTestsPass(ProviderWalaAspectJ.class);
	}

	/** Argument sources are supervised under WALA and instrumentation. */
	@Test
	void argumentSourceIsSupervisedMavenWalaInstrumentation() {
		assertBothTestsPass(ProviderWalaInstrumentation.class);
	}
}
