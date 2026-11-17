package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.holdsLock;

/**
 * Student code calling {@code Thread.holdsLock}, which the thread rule keeps
 * forbidden now that no bare {@code java.lang.Thread} entry covers it.
 */
public final class HoldsLockMain {

	/**
	 * Refuses instantiation, since the class only offers a static method.
	 */
	private HoldsLockMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Asks whether the current thread holds the monitor of an object.
	 *
	 * @param lock the object
	 * @return whether the current thread holds its monitor
	 */
	public static boolean holdsLock(Object lock) {
		return Thread.holdsLock(lock);
	}
}
