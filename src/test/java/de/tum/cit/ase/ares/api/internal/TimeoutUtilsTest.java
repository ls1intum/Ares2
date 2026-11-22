package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

class TimeoutUtilsTest {

	/**
	 * The pattern Artemis uses to replace a timeout feedback with its own
	 * explanation, copied from {@code ProgrammingExerciseFeedbackCreationService}.
	 */
	private static final Pattern ARTEMIS_TIMEOUT_PATTERN = Pattern.compile("^.*:(.*timed out after.*)"); //$NON-NLS-1$

	/**
	 * The English prefix a timeout carries: caused by the student code, while it
	 * runs.
	 */
	private static final String ENGLISH_PREFIX = "Ares Security Error (Reason: Student-Code; Stage: Execution): "; //$NON-NLS-1$

	/** The German prefix a timeout carries. */
	private static final String GERMAN_PREFIX = "Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausführung): "; //$NON-NLS-1$

	@Test
	void timedOutExecutionUsesOneCancellationInterrupt() {
		Future<?> future = mock(Future.class);
		ExecutorService executorService = mock(ExecutorService.class);
		when(executorService.isTerminated()).thenReturn(true);

		TimeoutUtils.terminateTimedOutExecution(future, executorService, Duration.ofSeconds(1),
				exitCode -> fail("Interruption-aware execution must not request fatal termination")); //$NON-NLS-1$

		InOrder cancellationOrder = inOrder(executorService, future);
		cancellationOrder.verify(executorService).shutdown();
		cancellationOrder.verify(future).cancel(true);
		verify(executorService, never()).shutdownNow();
	}

	@Test
	void interruptionAwareExecutionFinishesBeforeControlReturns() throws Exception {
		AtomicBoolean workerFinished = new AtomicBoolean();
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$

		assertThrows(SecurityException.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.onSpinWait();
				}
			} finally {
				workerFinished.set(true);
			}
			return null;
		}, context));

		assertThat(workerFinished).isTrue();
	}

	@Test
	void interruptionIgnoringExecutionInvalidatesTheCurrentFork() throws Exception {
		AtomicBoolean releaseWorker = new AtomicBoolean();
		AtomicInteger requestedExitCode = new AtomicInteger(-1);
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$
		try {
			FatalTermination termination = assertThrows(FatalTermination.class,
					() -> TimeoutUtils.performTimeoutExecution(() -> {
						while (!releaseWorker.get()) {
							Thread.onSpinWait();
						}
						return null;
					}, context, java.time.Duration.ofMillis(10), exitCode -> {
						requestedExitCode.set(exitCode);
						throw new FatalTermination();
					}));
			assertThat(termination).isNotNull();
		} finally {
			releaseWorker.set(true);
		}

		assertThat(requestedExitCode).hasValue(124);
	}

	/**
	 * A timeout is a plain {@link SecurityException} with the English prefix that
	 * blames the student code, and it names the exceeded time.
	 */
	@Test
	void timeoutIsAPrefixedSecurityExceptionBlamingTheStudentCode() throws Exception {
		SecurityException failure = timeoutIn(Locale.ENGLISH, contextFor("strictTimeoutTarget")); //$NON-NLS-1$

		assertThat(failure).hasNoCause();
		assertThat(failure.getClass()).isEqualTo(SecurityException.class);
		assertThat(failure.getMessage()).startsWith(ENGLISH_PREFIX).isEqualTo(AdviceActionWording
				.inLocale(Locale.ENGLISH, () -> Messages.localized("timeout.failure_message", "20 ms"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The English timeout still matches the pattern Artemis uses, and the part
	 * Artemis captures holds the words it looks for, after the prefix's colon.
	 */
	@Test
	void englishTimeoutStillMatchesTheArtemisTimeoutPattern() throws Exception {
		String message = timeoutIn(Locale.ENGLISH, contextFor("strictTimeoutTarget")).getMessage(); //$NON-NLS-1$

		Matcher matcher = ARTEMIS_TIMEOUT_PATTERN.matcher(message);
		assertThat(matcher.matches()).isTrue();
		assertThat(matcher.group(1)).contains("timed out after 20 ms").doesNotContain("Reason:"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The German timeout carries the German prefix with the same Reason and Stage.
	 */
	@Test
	void germanTimeoutCarriesTheGermanPrefix() throws Exception {
		String message = timeoutIn(Locale.GERMAN, contextFor("strictTimeoutTarget")).getMessage(); //$NON-NLS-1$

		assertThat(message).startsWith(GERMAN_PREFIX).contains("20 ms"); //$NON-NLS-1$
	}

	/**
	 * Under privileged-only reporting the timeout is still thrown wrapped as a
	 * privileged exception, as before, so the report shows it instead of the
	 * instructor text. Only the wrapped failure's type and text changed.
	 */
	@Test
	void privilegedOnlyTimeoutIsStillWrappedAsPrivileged() throws Exception {
		TestContext context = contextFor("privilegedStrictTimeoutTarget"); //$NON-NLS-1$

		PrivilegedException privileged = assertThrows(PrivilegedException.class,
				() -> TimeoutUtils.performTimeoutExecution(TimeoutUtilsTest::spinUntilInterrupted, context));

		assertThat(privileged.getPriviledgedThrowable()).isExactlyInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("timeout.failure_message", "20 ms")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	void productionTerminatorStopsAContaminatedChildJvm() throws Exception {
		String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")); //$NON-NLS-1$ //$NON-NLS-2$
		Process process = new ProcessBuilder(javaExecutable, "-cp", classPath, TimeoutUtilsForkProbe.class.getName()) //$NON-NLS-1$
				.redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
			assertThat(process.exitValue()).isEqualTo(124);
		} finally {
			process.destroyForcibly();
		}
	}

	private static final class FatalTermination extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Runs a busy loop past the strict timeout of the given context in the given
	 * locale and returns the timeout.
	 *
	 * @param locale  the locale the message is built in
	 * @param context the context naming the strict timeout
	 * @return the timeout failure
	 */
	private static SecurityException timeoutIn(Locale locale, TestContext context) {
		SecurityException[] failure = new SecurityException[1];
		AdviceActionWording.inLocale(locale, () -> {
			failure[0] = assertThrows(SecurityException.class,
					() -> TimeoutUtils.performTimeoutExecution(TimeoutUtilsTest::spinUntilInterrupted, context));
			return failure[0].getMessage();
		});
		return failure[0];
	}

	/**
	 * Waits busily until the thread is interrupted, as a test body that runs too
	 * long but still stops when asked.
	 *
	 * @return always {@code null}
	 */
	private static Object spinUntilInterrupted() {
		while (!Thread.currentThread().isInterrupted()) {
			Thread.onSpinWait();
		}
		return null;
	}

	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = TimeoutUtilsTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(TimeoutUtilsTest.class));
		return context;
	}

	// Reflective use only, through the mocked test context.
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutTarget() {
		// Provides the annotation consumed through the mocked test context.
	}

	/**
	 * Names a strict timeout and privileged-only reporting, read through the mocked
	 * test context.
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	@PrivilegedExceptionsOnly("instructor text")
	private static void privilegedStrictTimeoutTarget() {
		// Provides the annotations consumed through the mocked test context.
	}
}
