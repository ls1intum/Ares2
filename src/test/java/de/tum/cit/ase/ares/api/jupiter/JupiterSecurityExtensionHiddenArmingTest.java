package de.tum.cit.ase.ares.api.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;

/**
 * Checks that an {@link Error} while the guard is armed for a hidden test's
 * constructor is reported like any other hidden failure: failed, with no text.
 */
class JupiterSecurityExtensionHiddenArmingTest {

	/** Whether the fixture's constructor or test ran. */
	private static volatile boolean reached;

	/** Whether the fixture's arming threw its error. */
	private static volatile boolean armingThrew;

	/** The agent's failure of an earlier installation, restored afterwards. */
	private Object savedFailure;

	/** Cannot arm the guard: arming fails with an error carrying private text. */
	public static class ErrorWhileArming extends JupiterSecurityExtension {

		/**
		 * Throws the error for every test method.
		 *
		 * @param context the context being armed.
		 */
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				armingThrew = true;
				throw new LinkageError("SECRET_ARMING_ERROR");
			}
		}
	}

	/** A hidden test past its deadline whose guard cannot be armed. */
	@ExtendWith(ErrorWhileArming.class)
	@Policy(activated = false)
	static class HiddenFixture {

		/** Records that the constructor ran. */
		HiddenFixture() {
			reached = true;
		}

		/** Records that the test ran. */
		@HiddenTest
		@Deadline("2000-01-01 00:00 UTC")
		void hidden() {
			reached = true;
		}
	}

	/**
	 * Clears the agent's installation failure for the run.
	 *
	 * @throws ReflectiveOperationException if the field cannot be read.
	 */
	@BeforeEach
	void clearFailure() throws ReflectiveOperationException {
		savedFailure = activationFailure().get(null);
		activationFailure().set(null, null);
		reached = false;
		armingThrew = false;
	}

	/**
	 * Restores the agent's installation failure.
	 *
	 * @throws ReflectiveOperationException if the field cannot be written.
	 */
	@AfterEach
	void restoreFailure() throws ReflectiveOperationException {
		activationFailure().set(null, savedFailure);
	}

	/** The error fails the hidden test without its text, and nothing ran. */
	@Test
	void anErrorWhileArmingAHiddenTestShowsNothing() {
		Events events = EngineTestKit.engine("junit-jupiter").selectors(selectClass(HiddenFixture.class)).execute()
				.testEvents();
		assertThat(events.failed().count()).isEqualTo(1);
		Throwable reported = events.failed().stream().findFirst().orElseThrow().getPayload(TestExecutionResult.class)
				.orElseThrow().getThrowable().orElseThrow();
		assertThat(armingThrew).isTrue();
		assertThat(reported.getClass().getName()).isEqualTo("de.tum.cit.ase.ares.api.internal.HiddenTestFailure");
		assertThat(String.valueOf(reported)).doesNotContain("SECRET_ARMING_ERROR");
		assertThat(reported.getCause()).isNull();
		assertThat(reached).isFalse();
	}

	/**
	 * Opens the agent's record of a failed installation.
	 *
	 * @return the accessible field.
	 * @throws NoSuchFieldException if the agent has no such field.
	 */
	private static Field activationFailure() throws NoSuchFieldException {
		Field field = JavaInstrumentationAgent.class.getDeclaredField("activationFailure");
		field.setAccessible(true);
		return field;
	}
}
