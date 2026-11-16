package de.tum.cit.ase.ares.api.aop.java.instrumentation;

/**
 * A record such as a student writes, which also starts a thread and notifies on
 * a monitor. Top-level, so its rewritten copy loads without its enclosing
 * class.
 *
 * @param x the first component
 * @param y the second component
 */
public record RecordCallSitePoint(int x, int y) {

	/**
	 * Starts the given thread.
	 *
	 * @param thread the thread
	 */
	public static void startThread(Thread thread) {
		thread.start();
	}

	/**
	 * Notifies on the given monitor.
	 *
	 * @param monitor the monitor
	 */
	public static void notifyMonitor(Object monitor) {
		synchronized (monitor) {
			monitor.notify();
		}
	}
}
