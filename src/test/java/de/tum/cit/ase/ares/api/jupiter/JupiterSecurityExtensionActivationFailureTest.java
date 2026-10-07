package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;

/**
 * Checks that a test instance is never built unguarded: when the guard cannot
 * be armed, neither its constructor nor its field initialisers run, and the
 * test fails with the cause.
 */
class JupiterSecurityExtensionActivationFailureTest {

	/**
	 * The phases the fixture reached.
	 */
	private static final List<String> OBSERVATIONS = new CopyOnWriteArrayList<>();

	/**
	 * The agent's failure of an earlier installation, saved and restored around
	 * each test.
	 */
	private Object savedFailure;

	/**
	 * Cannot arm the guard for a test method, as when its preparation throws.
	 */
	public static class FailingPreparationExtension extends JupiterSecurityExtension {
		/**
		 * Fails for every test method.
		 */
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				throw new SecurityException("deliberate preparation failure");
			}
		}
	}

	/**
	 * Records whether its constructor and its test ran.
	 */
	@ExtendWith(FailingPreparationExtension.class)
	static class ConstructorFixture {
		/**
		 * Records the constructor, as student code called from it would run.
		 */
		ConstructorFixture() {
			OBSERVATIONS.add("constructor");
		}

		/**
		 * Records the test.
		 */
		@Test
		void test() {
			OBSERVATIONS.add("test");
		}
	}

	/**
	 * Stands for student code with a side effect, created from a field initialiser.
	 */
	static class Leaky {
		/**
		 * Records the side effect.
		 */
		Leaky() {
			OBSERVATIONS.add("leak");
		}
	}

	/**
	 * Calls student code from a field initialiser, as a test class may.
	 */
	@ExtendWith(FailingPreparationExtension.class)
	static class FieldInitialiserFixture {
		/**
		 * Runs the student code when the instance is built.
		 */
		private final Leaky leaky = new Leaky();

		/**
		 * Records the test.
		 */
		@Test
		void test() {
			OBSERVATIONS.add("test " + leaky);
		}
	}

	/**
	 * Saves the agent's installation failure and clears the observations.
	 *
	 * @throws ReflectiveOperationException If the field cannot be read.
	 */
	@BeforeEach
	void saveFailure() throws ReflectiveOperationException {
		savedFailure = activationFailure().get(null);
		OBSERVATIONS.clear();
	}

	/**
	 * Restores the agent's installation failure.
	 *
	 * @throws ReflectiveOperationException If the field cannot be written.
	 */
	@AfterEach
	void restoreFailure() throws ReflectiveOperationException {
		activationFailure().set(null, savedFailure);
	}

	/**
	 * A failed installation stops the constructor from running.
	 *
	 * @throws ReflectiveOperationException If the field cannot be written.
	 */
	@Test
	void failedInstallationKeepsTheConstructorFromRunning() throws ReflectiveOperationException {
		activationFailure().set(null, new SecurityException("deliberate installation failure"));

		EngineTestKit.engine("junit-jupiter").selectors(selectClass(ConstructorFixture.class)).execute().testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of(), OBSERVATIONS);
	}

	/**
	 * Any other failure to arm the guard also stops the constructor from running
	 * and fails the test with that failure.
	 *
	 * @throws ReflectiveOperationException If the field cannot be written.
	 */
	@Test
	void otherPreparationFailureKeepsTheConstructorFromRunningAndFailsTheTest() throws ReflectiveOperationException {
		activationFailure().set(null, null);

		EngineTestKit.engine("junit-jupiter").selectors(selectClass(ConstructorFixture.class)).execute().testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1)).assertThatEvents()
				.haveExactly(1, event(finishedWithFailure(instanceOf(SecurityException.class),
						message("deliberate preparation failure"))));

		assertEquals(List.of(), OBSERVATIONS);
	}

	/**
	 * Student code called from a field initialiser does not run when the guard
	 * cannot be armed.
	 *
	 * @throws ReflectiveOperationException If the field cannot be written.
	 */
	@Test
	void failedPreparationKeepsStudentCodeInAFieldInitialiserFromRunning() throws ReflectiveOperationException {
		activationFailure().set(null, null);

		EngineTestKit.engine("junit-jupiter").selectors(selectClass(FieldInitialiserFixture.class)).execute()
				.testEvents().assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of(), OBSERVATIONS);
	}

	/**
	 * Opens the agent's record of a failed installation.
	 *
	 * @return The accessible field.
	 * @throws NoSuchFieldException If the agent has no such field.
	 */
	private static Field activationFailure() throws NoSuchFieldException {
		Field field = JavaInstrumentationAgent.class.getDeclaredField("activationFailure");
		field.setAccessible(true);
		return field;
	}
}
