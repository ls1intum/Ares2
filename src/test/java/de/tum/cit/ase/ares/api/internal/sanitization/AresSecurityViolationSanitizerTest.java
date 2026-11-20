package de.tum.cit.ase.ares.api.internal.sanitization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.util.UnexpectedExceptionError;
import de.tum.cit.ase.ares.testutilities.LookalikeViolation;

/**
 * Checks that a blocked action Ares's own checks raised reaches the report as a
 * plain {@link SecurityException}, and that a student exception with the same
 * name does not.
 */
class AresSecurityViolationSanitizerTest {

	/** The message of the blocked action. */
	private static final String MESSAGE = "Ares Security Error (Reason: Student-Code; Stage: Execution): blocked";

	/**
	 * Each AOP copy of the exception is reported as a plain one with its message
	 * and stack.
	 *
	 * @param type the copy.
	 * @throws ReflectiveOperationException if the copy cannot be created.
	 */
	@ParameterizedTest
	@ValueSource(classes = { de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation.class,
			de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation.class })
	void aViolationIsReportedAsAPlainSecurityException(Class<? extends SecurityException> type)
			throws ReflectiveOperationException {
		SecurityException violation = violation(type);
		Throwable reported = ThrowableSanitizer.sanitize(violation);
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(MESSAGE);
		assertThat(reported.getStackTrace()).containsExactly(violation.getStackTrace());
	}

	/**
	 * A student exception that only borrows the name is still reported as
	 * unexpected.
	 */
	@Test
	void aLookalikeIsNotTreatedAsAViolation() {
		Throwable reported = ThrowableSanitizer.sanitize(new LookalikeViolation.AresSecurityViolation(MESSAGE));
		assertThat(reported).isExactlyInstanceOf(UnexpectedExceptionError.class);
	}

	/**
	 * Creates a copy of the exception the way Ares's checks do.
	 *
	 * @param type the copy.
	 * @return the exception.
	 * @throws ReflectiveOperationException if the copy cannot be created.
	 */
	private static SecurityException violation(Class<? extends SecurityException> type)
			throws ReflectiveOperationException {
		Constructor<? extends SecurityException> constructor = type.getDeclaredConstructor(String.class, String.class,
				String.class, String.class);
		constructor.setAccessible(true);
		return constructor.newInstance("file", "read", "secret.txt", MESSAGE);
	}
}
