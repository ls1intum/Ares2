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
	private static final int UNRESPONSIVE_TIMEOUT_EXIT_CODE = 124;

	private TimeoutUtils() {
	}

	public static Optional<Duration> findTimeout(TestContext context) {
		var strictTimeout = TestContextUtils.findAnnotationIn(context, StrictTimeout.class);
		return strictTimeout.map(st -> Duration.of(st.value(), st.unit().toChronoUnit()));
	}

	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context) throws Throwable {
		return performTimeoutExecution(execution, context, DEFAULT_TERMINATION_GRACE_PERIOD);
	}

	public static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration terminationGracePeriod) throws Throwable {
		return performTimeoutExecution(execution, context, terminationGracePeriod,
				exitCode -> Runtime.getRuntime().halt(exitCode));
	}

	static <T> T performTimeoutExecution(ThrowingSupplier<T> execution, TestContext context,
			Duration terminationGracePeriod, IntConsumer fatalProcessTerminator) throws Throwable {
		var timeout = findTimeout(context);
		if (timeout.isEmpty()) {
			return execution.get();
		}
		return executeWithTimeout(timeout.get(), () -> rethrowThrowableSafe(execution), context, terminationGracePeriod,
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
			Duration terminationGracePeriod, IntConsumer fatalProcessTerminator) throws Throwable { // NOSONAR
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
			terminateTimedOutExecution(future, executorService, terminationGracePeriod, fatalProcessTerminator);
			throw generateTimeoutFailure(timeout, context);
		} finally {
			executorService.shutdownNow();
		}
	}

	private static void terminateTimedOutExecution(Future<?> future, ExecutorService executorService,
			Duration terminationGracePeriod, IntConsumer fatalProcessTerminator) {
		future.cancel(true);
		executorService.shutdownNow();
		/*
		 * Give interruption-aware code time to finish before the owning thread
		 * continues. If it ignores interruption, the fork is already contaminated:
		 * returning would let untrusted code outlive its security, IO and reporting
		 * lifecycle and affect later tests in a reused JVM. Thread.stop() cannot repair
		 * that safely or reliably, so fail closed by terminating the complete worker
		 * process and let Maven, Gradle or the IDE report the crashed test fork.
		 */
		if (!awaitTermination(executorService, terminationGracePeriod.toMillis())) {
			fatalProcessTerminator.accept(UNRESPONSIVE_TIMEOUT_EXIT_CODE);
			throw new AssertionError("Fatal process terminator returned without terminating the test worker"); //$NON-NLS-1$
		}
	}

	private static boolean awaitTermination(ExecutorService executorService, long timeoutMillis) {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
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
