package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;

/**
 * Child-process fixture that must be terminated: without an argument by
 * {@link TimeoutUtils}, with {@code message} or {@code lock} by
 * {@link ReportingUtils}, whose failure it cannot finish reading because its
 * own getMessage loops for ever or student code keeps its lock.
 */
public final class TimeoutUtilsForkProbe {
	private TimeoutUtilsForkProbe() {
	}

	public static void main(String[] arguments) throws Throwable {
		if (arguments.length > 0) {
			reportUnreadableFailure(arguments[0]);
			return;
		}
		Method method = TimeoutUtilsForkProbe.class.getDeclaredMethod("strictTimeoutTarget"); //$NON-NLS-1$
		TestContext context = new ProbeContext(method);
		TimeoutUtils.performTimeoutExecution(() -> {
			while (true) {
				Thread.onSpinWait();
			}
		}, context);
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutTarget() {
	}

	/**
	 * Reports the failure the argument names, outside any policy.
	 *
	 * @param kind {@code message} or {@code lock}.
	 * @throws Exception if the probe cannot be set up
	 */
	private static void reportUnreadableFailure(String kind) throws Exception {
		Method method = TimeoutUtilsForkProbe.class.getDeclaredMethod("unsupervisedTarget"); //$NON-NLS-1$
		Throwable failure = "lock".equals(kind) ? lockedForEver() : new EndlessMessageException(); //$NON-NLS-1$
		ReportingUtils.processThrowable(failure, new ProbeContext(method));
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

	/** The test method of the reporting probe; it carries no policy. */
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

	/**
	 * A test context for one method of a child-process probe, with no test type.
	 */
	static final class ProbeContext extends TestContext {
		private final Method method;

		ProbeContext(Method method) {
			this.method = method;
		}

		@Override
		public Optional<Method> testMethod() {
			return Optional.of(method);
		}

		@Override
		public Optional<Class<?>> testClass() {
			return Optional.of(TimeoutUtilsForkProbe.class);
		}

		@Override
		public Optional<Object> testInstance() {
			return Optional.empty();
		}

		@Override
		public Optional<String> displayName() {
			return Optional.of("timeout fork probe"); //$NON-NLS-1$
		}

		@Override
		public Optional<AnnotatedElement> annotatedElement() {
			return Optional.of(method);
		}

		@Override
		public Optional<TestType> findTestType() {
			return Optional.empty();
		}
	}
}
