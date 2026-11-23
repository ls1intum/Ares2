package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks the shape of the failures {@link ReportingUtils} and
 * {@link TestGuardUtils} put into the report: the fixed texts for a hidden
 * test, an internal Ares fault and a hidden test before its deadline are
 * prefixed {@link SecurityException}s, while the student's own failures pass
 * through unchanged.
 */
class ReportingUtilsMessageShapeTest {

	/**
	 * Text the student code puts into its failure, which a hidden test must not
	 * show.
	 */
	private static final String STUDENT_SECRET = "STUDENT_SECRET"; //$NON-NLS-1$

	/** The instructor text shown under privileged-only reporting. */
	private static final String INSTRUCTOR_TEXT = "INSTRUCTOR_TEXT"; //$NON-NLS-1$

	/** A message as Ares writes it when its own settings class cannot be found. */
	private static final String INTERNAL_FAULT = "Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not find 'JavaAOPTestCaseSettings' class to access field 'x'"; //$NON-NLS-1$

	/** A message as Ares writes it when it blocks the student code. */
	private static final String DENIAL = "Ares Security Error (Reason: Student-Code; Stage: Execution): example.Student tried to illegally read the file secret.txt, but was blocked by Ares."; //$NON-NLS-1$

	/** Holds the methods the test contexts point at. */
	static class Fixtures {
		/** A test without deadline or reporting settings. */
		void plain() {
		}

		/** A hidden test whose deadline lies far in the future. */
		@Deadline("2999-01-01 00:00 UTC")
		void hiddenBeforeDeadline() {
		}

		/** A test that shows only privileged failures. */
		@PrivilegedExceptionsOnly(INSTRUCTOR_TEXT)
		void privilegedOnly() {
		}
	}

	/**
	 * A failed hidden test shows only its fixed text as a prefixed
	 * {@link SecurityException} that blames the student code, and keeps nothing of
	 * the original failure: no cause, no suppressed failure, no student text.
	 */
	@Test
	void hiddenFailureIsAPrefixedSecurityExceptionWithoutStudentDetail() throws Exception {
		IllegalStateException studentFailure = new IllegalStateException(STUDENT_SECRET,
				new RuntimeException(STUDENT_SECRET));
		studentFailure.addSuppressed(new IllegalArgumentException(STUDENT_SECRET));
		TestContext hidden = TestContextFixtures.of(Fixtures.class, "plain", TestType.HIDDEN); //$NON-NLS-1$

		Throwable english = inLocale(Locale.ENGLISH, () -> ReportingUtils.processThrowable(studentFailure, hidden));
		Throwable german = inLocale(Locale.GERMAN, () -> ReportingUtils.processThrowable(studentFailure, hidden));

		assertRedacted(english);
		assertThat(english.getMessage())
				.isEqualTo("Ares Security Error (Reason: Student-Code; Stage: Execution): Hidden test failed."); //$NON-NLS-1$
		assertRedacted(german);
		assertThat(german.getMessage()).startsWith("Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausführung): "); //$NON-NLS-1$
	}

	/**
	 * A hidden test that meets an internal Ares fault still shows only the hidden
	 * text, so the fault does not reveal why it failed.
	 */
	@Test
	void hiddenInternalFaultShowsOnlyTheHiddenText() throws Exception {
		TestContext hidden = TestContextFixtures.of(Fixtures.class, "plain", TestType.HIDDEN); //$NON-NLS-1$

		Throwable reported = ReportingUtils.processThrowable(new SecurityException(INTERNAL_FAULT), hidden);

		assertRedacted(reported);
		assertThat(reported.getMessage()).isEqualTo(Messages.localized("test_guard.hidden_test_failed")); //$NON-NLS-1$
	}

	/**
	 * An internal Ares fault in a public test shows the fixed text asking to
	 * contact the instructor, as a prefixed {@link SecurityException} that blames
	 * Ares while the test runs.
	 */
	@Test
	void internalFaultIsAPrefixedSecurityExceptionBlamingAres() throws Exception {
		TestContext visible = TestContextFixtures.of(Fixtures.class, "plain", TestType.PUBLIC); //$NON-NLS-1$

		Throwable english = inLocale(Locale.ENGLISH,
				() -> ReportingUtils.processThrowable(new SecurityException(INTERNAL_FAULT), visible));
		Throwable german = inLocale(Locale.GERMAN,
				() -> ReportingUtils.processThrowable(new SecurityException(INTERNAL_FAULT), visible));

		assertRedacted(english);
		assertThat(english.getMessage()).isEqualTo(
				"Ares Security Error (Reason: Ares-Code; Stage: Execution): An internal Ares error occurred during test execution. Please contact your instructor."); //$NON-NLS-1$
		assertRedacted(german);
		assertThat(german.getMessage()).startsWith("Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Ausführung): "); //$NON-NLS-1$
	}

	/**
	 * A hidden test run before its deadline is refused with a prefixed
	 * {@link SecurityException} that blames Ares while the test runs.
	 */
	@Test
	void hiddenTestBeforeItsDeadlineIsAPrefixedSecurityExceptionBlamingAres() throws Exception {
		TestContext hidden = TestContextFixtures.of(Fixtures.class, "hiddenBeforeDeadline", TestType.HIDDEN); //$NON-NLS-1$

		SecurityException english = inLocale(Locale.ENGLISH,
				() -> assertThrows(SecurityException.class, () -> TestGuardUtils.checkForHidden(hidden)));
		SecurityException german = inLocale(Locale.GERMAN,
				() -> assertThrows(SecurityException.class, () -> TestGuardUtils.checkForHidden(hidden)));

		assertRedacted(english);
		assertThat(english.getMessage()).isEqualTo(
				"Ares Security Error (Reason: Ares-Code; Stage: Execution): Hidden tests will be executed after the deadline."); //$NON-NLS-1$
		assertRedacted(german);
		assertThat(german.getMessage()).startsWith("Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Ausführung): "); //$NON-NLS-1$
	}

	/**
	 * A student's own exception in a public test passes through with its type and
	 * text, and gains no Ares prefix.
	 */
	@Test
	void studentExceptionPassesThroughUnchangedAndUnprefixed() throws Exception {
		TestContext visible = TestContextFixtures.of(Fixtures.class, "plain", TestType.PUBLIC); //$NON-NLS-1$

		Throwable reported = ReportingUtils.processThrowable(new IllegalStateException(STUDENT_SECRET), visible);

		assertThat(reported).isExactlyInstanceOf(IllegalStateException.class).hasMessage(STUDENT_SECRET);
		assertThat(reported.getMessage()).doesNotContain("Ares Security Error"); //$NON-NLS-1$
	}

	/**
	 * A prefixed {@link SecurityException}, processed twice, keeps its one prefix,
	 * and a newly prefixed Ares text that is not about the settings class is not
	 * mistaken for an internal fault.
	 */
	@Test
	void prefixedSecurityExceptionKeepsOnePrefixAndIsNoInternalFault() throws Exception {
		TestContext visible = TestContextFixtures.of(Fixtures.class, "plain", TestType.PUBLIC); //$NON-NLS-1$
		String beforeDeadline = Messages.localized("test_guard.hidden_test_before_deadline_message"); //$NON-NLS-1$

		Throwable once = ReportingUtils.processThrowable(new SecurityException(DENIAL), visible);
		Throwable twice = ReportingUtils.processThrowable(once, visible);
		Throwable aresText = ReportingUtils.processThrowable(new SecurityException(beforeDeadline), visible);

		assertThat(twice).isExactlyInstanceOf(SecurityException.class).hasMessage(DENIAL);
		assertThat(twice.getMessage().split("Ares Security Error", -1)).hasSize(2); //$NON-NLS-1$
		assertThat(aresText).hasMessage(beforeDeadline);
	}

	/**
	 * Under privileged-only reporting a prefixed Ares denial is replaced by the
	 * instructor text, as before this change.
	 */
	@Test
	void privilegedOnlyReplacesAPrefixedAresDenialWithTheInstructorText() throws Exception {
		TestContext privileged = TestContextFixtures.of(Fixtures.class, "privilegedOnly", TestType.PUBLIC); //$NON-NLS-1$

		Throwable reported = ReportingUtils.processThrowable(new SecurityException(DENIAL), privileged);

		assertThat(reported).isExactlyInstanceOf(AssertionError.class).hasMessage(INSTRUCTOR_TEXT);
	}

	/**
	 * Under privileged-only reporting a timeout, which arrives wrapped as
	 * privileged, is unwrapped and shown with its prefixed text, as before.
	 */
	@Test
	void privilegedOnlyStillShowsAWrappedTimeout() throws Exception {
		TestContext privileged = TestContextFixtures.of(Fixtures.class, "privilegedOnly", TestType.PUBLIC); //$NON-NLS-1$
		String timeout = Messages.localized("timeout.failure_message", "1 s"); //$NON-NLS-1$ //$NON-NLS-2$

		Throwable reported = ReportingUtils.processThrowable(new PrivilegedException(new SecurityException(timeout)),
				privileged);

		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasMessage(timeout);
	}

	/**
	 * Checks that a fixed Ares text is a plain {@link SecurityException} that
	 * carries nothing else: no cause, no suppressed failure and no student text.
	 *
	 * @param reported the failure to check
	 */
	private static void assertRedacted(Throwable reported) {
		assertThat(reported).isExactlyInstanceOf(SecurityException.class).hasNoCause();
		assertThat(reported.getSuppressed()).isEmpty();
		assertThat(reported.getMessage()).doesNotContain(STUDENT_SECRET);
	}

	/**
	 * Runs a step with the given locale as default and returns its result.
	 *
	 * @param <T>    the type of the result
	 * @param locale the locale to use
	 * @param step   the step to run
	 * @return the result of the step
	 */
	private static <T> T inLocale(Locale locale, Supplier<T> step) {
		Object[] result = new Object[1];
		AdviceActionWording.inLocale(locale, () -> {
			result[0] = step.get();
			return ""; //$NON-NLS-1$
		});
		@SuppressWarnings("unchecked")
		T typed = (T) result[0];
		return typed;
	}
}
