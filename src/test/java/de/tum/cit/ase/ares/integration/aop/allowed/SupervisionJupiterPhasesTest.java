package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle.ClosingReader;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle.LingeringReader;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle.ProviderReader;

/**
 * Student code that JUnit runs on its own thread outside a test method must run
 * under the policy too: arguments a lazy source builds between invocations,
 * elements a test factory builds after it returned, arguments JUnit closes
 * after an invocation, the source of a parameterised class and the static
 * initialiser of a test class. Each case runs a fixture through the JUnit
 * engine and expects the allowed read to work and the forbidden one to be
 * refused.
 */
class SupervisionJupiterPhasesTest {

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
	/** The readers the closing fixture handed to JUnit, in the order built. */
	private static final List<ClosingReader> CLOSING_READERS = new CopyOnWriteArrayList<>();

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
	 * Says what reading a file should give under the policy.
	 *
	 * @param file the file
	 * @return {@code read} for the allowed file, {@code refused} otherwise
	 */
	private static String expectedFor(String file) {
		return TRUSTED_FILE.equals(file) ? "read" : "refused";
	}

	/**
	 * The files the fixtures read, allowed first, so the forbidden one is built
	 * after the first invocation has ended.
	 *
	 * @return the files in the order they are read
	 */
	private static Stream<String> files() {
		return Stream.of(TRUSTED_FILE, FORBIDDEN_FILE, TRUSTED_FILE);
	}

	/**
	 * Runs a fixture and checks that all of its tests passed, naming what failed
	 * otherwise.
	 *
	 * @param fixture the fixture to run
	 * @param count   how many tests it has
	 */
	private static void assertAllTestsPass(Class<?> fixture, int count) {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture))
				.execute();
		List<String> failures = results.allEvents().failed().stream()
				.map(event -> event.getPayload(TestExecutionResult.class).flatMap(TestExecutionResult::getThrowable)
						.map(String::valueOf).orElse(event.getTestDescriptor().getDisplayName()))
				.toList();
		assertEquals(List.of(), failures);
		assertEquals(count, results.testEvents().succeeded().count());
	}

	/**
	 * Runs the closing fixture and checks that JUnit closed each argument under the
	 * policy.
	 *
	 * @param fixture the fixture to run
	 */
	private static void assertClosedUnderThePolicy(Class<?> fixture) {
		CLOSING_READERS.clear();
		assertAllTestsPass(fixture, 2);
		assertEquals(List.of("read", "refused"), CLOSING_READERS.stream().map(ClosingReader::outcome).toList());
	}

	/** Builds student objects lazily, one per invocation. */
	abstract static class LazySourceFixture {

		/**
		 * The argument source, built element by element as JUnit runs the invocations.
		 *
		 * @return a student object built from each file, with the expected outcome
		 */
		static Stream<Arguments> readers() {
			return files().map(file -> Arguments.of(new ProviderReader(file), expectedFor(file)));
		}

		/**
		 * The allowed reads succeeded and the forbidden one was refused.
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

	/** Builds student objects lazily, one per dynamic test. */
	abstract static class FactoryFixture {

		/**
		 * The factory, whose elements JUnit builds after it returned.
		 *
		 * @return a dynamic test per file
		 */
		@TestFactory
		@Public
		Stream<DynamicTest> readers() {
			return files().map(file -> {
				ProviderReader reader = new ProviderReader(file);
				return DynamicTest.dynamicTest(file, () -> assertEquals(expectedFor(file), reader.outcome()));
			});
		}
	}

	/** Hands JUnit student objects it closes after each invocation. */
	abstract static class ClosingFixture {

		/**
		 * The argument source, recording each object it builds.
		 *
		 * @return a student object per file, allowed first
		 */
		static Stream<Arguments> closers() {
			return Stream.of(TRUSTED_FILE, FORBIDDEN_FILE).map(ClosingReader::new).peek(CLOSING_READERS::add)
					.map(Arguments::of);
		}

		/**
		 * Nothing to check here; JUnit closes the argument afterwards.
		 *
		 * @param reader the student object
		 */
		@ParameterizedTest
		@MethodSource("closers")
		@Public
		void argumentIsClosedAfterwards(ClosingReader reader) {
			assertEquals("not closed", reader.outcome());
		}
	}

	/**
	 * A parameterised class whose first run leaves a student thread waiting, which
	 * its argument source releases before the second run.
	 */
	abstract static class LeftoverFixture {

		/** What the leftover thread's read gave between the two runs. */
		static volatile String outcomeBetweenRuns = "not released";

		/** The number of this run of the class, from one. */
		@Parameter(0)
		int run;

		/**
		 * The argument source, which releases the leftover thread before it builds the
		 * second run.
		 *
		 * @return the run numbers
		 */
		static Stream<Arguments> runs() {
			return Stream.of(1, 2).map(run -> {
				if (run == 2) {
					releaseLeftoverThread();
				}
				return Arguments.of(run);
			});
		}

		/** Lets the leftover thread read and waits until it has. */
		private static void releaseLeftoverThread() {
			LingeringReader.release();
			try {
				LingeringReader.awaitFinished();
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
			outcomeBetweenRuns = LingeringReader.outcome();
		}

		/**
		 * The first run reads under its policy and leaves a thread waiting; the second
		 * checks that the thread was refused between the runs.
		 */
		void checkRun() {
			if (run == 1) {
				assertEquals("read", new ProviderReader(TRUSTED_FILE).outcome());
				LingeringReader.startWaiting();
			} else {
				assertTrue(outcomeBetweenRuns.startsWith("refused: "), outcomeBetweenRuns);
			}
		}
	}

	/** Builds student objects lazily, one per run of the class. */
	abstract static class ParameterizedClassFixture {

		/** The student object of this run of the class. */
		@Parameter(0)
		ProviderReader reader;

		/** Its expected outcome. */
		@Parameter(1)
		String expected;

		/**
		 * The argument source of the class.
		 *
		 * @return a student object built from each file, with the expected outcome
		 */
		static Stream<Arguments> readers() {
			return files().map(file -> Arguments.of(new ProviderReader(file), expectedFor(file)));
		}

		/** The allowed reads succeeded and the forbidden one was refused. */
		@PublicTest
		void argumentWasBuiltUnderThePolicy() {
			assertEquals(expected, reader.outcome());
		}
	}

	/** The lazy argument case under ArchunitAspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class LazySourceArchunitAspectJ extends LazySourceFixture {
	}

	/** The test factory case under ArchunitAspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class FactoryArchunitAspectJ extends FactoryFixture {
	}

	/** The closed argument case under ArchunitAspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class ClosingArchunitAspectJ extends ClosingFixture {
	}

	/** The parameterised class case under ArchunitAspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	@Public
	@ParameterizedClass
	@MethodSource("readers")
	static class ParameterizedClassArchunitAspectJ extends ParameterizedClassFixture {
	}

	/**
	 * The leftover thread case under ArchunitAspectJ, with a policy on the method
	 * only.
	 */
	@Public
	@ParameterizedClass
	@MethodSource("runs")
	static class LeftoverArchunitAspectJ extends LeftoverFixture {

		/** Checks the run of the class it is given. */
		@PublicTest
		@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
		void leftoverThreadIsRefusedBetweenRuns() {
			checkRun();
		}
	}

	/** The static initialiser case under ArchunitAspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	@Public
	static class StaticInitialiserArchunitAspectJ {

		/** A folder JUnit sets before any test, which initialises this class. */
		@TempDir
		static Path scratch;

		/** What reading the allowed file gave while this class was initialised. */
		static final String TRUSTED_OUTCOME = new ProviderReader(TRUSTED_FILE).outcome();

		/** What reading the forbidden file gave while this class was initialised. */
		static final String FORBIDDEN_OUTCOME = new ProviderReader(FORBIDDEN_FILE).outcome();

		/** The allowed read succeeded and the forbidden one was refused. */
		@PublicTest
		void initialiserRanUnderThePolicy() {
			assertEquals("read", TRUSTED_OUTCOME);
			assertEquals("refused", FORBIDDEN_OUTCOME);
		}
	}

	/** The lazy argument case under ArchunitInstrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class LazySourceArchunitInstrumentation extends LazySourceFixture {
	}

	/** The test factory case under ArchunitInstrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class FactoryArchunitInstrumentation extends FactoryFixture {
	}

	/** The closed argument case under ArchunitInstrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class ClosingArchunitInstrumentation extends ClosingFixture {
	}

	/** The parameterised class case under ArchunitInstrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	@Public
	@ParameterizedClass
	@MethodSource("readers")
	static class ParameterizedClassArchunitInstrumentation extends ParameterizedClassFixture {
	}

	/**
	 * The leftover thread case under ArchunitInstrumentation, with a policy on the
	 * method only.
	 */
	@Public
	@ParameterizedClass
	@MethodSource("runs")
	static class LeftoverArchunitInstrumentation extends LeftoverFixture {

		/** Checks the run of the class it is given. */
		@PublicTest
		@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
		void leftoverThreadIsRefusedBetweenRuns() {
			checkRun();
		}
	}

	/** The static initialiser case under ArchunitInstrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	@Public
	static class StaticInitialiserArchunitInstrumentation {

		/** A folder JUnit sets before any test, which initialises this class. */
		@TempDir
		static Path scratch;

		/** What reading the allowed file gave while this class was initialised. */
		static final String TRUSTED_OUTCOME = new ProviderReader(TRUSTED_FILE).outcome();

		/** What reading the forbidden file gave while this class was initialised. */
		static final String FORBIDDEN_OUTCOME = new ProviderReader(FORBIDDEN_FILE).outcome();

		/** The allowed read succeeded and the forbidden one was refused. */
		@PublicTest
		void initialiserRanUnderThePolicy() {
			assertEquals("read", TRUSTED_OUTCOME);
			assertEquals("refused", FORBIDDEN_OUTCOME);
		}
	}

	/** The lazy argument case under WalaAspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class LazySourceWalaAspectJ extends LazySourceFixture {
	}

	/** The test factory case under WalaAspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class FactoryWalaAspectJ extends FactoryFixture {
	}

	/** The closed argument case under WalaAspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class ClosingWalaAspectJ extends ClosingFixture {
	}

	/** The parameterised class case under WalaAspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	@Public
	@ParameterizedClass
	@MethodSource("readers")
	static class ParameterizedClassWalaAspectJ extends ParameterizedClassFixture {
	}

	/**
	 * The leftover thread case under WalaAspectJ, with a policy on the method only.
	 */
	@Public
	@ParameterizedClass
	@MethodSource("runs")
	static class LeftoverWalaAspectJ extends LeftoverFixture {

		/** Checks the run of the class it is given. */
		@PublicTest
		@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
		void leftoverThreadIsRefusedBetweenRuns() {
			checkRun();
		}
	}

	/** The static initialiser case under WalaAspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	@Public
	static class StaticInitialiserWalaAspectJ {

		/** A folder JUnit sets before any test, which initialises this class. */
		@TempDir
		static Path scratch;

		/** What reading the allowed file gave while this class was initialised. */
		static final String TRUSTED_OUTCOME = new ProviderReader(TRUSTED_FILE).outcome();

		/** What reading the forbidden file gave while this class was initialised. */
		static final String FORBIDDEN_OUTCOME = new ProviderReader(FORBIDDEN_FILE).outcome();

		/** The allowed read succeeded and the forbidden one was refused. */
		@PublicTest
		void initialiserRanUnderThePolicy() {
			assertEquals("read", TRUSTED_OUTCOME);
			assertEquals("refused", FORBIDDEN_OUTCOME);
		}
	}

	/** The lazy argument case under WalaInstrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class LazySourceWalaInstrumentation extends LazySourceFixture {
	}

	/** The test factory case under WalaInstrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class FactoryWalaInstrumentation extends FactoryFixture {
	}

	/** The closed argument case under WalaInstrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class ClosingWalaInstrumentation extends ClosingFixture {
	}

	/** The parameterised class case under WalaInstrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	@Public
	@ParameterizedClass
	@MethodSource("readers")
	static class ParameterizedClassWalaInstrumentation extends ParameterizedClassFixture {
	}

	/**
	 * The leftover thread case under WalaInstrumentation, with a policy on the
	 * method only.
	 */
	@Public
	@ParameterizedClass
	@MethodSource("runs")
	static class LeftoverWalaInstrumentation extends LeftoverFixture {

		/** Checks the run of the class it is given. */
		@PublicTest
		@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
		void leftoverThreadIsRefusedBetweenRuns() {
			checkRun();
		}
	}

	/** The static initialiser case under WalaInstrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	@Public
	static class StaticInitialiserWalaInstrumentation {

		/** A folder JUnit sets before any test, which initialises this class. */
		@TempDir
		static Path scratch;

		/** What reading the allowed file gave while this class was initialised. */
		static final String TRUSTED_OUTCOME = new ProviderReader(TRUSTED_FILE).outcome();

		/** What reading the forbidden file gave while this class was initialised. */
		static final String FORBIDDEN_OUTCOME = new ProviderReader(FORBIDDEN_FILE).outcome();

		/** The allowed read succeeded and the forbidden one was refused. */
		@PublicTest
		void initialiserRanUnderThePolicy() {
			assertEquals("read", TRUSTED_OUTCOME);
			assertEquals("refused", FORBIDDEN_OUTCOME);
		}
	}

	/**
	 * Arguments a lazy source builds after the first invocation are supervised
	 * under ArchunitAspectJ.
	 */
	@Test
	void lazyArgumentsAreSupervisedMavenArchunitAspectJ() {
		assertAllTestsPass(LazySourceArchunitAspectJ.class, 3);
	}

	/**
	 * Elements a test factory builds after it returned are supervised under
	 * ArchunitAspectJ.
	 */
	@Test
	void factoryElementsAreSupervisedMavenArchunitAspectJ() {
		assertAllTestsPass(FactoryArchunitAspectJ.class, 3);
	}

	/**
	 * Arguments JUnit closes after an invocation are supervised under
	 * ArchunitAspectJ.
	 */
	@Test
	void closedArgumentsAreSupervisedMavenArchunitAspectJ() {
		assertClosedUnderThePolicy(ClosingArchunitAspectJ.class);
	}

	/**
	 * The argument source of a parameterised class is supervised under
	 * ArchunitAspectJ.
	 */
	@Test
	void parameterizedClassSourceIsSupervisedMavenArchunitAspectJ() {
		assertAllTestsPass(ParameterizedClassArchunitAspectJ.class, 3);
	}

	/**
	 * A thread the first run of a parameterised class without a class policy left
	 * running is refused between its runs under ArchunitAspectJ.
	 */
	@Test
	void leftoverThreadIsRefusedBetweenClassRunsMavenArchunitAspectJ() {
		assertAllTestsPass(LeftoverArchunitAspectJ.class, 2);
	}

	/**
	 * The static initialiser of a test class is supervised under ArchunitAspectJ.
	 */
	@Test
	void staticInitialiserIsSupervisedMavenArchunitAspectJ() {
		assertAllTestsPass(StaticInitialiserArchunitAspectJ.class, 1);
	}

	/**
	 * Arguments a lazy source builds after the first invocation are supervised
	 * under ArchunitInstrumentation.
	 */
	@Test
	void lazyArgumentsAreSupervisedMavenArchunitInstrumentation() {
		assertAllTestsPass(LazySourceArchunitInstrumentation.class, 3);
	}

	/**
	 * Elements a test factory builds after it returned are supervised under
	 * ArchunitInstrumentation.
	 */
	@Test
	void factoryElementsAreSupervisedMavenArchunitInstrumentation() {
		assertAllTestsPass(FactoryArchunitInstrumentation.class, 3);
	}

	/**
	 * Arguments JUnit closes after an invocation are supervised under
	 * ArchunitInstrumentation.
	 */
	@Test
	void closedArgumentsAreSupervisedMavenArchunitInstrumentation() {
		assertClosedUnderThePolicy(ClosingArchunitInstrumentation.class);
	}

	/**
	 * The argument source of a parameterised class is supervised under
	 * ArchunitInstrumentation.
	 */
	@Test
	void parameterizedClassSourceIsSupervisedMavenArchunitInstrumentation() {
		assertAllTestsPass(ParameterizedClassArchunitInstrumentation.class, 3);
	}

	/**
	 * A thread the first run of a parameterised class without a class policy left
	 * running is refused between its runs under ArchunitInstrumentation.
	 */
	@Test
	void leftoverThreadIsRefusedBetweenClassRunsMavenArchunitInstrumentation() {
		assertAllTestsPass(LeftoverArchunitInstrumentation.class, 2);
	}

	/**
	 * The static initialiser of a test class is supervised under
	 * ArchunitInstrumentation.
	 */
	@Test
	void staticInitialiserIsSupervisedMavenArchunitInstrumentation() {
		assertAllTestsPass(StaticInitialiserArchunitInstrumentation.class, 1);
	}

	/**
	 * Arguments a lazy source builds after the first invocation are supervised
	 * under WalaAspectJ.
	 */
	@Test
	void lazyArgumentsAreSupervisedMavenWalaAspectJ() {
		assertAllTestsPass(LazySourceWalaAspectJ.class, 3);
	}

	/**
	 * Elements a test factory builds after it returned are supervised under
	 * WalaAspectJ.
	 */
	@Test
	void factoryElementsAreSupervisedMavenWalaAspectJ() {
		assertAllTestsPass(FactoryWalaAspectJ.class, 3);
	}

	/**
	 * Arguments JUnit closes after an invocation are supervised under WalaAspectJ.
	 */
	@Test
	void closedArgumentsAreSupervisedMavenWalaAspectJ() {
		assertClosedUnderThePolicy(ClosingWalaAspectJ.class);
	}

	/**
	 * The argument source of a parameterised class is supervised under WalaAspectJ.
	 */
	@Test
	void parameterizedClassSourceIsSupervisedMavenWalaAspectJ() {
		assertAllTestsPass(ParameterizedClassWalaAspectJ.class, 3);
	}

	/**
	 * A thread the first run of a parameterised class without a class policy left
	 * running is refused between its runs under WalaAspectJ.
	 */
	@Test
	void leftoverThreadIsRefusedBetweenClassRunsMavenWalaAspectJ() {
		assertAllTestsPass(LeftoverWalaAspectJ.class, 2);
	}

	/**
	 * The static initialiser of a test class is supervised under WalaAspectJ.
	 */
	@Test
	void staticInitialiserIsSupervisedMavenWalaAspectJ() {
		assertAllTestsPass(StaticInitialiserWalaAspectJ.class, 1);
	}

	/**
	 * Arguments a lazy source builds after the first invocation are supervised
	 * under WalaInstrumentation.
	 */
	@Test
	void lazyArgumentsAreSupervisedMavenWalaInstrumentation() {
		assertAllTestsPass(LazySourceWalaInstrumentation.class, 3);
	}

	/**
	 * Elements a test factory builds after it returned are supervised under
	 * WalaInstrumentation.
	 */
	@Test
	void factoryElementsAreSupervisedMavenWalaInstrumentation() {
		assertAllTestsPass(FactoryWalaInstrumentation.class, 3);
	}

	/**
	 * Arguments JUnit closes after an invocation are supervised under
	 * WalaInstrumentation.
	 */
	@Test
	void closedArgumentsAreSupervisedMavenWalaInstrumentation() {
		assertClosedUnderThePolicy(ClosingWalaInstrumentation.class);
	}

	/**
	 * The argument source of a parameterised class is supervised under
	 * WalaInstrumentation.
	 */
	@Test
	void parameterizedClassSourceIsSupervisedMavenWalaInstrumentation() {
		assertAllTestsPass(ParameterizedClassWalaInstrumentation.class, 3);
	}

	/**
	 * A thread the first run of a parameterised class without a class policy left
	 * running is refused between its runs under WalaInstrumentation.
	 */
	@Test
	void leftoverThreadIsRefusedBetweenClassRunsMavenWalaInstrumentation() {
		assertAllTestsPass(LeftoverWalaInstrumentation.class, 2);
	}

	/**
	 * The static initialiser of a test class is supervised under
	 * WalaInstrumentation.
	 */
	@Test
	void staticInitialiserIsSupervisedMavenWalaInstrumentation() {
		assertAllTestsPass(StaticInitialiserWalaInstrumentation.class, 1);
	}
}
