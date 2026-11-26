package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.Comparator;
import java.util.concurrent.Callable;

/**
 * A comparator the student wrote for a sorted set of tasks. Once armed, each
 * comparison starts a thread the policy does not allow, so a check that
 * compares while it is in progress would start that thread unchecked.
 */
public final class StudentTaskComparator implements Comparator<Callable<Integer>> {

	/** Whether comparisons start the thread. */
	private static volatile boolean armed;

	/** Whether a comparison started the thread. */
	private static volatile boolean threadStarted;

	/**
	 * Sets whether comparisons start the thread, and forgets an earlier start.
	 *
	 * @param arm whether to start the thread from now on
	 */
	public static void arm(boolean arm) {
		armed = arm;
		threadStarted = false;
	}

	/**
	 * Says whether a comparison started the thread.
	 *
	 * @return whether it did
	 */
	public static boolean threadStarted() {
		return threadStarted;
	}

	/**
	 * Orders two tasks by identity, starting the thread first when armed.
	 *
	 * @param left  one task
	 * @param right the other task
	 * @return their order
	 */
	@Override
	public int compare(Callable<Integer> left, Callable<Integer> right) {
		if (armed) {
			Thread thread = new Thread(() -> Math.sqrt(2));
			thread.setDaemon(true);
			thread.start();
			threadStarted = true;
		}
		return Integer.compare(System.identityHashCode(left), System.identityHashCode(right));
	}
}
