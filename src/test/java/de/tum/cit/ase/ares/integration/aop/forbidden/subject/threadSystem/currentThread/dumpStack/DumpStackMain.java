package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.dumpStack;

/**
 * Student code calling {@code Thread.dumpStack}, which the thread rule keeps
 * forbidden now that no bare {@code java.lang.Thread} entry covers it.
 */
public final class DumpStackMain {

	/**
	 * Refuses instantiation, since the class only offers a static method.
	 */
	private DumpStackMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Prints the stack of the current thread.
	 */
	public static void dumpStack() {
		Thread.dumpStack();
	}
}
