package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * Child-process fixture whose failure Ares cannot finish reading: its own
 * getMessage loops for ever ignoring interruption, or student code keeps its
 * lock. {@link ReportingUtils} must end the run with exit code 124.
 */
public final class ReportingUtilsForkProbe {

	private ReportingUtilsForkProbe() {
	}

	/**
	 * Reports the failure the argument names, outside any policy.
	 *
	 * @param arguments {@code message} or {@code lock}.
	 * @throws Exception if the probe cannot be set up
	 */
	public static void main(String[] arguments) throws Exception {
		Method method = ReportingUtilsForkProbe.class.getDeclaredMethod("unsupervisedTarget"); //$NON-NLS-1$
		Throwable failure = "lock".equals(arguments[0]) ? lockedForEver() : new EndlessMessageException(); //$NON-NLS-1$
		ReportingUtils.processThrowable(failure, new TimeoutUtilsForkProbe.ProbeContext(method));
	}

	/**
	 * A failure whose lock a daemon thread holds until the run ends.
	 *
	 * @return the locked failure.
	 * @throws InterruptedException if interrupted while waiting for the lock
	 */
	private static Throwable lockedForEver() throws InterruptedException {
		RuntimeException failure = new RuntimeException("locked"); //$NON-NLS-1$
		CountDownLatch locked = new CountDownLatch(1);
		Thread holder = new Thread(() -> holdForEver(failure, locked));
		holder.setDaemon(true);
		holder.start();
		locked.await();
		return failure;
	}

	/**
	 * Holds a failure's lock without ever releasing it, as student code could.
	 *
	 * @param failure the failure to lock.
	 * @param locked  counted down once the lock is held.
	 */
	private static void holdForEver(Throwable failure, CountDownLatch locked) {
		synchronized (failure) {
			locked.countDown();
			while (true) {
				Thread.onSpinWait();
			}
		}
	}

	/** The test method of the probe; it carries no policy. */
	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private static void unsupervisedTarget() {
	}

	/**
	 * A student exception whose own getMessage never returns, even when
	 * interrupted.
	 */
	private static final class EndlessMessageException extends RuntimeException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Loops for ever, ignoring interruption, as a hostile override could.
		 *
		 * @return never.
		 */
		@Override
		public String getMessage() {
			while (true) {
				Thread.onSpinWait();
			}
		}
	}
}
