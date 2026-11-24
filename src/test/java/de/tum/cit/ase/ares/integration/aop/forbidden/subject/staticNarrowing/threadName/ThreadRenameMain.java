package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.threadName;

/**
 * Supervised code that renames the thread it runs on.
 */
public final class ThreadRenameMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private ThreadRenameMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Renames the current thread.
	 */
	public static void rename() {
		Thread.currentThread().setName("renamed by student code");
	}
}
