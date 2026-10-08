package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

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
 * Checks that a test instance is not built unguarded when the instrumentation
 * could not be installed, while any other failure to arm the guard still leaves
 * the constructor to run, as JUnit may skip the test afterwards.
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
	 * Any other failure to arm the guard still lets the constructor run and fails
	 * the test.
	 *
	 * @throws ReflectiveOperationException If the field cannot be written.
	 */
	@Test
	void otherPreparationFailureStillBuildsTheInstance() throws ReflectiveOperationException {
		activationFailure().set(null, null);

		EngineTestKit.engine("junit-jupiter").selectors(selectClass(ConstructorFixture.class)).execute().testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of("constructor"), OBSERVATIONS);
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
