package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.read;

/**
 * Student code that only reads the current thread, which the thread rule
 * permits.
 */
public final class ReadCurrentThreadMain {

	/**
	 * Refuses instantiation, since the class only offers a static method.
	 */
	private ReadCurrentThreadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Reads the current thread twice and compares the two references.
	 *
	 * @return {@code true}, since both reads return the same thread
	 */
	public static boolean readsTheSameThreadTwice() {
		Thread first = Thread.currentThread();
		return first == Thread.currentThread();
	}
}
