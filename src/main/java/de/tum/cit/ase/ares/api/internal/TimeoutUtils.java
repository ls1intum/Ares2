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
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutConfiguration;

@API(status = Status.INTERNAL)
public final class TimeoutUtils {
	private static final Duration DEFAULT_TERMINATION_GRACE_PERIOD = Duration.ofMillis(50);
	/**
	 * The largest termination grace period the policy may set. A longer period
	 * would overflow the wait's nanosecond deadline, so it is refused before the
	 * worker starts; a day is far beyond what a test needs.
	 */
	private static final Duration MAXIMUM_TERMINATION_GRACE_PERIOD = Duration.ofDays(1);
	private static final int UNRESPONSIVE_TIMEOUT_EXIT_CODE = 124;

	private TimeoutUtils() {
	}

	/**
	 * The timeout for a test: the nearest {@link StrictTimeout}, else the active
	 * policy's {@code regardingTheStrictTimeoutForTheTestCases}, else none.
	 *
	 * @param context the test context.
	 * @return the timeout, or empty when the test is not bounded
	 */
	public static Optional<Duration> findTimeout(TestContext context) {
		var strictTimeout = TestContextUtils.findAnnotationIn(context, StrictTimeout.class);
		if (strictTimeout.isPresent()) {
			return strictTimeout.map(st -> Duration.of(st.value(), st.unit().toChronoUnit()));
		}
		Optional<StrictTimeoutConfiguration> policy = ConfigurationUtils.findPolicyStrictTimeout(context);
		return policy.isEmpty() ? Optional.empty() : Optional.of(policy.get().timeout());
	}

	/**
	 * The termination grace period the active policy sets with
	 * {@code theTerminationGraceIs}, checked against its bound. Empty when the
	 * policy sets none; the caller's period then applies.
	 *
	 * @param context the test context to read the policy from.
	 * @return the configured period, or empty if none was configured.
	 */
	static Optional<Duration> findTerminationGracePeriod(TestContext context) {
		Optional<StrictTimeoutConfiguration> policy = ConfigurationUtils.findPolicyStrictTimeout(context);
		if (policy.isEmpty()) {
			return Optional.empty();
		}
		Optional<Duration> configured = policy.get().terminationGrace();
		if (configured.isPresent()) {
			terminationGraceNanos(configured.get(), "regardingTheStrictTimeoutForTheTestCases.theTerminationGraceIs"); //$NON-NLS-1$
		}
		return configured;
	}

	/**
	 * The period as a nanosecond count, refusing a negative one or one above
	 * {@link #MAXIMUM_TERMINATION_GRACE_PERIOD}. Every period is converted here
	 * before the worker starts, so an overflow can never stop the fatal terminator
	 * after a timeout.
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

	/**
	 * Runs the execution under the test's timeout, if it has one, with the default
	 * grace period of 50 ms when the policy sets none.
	 *
	 * @param <T>       the type the execution returns.
	 * @param execution the execution to bound.
	 * @param context   the test context.
	 * @return whatever the execution returned.
	 * @throws Throwable whatever the execution threw.
	 */
	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context) throws Throwable {
		return performTimeoutExecution(execution, context, DEFAULT_TERMINATION_GRACE_PERIOD);
	}

	/**
	 * Runs the execution under the test's timeout, if it has one: the nearest
	 * {@link StrictTimeout}, else the policy's.
	 *
	 * @param <T>                           the type the execution returns.
	 * @param execution                     the execution to bound.
	 * @param context                       the test context.
	 * @param defaultTerminationGracePeriod the time the worker gets to stop after
	 *                                      its interruption when the policy sets no
	 *                                      {@code theTerminationGraceIs}.
	 * @return whatever the execution returned.
	 * @throws Throwable whatever the execution threw.
	 */
	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration defaultTerminationGracePeriod) throws Throwable {
		return performTimeoutExecution(execution, context, defaultTerminationGracePeriod,
				exitCode -> Runtime.getRuntime().halt(exitCode));
	}

	/**
	 * Runs the execution under the test's timeout with a replaceable fatal
	 * terminator. The timeout and the grace period, the caller's default included,
	 * are converted and checked before the worker starts.
	 *
	 * @param <T>                           the type the execution returns.
	 * @param execution                     the execution to bound.
	 * @param context                       the test context.
	 * @param defaultTerminationGracePeriod the grace period when the policy sets
	 *                                      none.
	 * @param fatalProcessTerminator        ends the process with an exit code.
	 * @return whatever the execution returned.
	 * @throws Throwable whatever the execution threw.
	 */
	static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration defaultTerminationGracePeriod, IntConsumer fatalProcessTerminator) throws Throwable {
		var timeout = findTimeout(context);
		if (timeout.isEmpty()) {
			return execution.get();
		}
		Duration terminationGracePeriod = findTerminationGracePeriod(context).orElse(defaultTerminationGracePeriod);
		long terminationGraceNanos = terminationGraceNanos(terminationGracePeriod, "terminationGracePeriod"); //$NON-NLS-1$
		long timeoutNanos = timeoutNanos(timeout.get());
		boolean privilegedTimeout = ConfigurationUtils.getNonprivilegedFailureMessage(context).isPresent();
		return executeWithTimeout(timeout.get(), timeoutNanos, () -> rethrowThrowableSafe(execution),
				terminationGraceNanos, privilegedTimeout, fatalProcessTerminator);
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

	/**
	 * The timeout as a nanosecond count, the precision the wait uses, converted
	 * before the worker starts so an overflow never surfaces while student code
	 * runs.
	 *
	 * @param timeout the timeout to convert.
	 * @return the timeout in nanoseconds.
	 * @throws IllegalArgumentException if it does not fit a nanosecond count.
	 */
	static long timeoutNanos(Duration timeout) {
		try {
			return timeout.toNanos();
		} catch (ArithmeticException tooLong) {
			throw new IllegalArgumentException("timeout must fit a nanosecond count, but was " + timeout, tooLong); //$NON-NLS-1$
		}
	}

	/**
	 * Runs the action on a fresh worker thread and waits for it at most the
	 * timeout; a timed-out action is stopped and reported as a timeout failure.
	 *
	 * @param <T>                    the type the action returns.
	 * @param timeout                the timeout, for the message.
	 * @param timeoutNanos           the timeout in nanoseconds.
	 * @param action                 the action.
	 * @param terminationGraceNanos  the time the worker gets to stop.
	 * @param privilegedTimeout      whether the failure is wrapped as privileged.
	 * @param fatalProcessTerminator ends the process with an exit code.
	 * @return whatever the action returned.
	 * @throws Throwable whatever the action threw, or the timeout failure.
	 */
	private static <T> T executeWithTimeout(Duration timeout, long timeoutNanos, Callable<T> action,
			long terminationGraceNanos, boolean privilegedTimeout, IntConsumer fatalProcessTerminator)
			throws Throwable { // NOSONAR
		var threadFactory = new WhitelistedThreadFactory();
		var executorService = Executors.newSingleThreadExecutor(threadFactory);
		Future<T> future = executorService.submit(action);
		try {
			return invokeChecked(() -> awaitResult(future, timeoutNanos));
		} catch (ExecutionException ex) {
			// should never happen, but you never know
			if (ex.getCause() instanceof ExecutionException) {
				throw ex.getCause().getCause();
			}
			throw ex.getCause();
		} catch (@SuppressWarnings("unused") TimeoutException ex) {
			terminateTimedOutExecution(future, executorService, terminationGraceNanos, fatalProcessTerminator);
			throw generateTimeoutFailure(timeout, privilegedTimeout);
		} finally {
			executorService.shutdownNow();
		}
	}

	/**
	 * Waits for the execution's result at most the timeout, in nanoseconds, so a
	 * timeout below a millisecond is not rounded down to an immediate failure.
	 *
	 * @param <T>          the type the execution returns.
	 * @param future       the running execution.
	 * @param timeoutNanos the longest wait, in nanoseconds.
	 * @return the execution's result.
	 * @throws InterruptedException if the waiting thread is interrupted.
	 * @throws ExecutionException   if the execution threw.
	 * @throws TimeoutException     if the wait ran out.
	 */
	static <T> T awaitResult(Future<T> future, long timeoutNanos)
			throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeoutNanos, TimeUnit.NANOSECONDS);
	}

	/**
	 * Stops a timed-out execution with a single interruption and waits up to the
	 * grace period for its worker to end. A worker that ignores the interruption
	 * would outlive the test's security lifecycle in a reused JVM, so the whole
	 * process is then ended with exit code 124.
	 *
	 * @param future                 the timed-out execution.
	 * @param executorService        the executor running it.
	 * @param terminationGraceNanos  the time the worker gets to stop.
	 * @param fatalProcessTerminator ends the process with an exit code.
	 */
	static void terminateTimedOutExecution(Future<?> future, ExecutorService executorService,
			long terminationGraceNanos, IntConsumer fatalProcessTerminator) {
		executorService.shutdown();
		future.cancel(true);
		if (!awaitTermination(executorService, terminationGraceNanos)) {
			fatalProcessTerminator.accept(UNRESPONSIVE_TIMEOUT_EXIT_CODE);
			throw new AssertionError("Fatal process terminator returned without terminating the test worker"); //$NON-NLS-1$
		}
	}

	/**
	 * Waits up to the given time for the executor to terminate, keeping a pending
	 * interruption of the waiting thread.
	 *
	 * @param executorService the executor.
	 * @param timeoutNanos    the longest wait, in nanoseconds.
	 * @return whether the executor terminated in time.
	 */
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

	/**
	 * Builds the timeout failure. With privileged-only reporting on it is wrapped
	 * as privileged, so a public timeout keeps its own text rather than the
	 * policy's message.
	 *
	 * @param timeout           the timeout that passed.
	 * @param privilegedTimeout whether the policy turns privileged-only reporting
	 *                          on.
	 * @return the failure to throw.
	 */
	private static AssertionFailedError generateTimeoutFailure(Duration timeout, boolean privilegedTimeout) {
		var failure = localizedFailure("timeout.failure_message", formatDuration(timeout)); //$NON-NLS-1$
		if (privilegedTimeout) {
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
