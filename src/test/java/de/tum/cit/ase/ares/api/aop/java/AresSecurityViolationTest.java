package de.tum.cit.ase.ares.api.aop.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks both runtime-check copies of Ares's blocked-action exception: their
 * factory, called directly or reflectively, gives code outside the copy's own
 * checking package only a plain exception, and no constructor is public.
 * Forcing a private constructor open with {@code setAccessible} is not covered.
 */
class AresSecurityViolationTest {

	/** The message a forged violation carries. */
	private static final String MESSAGE = "Ares Security Error (Reason: Student-Code; Stage: Execution): forged";

	/**
	 * A caller outside the copy's package receives a plain exception with the same
	 * message.
	 *
	 * @param type the copy.
	 * @throws ReflectiveOperationException if the factory cannot be found.
	 */
	@ParameterizedTest
	@ValueSource(classes = { de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation.class,
			de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation.class })
	void aForeignCallerReceivesAPlainException(Class<?> type) throws ReflectiveOperationException {
		Throwable created = (Throwable) factory(type).invoke(null, "file", "read", "/etc/passwd", MESSAGE);
		assertThat(created).isExactlyInstanceOf(SecurityException.class).hasMessage(MESSAGE);
	}

	/**
	 * No copy has a constructor other code could call.
	 *
	 * @param type the copy.
	 */
	@ParameterizedTest
	@ValueSource(classes = { de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation.class,
			de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation.class })
	void noCopyHasAnOpenConstructor(Class<?> type) {
		assertThat(Arrays.stream(type.getDeclaredConstructors()).map(c -> c.getModifiers()))
				.allMatch(Modifier::isPrivate);
	}

	/**
	 * Returns a copy's public factory.
	 *
	 * @param type the copy.
	 * @return the factory method.
	 * @throws NoSuchMethodException if it is missing.
	 */
	private static Method factory(Class<?> type) throws NoSuchMethodException {
		return type.getMethod("create", String.class, Object.class, Object.class, String.class);
	}
}
