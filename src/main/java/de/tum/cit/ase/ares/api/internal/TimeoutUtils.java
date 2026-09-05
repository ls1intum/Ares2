package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.internal.BlacklistedInvoker.invokeChecked;
import static de.tum.cit.ase.ares.api.localization.Messages.localizedFailure;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.AssertionFailedError;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.context.*;

@API(status = Status.INTERNAL)
public final class TimeoutUtils {
	private static final Duration DEFAULT_TERMINATION_GRACE_PERIOD = Duration.ofMillis(50);
	/**
	 * The largest termination grace period an instructor may configure.
	 * <p>
	 * A bound is needed because the period became instructor-controlled: the wait
	 * is expressed in nanoseconds against a deadline, and a period long enough to
	 * overflow that arithmetic would make the wait expire at once or, worse, throw
	 * out of {@link #terminateTimedOutExecution} before the fatal terminator runs,
	 * leaving a contaminated fork alive. Rejecting it when the annotation is read,
	 * which is before the worker starts, keeps that failure impossible rather than
	 * merely unlikely. A day is far beyond any period a test can want and well
	 * inside what the arithmetic holds.
	 */
	private static final Duration MAXIMUM_TERMINATION_GRACE_PERIOD = Duration.ofDays(1);
	private static final int UNRESPONSIVE_TIMEOUT_EXIT_CODE = 124;

	private TimeoutUtils() {
	}

	public static Optional<Duration> findTimeout(TestContext context) {
		var strictTimeout = TestContextUtils.findAnnotationIn(context, StrictTimeout.class);
		return strictTimeout.map(st -> Duration.of(st.value(), st.unit().toChronoUnit()));
	}

	/**
	 * The termination grace period the instructor configured, if any.
	 * <p>
	 * Empty when no {@link StrictTimeout} is in scope, and equally when one is but
	 * leaves {@link StrictTimeout#terminationGrace()} negative, which is how the
	 * annotation expresses "not configured". Distinguishing the two matters because
	 * the extensions do not share one period: an absent value has to fall back to
	 * the caller's own, which is 50 ms for JUnit and one second for jqwik, and an
	 * attribute default could only ever have named one of them.
	 *
	 * @param context the test context to read the annotation from.
	 * @return the configured period, or empty if none was configured.
	 */
	static Optional<Duration> findTerminationGracePeriod(TestContext context) {
		var strictTimeout = TestContextUtils.findAnnotationIn(context, StrictTimeout.class);
		if (strictTimeout.isEmpty() || strictTimeout.get().terminationGrace() < 0) {
			return Optional.empty();
		}
		StrictTimeout annotation = strictTimeout.get();
		try {
			Duration configured = Duration.of(annotation.terminationGrace(),
					annotation.terminationGraceUnit().toChronoUnit());
			terminationGraceNanos(configured, "@StrictTimeout terminationGrace"); //$NON-NLS-1$
			return Optional.of(configured);
		} catch (@SuppressWarnings("unused") ArithmeticException tooLarge) {
			throw new IllegalArgumentException("@StrictTimeout terminationGrace of " //$NON-NLS-1$
					+ annotation.terminationGrace() + " " + annotation.terminationGraceUnit() //$NON-NLS-1$
					+ " must not exceed " + MAXIMUM_TERMINATION_GRACE_PERIOD); //$NON-NLS-1$
		}
	}

	/**
	 * The period as a nanosecond count, refusing anything the wait cannot hold.
	 * <p>
	 * Every termination grace period passes through here, whether it came from the
	 * annotation or from the extension that supplied the default, and it does so
	 * before the worker is created. That ordering is the point: converting after
	 * the worker has timed out and been cancelled would let an overflow throw out
	 * of {@link #terminateTimedOutExecution} before the fatal terminator runs, and
	 * a fork whose untrusted code ignores interruption would then outlive its
	 * security lifecycle. Nothing is converted after cancellation.
	 *
	 * @param period the period to convert; must be non-null, non-negative and at
	 *               most {@link #MAXIMUM_TERMINATION_GRACE_PERIOD}.
	 * @param source what to name in the message if it is unusable.
	 * @return the period in nanoseconds.
	 * @throws IllegalArgumentException if the period is unusable.
	 */
	private static long terminationGraceNanos(Duration period, String source) {
		if (period == null || period.isNegative() || period.compareTo(MAXIMUM_TERMINATION_GRACE_PERIOD) > 0) {
			throw new IllegalArgumentException(
					source + " must be between zero and " + MAXIMUM_TERMINATION_GRACE_PERIOD + ", but was " + period); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return period.toNanos();
	}

	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context) throws Throwable {
		return performTimeoutExecution(execution, context, DEFAULT_TERMINATION_GRACE_PERIOD);
	}

	/**
	 * Runs the execution under its {@link StrictTimeout}, if it has one.
	 *
	 * @param execution                     the execution to bound.
	 * @param context                       the test context carrying the
	 *                                      annotation.
	 * @param defaultTerminationGracePeriod the period to allow for termination
	 *                                      after the interruption when the
	 *                                      annotation does not configure one. It is
	 *                                      the caller's default, not an override:
	 *                                      an instructor-configured
	 *                                      {@code terminationGrace} always wins.
	 * @return whatever the execution returned.
	 * @throws Throwable whatever the execution threw.
	 */
	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration defaultTerminationGracePeriod) throws Throwable {
		return performTimeoutExecution(execution, context, defaultTerminationGracePeriod,
				exitCode -> Runtime.getRuntime().halt(exitCode));
	}

	static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration defaultTerminationGracePeriod, IntConsumer fatalProcessTerminator) throws Throwable {
		var timeout = findTimeout(context);
		if (timeout.isEmpty()) {
			return execution.get();
		}
		// Resolved here rather than in the entry points, so the annotation is read a
		// second time only for an execution that is actually bounded, and so every
		// overload resolves it the same way. The caller's default is held to the same
		// bound as a configured value: the parameter is public, so "the callers in
		// this repository pass a constant" is not an invariant.
		Duration terminationGracePeriod = findTerminationGracePeriod(context).orElse(defaultTerminationGracePeriod);
		long terminationGraceNanos = terminationGraceNanos(terminationGracePeriod, "terminationGracePeriod"); //$NON-NLS-1$
		return executeWithTimeout(timeout.get(), () -> rethrowThrowableSafe(execution), context, terminationGraceNanos,
				fatalProcessTerminator);
	}

	private static <T> T rethrowThrowableSafe(ThrowingSupplier<T> execution) throws Exception { // NOSONAR
		try {
			return execution.get();
		} catch (Exception | Error e) {
			throw e;
		} catch (Throwable t) {
			/*
			 * Should never happen, as there are no other direct subclasses of Throwable in
			 * use. But students might still do that, so better be prepared.
			 */
			throw new ExecutionException(t);
		}
	}

	private static <T> T executeWithTimeout(Duration timeout, Callable<T> action, TestContext context,
			long terminationGraceNanos, IntConsumer fatalProcessTerminator) throws Throwable { // NOSONAR
		var threadFactory = new WhitelistedThreadFactory();
		var executorService = Executors.newSingleThreadExecutor(threadFactory);
		Future<T> future = executorService.submit(action);
		try {
			return invokeChecked(() -> future.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
		} catch (ExecutionException ex) {
			// should never happen, but you never know
			if (ex.getCause() instanceof ExecutionException) {
				throw ex.getCause().getCause();
			}
			throw ex.getCause();
		} catch (@SuppressWarnings("unused") TimeoutException ex) {
			terminateTimedOutExecution(future, executorService, terminationGraceNanos, fatalProcessTerminator);
			throw generateTimeoutFailure(timeout, context);
		} finally {
			executorService.shutdownNow();
		}
	}

	static void terminateTimedOutExecution(Future<?> future, ExecutorService executorService,
			long terminationGraceNanos, IntConsumer fatalProcessTerminator) {
		executorService.shutdown();
		future.cancel(true);
		/*
		 * Future.cancel(true) delivers the single interruption that asks the timed-out
		 * execution to stop. Calling shutdownNow() here would interrupt the worker a
		 * second time; that interruption can race with interruption-aware code leaving
		 * its body. Give it time to finish before the owning thread continues. If it
		 * ignores interruption, the fork is already contaminated: returning would let
		 * untrusted code outlive its security, IO and reporting lifecycle and affect
		 * later tests in a reused JVM. Thread.stop() cannot repair that safely or
		 * reliably, so fail closed by terminating the complete worker process and let
		 * Maven, Gradle or the IDE report the crashed test fork.
		 */
		// Nanoseconds, converted and bounds-checked before the worker was created: the
		// period is instructor-configurable in any TimeUnit now, and rounding it down
		// to milliseconds would turn a configured sub-millisecond grace into the zero
		// that means "terminate at once".
		if (!awaitTermination(executorService, terminationGraceNanos)) {
			fatalProcessTerminator.accept(UNRESPONSIVE_TIMEOUT_EXIT_CODE);
			throw new AssertionError("Fatal process terminator returned without terminating the test worker"); //$NON-NLS-1$
		}
	}

	private static boolean awaitTermination(ExecutorService executorService, long timeoutNanos) {
		long deadline = System.nanoTime() + timeoutNanos;
		boolean interrupted = false;
		try {
			while (!executorService.isTerminated()) {
				long remainingNanos = deadline - System.nanoTime();
				if (remainingNanos <= 0) {
					return false;
				}
				try {
					if (executorService.awaitTermination(remainingNanos, TimeUnit.NANOSECONDS)) {
						return true;
					}
				} catch (@SuppressWarnings("unused") InterruptedException ex) {
					interrupted = true;
				}
			}
			return true;
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static AssertionFailedError generateTimeoutFailure(Duration timeout, TestContext context) {
		var failure = localizedFailure("timeout.failure_message", formatDuration(timeout)); //$NON-NLS-1$
		if (TestContextUtils.findAnnotationIn(context, PrivilegedExceptionsOnly.class).isPresent()) {
			throw new PrivilegedException(failure);
		}
		return failure;
	}

	private static String formatDuration(Duration duration) {
		List<String> parts = new ArrayList<>();
		long h = duration.toHours();
		int m = duration.toMinutesPart();
		int s = duration.toSecondsPart();
		int ms = duration.toMillisPart();
		if (h != 0) {
			parts.add(h + " h"); //$NON-NLS-1$
		}
		if (m != 0) {
			parts.add(m + " min"); //$NON-NLS-1$
		}
		if (s != 0) {
			parts.add(s + " s"); //$NON-NLS-1$
		}
		if (ms != 0) {
			parts.add(ms + " ms"); //$NON-NLS-1$
		}
		return String.join(" ", parts); //$NON-NLS-1$
	}

	private static class WhitelistedThreadFactory implements ThreadFactory {
		private static final AtomicInteger TIMEOUT_THREAD_ID = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			var thread = new Thread(r, "ajts-to-" + TIMEOUT_THREAD_ID.getAndIncrement()); //$NON-NLS-1$
			thread.setDaemon(true);
			if (thread.getPriority() != Thread.NORM_PRIORITY) {
				thread.setPriority(Thread.NORM_PRIORITY);
			}
			return thread;
		}
	}
}
