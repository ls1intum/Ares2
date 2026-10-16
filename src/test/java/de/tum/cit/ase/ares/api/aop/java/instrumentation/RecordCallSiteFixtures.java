package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Classes and stand-ins for {@link JavaInstrumentationRecordCallSiteTest}. They
 * are public because the rewritten copies load in a class loader of their own.
 */
public final class RecordCallSiteFixtures {

	/** How often the stand-in for {@code Thread.start()} was called. */
	static final AtomicInteger STARTS = new AtomicInteger();

	/** How often the stand-in for {@code Object.notify()} was called. */
	static final AtomicInteger NOTIFIES = new AtomicInteger();

	/**
	 * A plain class holding the other dynamic calls the Java compiler emits.
	 */
	public static final class OtherDynamicCalls {

		/** Prevents creating instances. */
		private OtherDynamicCalls() {
		}

		/**
		 * Builds a starter through a method reference to {@code Thread.start}.
		 *
		 * @param thread the thread
		 * @return the starter
		 */
		public static Runnable starterOf(Thread thread) {
			return thread::start;
		}

		/**
		 * Builds a supplier through a lambda that joins strings.
		 *
		 * @param number a number
		 * @return the supplier
		 */
		public static Supplier<String> describe(int number) {
			return () -> "number " + number;
		}
	}

	/**
	 * Stands in for {@code Thread.start()} and only counts the call.
	 *
	 * @param thread the thread that would have started
	 */
	public static void countStart(Thread thread) {
		STARTS.incrementAndGet();
	}

	/**
	 * Stands in for {@code Object.notify()} and only counts the call.
	 *
	 * @param monitor the monitor that would have been notified
	 */
	public static void countNotify(Object monitor) {
		NOTIFIES.incrementAndGet();
	}

	/** Prevents creating instances. */
	private RecordCallSiteFixtures() {
	}
}
