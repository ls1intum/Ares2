package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;

class TimeoutUtilsTest {
	@Test
	void timedOutExecutionUsesOneCancellationInterrupt() {
		Future<?> future = mock(Future.class);
		ExecutorService executorService = mock(ExecutorService.class);
		when(executorService.isTerminated()).thenReturn(true);

		TimeoutUtils.terminateTimedOutExecution(future, executorService, Duration.ofSeconds(1).toNanos(),
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

		assertThrows(AssertionFailedError.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
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
	void interruptionAwareExecutionHasTimeForBoundedFrameworkCleanup() throws Exception {
		AtomicBoolean workerFinished = new AtomicBoolean();
		AtomicInteger requestedExitCode = new AtomicInteger(-1);
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$

		assertThrows(AssertionFailedError.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Thread.onSpinWait();
				}
			} finally {
				long cleanupDeadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(200);
				while (System.nanoTime() < cleanupDeadline) {
					Thread.onSpinWait();
				}
				workerFinished.set(true);
			}
			return null;
		}, context, Duration.ofSeconds(1), requestedExitCode::set));

		assertThat(workerFinished).isTrue();
		assertThat(requestedExitCode).hasValue(-1);
	}

	@Test
	void resolvesInstructorConfiguredTerminationGracePeriod() throws Exception {
		TestContext context = contextFor("strictTimeoutWithCustomTerminationGrace"); //$NON-NLS-1$

		Optional<Duration> terminationGracePeriod = TimeoutUtils.findTerminationGracePeriod(context);

		assertThat(terminationGracePeriod).contains(Duration.ofSeconds(10));
	}

	@Test
	void unconfiguredTerminationGraceResolvesToNothingSoTheCallerKeepsItsOwn() throws Exception {
		// The attribute defaults to a negative value rather than to 50 ms on purpose:
		// the Jupiter path allows 50 ms and the jqwik path a second, so an attribute
		// default would have silently imposed one of them on the other. Absent has to
		// stay distinguishable from configured for both to keep their own.
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$

		assertThat(TimeoutUtils.findTerminationGracePeriod(context)).isEmpty();
	}

	@Test
	void zeroTerminationGraceIsConfiguredRatherThanAbsent() throws Exception {
		// Zero means "terminate at once", which is a decision, not an omission.
		TestContext context = contextFor("strictTimeoutWithZeroTerminationGrace"); //$NON-NLS-1$

		assertThat(TimeoutUtils.findTerminationGracePeriod(context)).contains(Duration.ZERO);
	}

	@Test
	void configuredTerminationGraceOverridesTheCallersDefault() throws Exception {
		// The third argument is the caller's default, not the period to use: an
		// instructor who configured one must win over it. Thirty seconds stands in for
		// a caller default long enough that taking it would be unmistakable, and the
		// annotation configures ten milliseconds. The timeout itself is 500 ms rather
		// than the 20 ms the other fixtures use, so the worker has certainly entered
		// its loop before it is cancelled; a worker cancelled before it starts never
		// ignores the interruption and would produce no fatal termination at all.
		AtomicBoolean workerStarted = new AtomicBoolean();
		AtomicBoolean releaseWorker = new AtomicBoolean();
		AtomicInteger requestedExitCode = new AtomicInteger(-1);
		TestContext context = contextFor("strictTimeoutWithShortTerminationGrace"); //$NON-NLS-1$
		long startedAt = System.nanoTime();
		try {
			assertThrows(FatalTermination.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
				workerStarted.set(true);
				while (!releaseWorker.get()) {
					Thread.onSpinWait();
				}
				return null;
			}, context, Duration.ofSeconds(30), exitCode -> {
				requestedExitCode.set(exitCode);
				throw new FatalTermination();
			}));
		} finally {
			releaseWorker.set(true);
		}

		assertThat(workerStarted).isTrue();
		assertThat(requestedExitCode).hasValue(124);
		// Well inside the caller default, so this fails rather than merely slows down
		// if the configured period were ignored.
		assertThat(Duration.ofNanos(System.nanoTime() - startedAt)).isLessThan(Duration.ofSeconds(10));
	}

	@Test
	void subMillisecondTerminationGraceKeepsItsPrecision() throws Exception {
		// Resolved and awaited in nanoseconds. Rounded down to milliseconds, as the
		// wait once was, this would become the zero that means "terminate at once".
		TestContext context = contextFor("strictTimeoutWithSubMillisecondTerminationGrace"); //$NON-NLS-1$

		assertThat(TimeoutUtils.findTerminationGracePeriod(context)).contains(Duration.ofNanos(500_000));
	}

	@Test
	void terminationGraceTooLargeToWaitOnIsRefusedWhenTheAnnotationIsRead() throws Exception {
		// Refused here, before the worker starts, rather than overflowing inside the
		// wait: an ArithmeticException thrown there would escape before the fatal
		// terminator runs and leave a contaminated fork alive.
		assertThrows(IllegalArgumentException.class,
				() -> TimeoutUtils.findTerminationGracePeriod(contextFor("strictTimeoutWithOverflowingGrace"))); //$NON-NLS-1$
		assertThrows(IllegalArgumentException.class,
				() -> TimeoutUtils.findTerminationGracePeriod(contextFor("strictTimeoutWithTwoDayGrace"))); //$NON-NLS-1$
	}

	@Test
	void anUnusableCallerDefaultIsRefusedBeforeTheWorkerStarts() throws Exception {
		// The default is a public parameter, so "every caller in this repository passes
		// a constant" is not an invariant. An unusable one must be refused on the way
		// in, where it costs a clear exception, rather than after cancellation, where
		// it would escape before the fatal terminator and leave the fork alive.
		AtomicBoolean executionStarted = new AtomicBoolean();
		TestContext context = contextFor("strictTimeoutTarget"); //$NON-NLS-1$

		assertThrows(IllegalArgumentException.class, () -> TimeoutUtils.performTimeoutExecution(() -> {
			executionStarted.set(true);
			return null;
		}, context, Duration.ofSeconds(Long.MAX_VALUE), exitCode -> fail("must not terminate the fork"))); //$NON-NLS-1$

		assertThat(executionStarted).isFalse();
	}

	@Test
	void jupiterKeepsItsFiftyMillisecondDefault() throws Exception {
		// The counterpart of the one second JqwikStrictTimeoutExtensionTest pins. The
		// two defaults exist separately, which is the whole reason the attribute has an
		// unset state rather than a numeric default.
		java.lang.reflect.Field field = TimeoutUtils.class.getDeclaredField("DEFAULT_TERMINATION_GRACE_PERIOD"); //$NON-NLS-1$
		field.setAccessible(true);

		assertThat((Duration) field.get(null)).isEqualTo(Duration.ofMillis(50));
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

	private static TestContext contextFor(String methodName) throws NoSuchMethodException {
		Method method = TimeoutUtilsTest.class.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(TimeoutUtilsTest.class));
		return context;
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutTarget() {
		// Provides the annotation consumed through the mocked test context.
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS, terminationGrace = 10, terminationGraceUnit = TimeUnit.SECONDS)
	private static void strictTimeoutWithCustomTerminationGrace() {
		// Provides the custom termination grace consumed through the mocked context.
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS, terminationGrace = 0)
	private static void strictTimeoutWithZeroTerminationGrace() {
		// Provides the configured zero grace consumed through the mocked context.
	}

	@StrictTimeout(value = 500, unit = TimeUnit.MILLISECONDS, terminationGrace = 10, terminationGraceUnit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutWithShortTerminationGrace() {
		// Provides a grace far below the caller default, so which of the two was used
		// is observable, with a timeout long enough for the worker to have started.
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS, terminationGrace = 500, terminationGraceUnit = TimeUnit.MICROSECONDS)
	private static void strictTimeoutWithSubMillisecondTerminationGrace() {
		// Provides a grace below a millisecond, which must survive resolution.
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS, terminationGrace = Long.MAX_VALUE, terminationGraceUnit = TimeUnit.DAYS)
	private static void strictTimeoutWithOverflowingGrace() {
		// Provides a grace no Duration can hold.
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS, terminationGrace = 2, terminationGraceUnit = TimeUnit.DAYS)
	private static void strictTimeoutWithTwoDayGrace() {
		// Provides a grace a Duration holds but the bound refuses.
	}
}
