package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A set of tasks the student wrote, whose iterator starts a thread the policy
 * does not allow. Ares must refuse the set rather than iterate it while its own
 * check runs, where that start would go unchecked.
 */
public final class StudentTaskSet extends AbstractSet<Callable<Integer>> {

	/** Whether the iterator's thread was started. */
	private static volatile boolean threadStarted;

	/** Forgets an earlier start. */
	public static void reset() {
		threadStarted = false;
	}

	/**
	 * Says whether the iterator's thread was started.
	 *
	 * @return whether it was
	 */
	public static boolean threadStarted() {
		return threadStarted;
	}

	/**
	 * Starts a thread of a lambda, then hands out one allowed task.
	 *
	 * @return an iterator over one task
	 */
	@Override
	public Iterator<Callable<Integer>> iterator() {
		Thread thread = new Thread(() -> Math.sqrt(2));
		thread.setDaemon(true);
		thread.start();
		threadStarted = true;
		return List.<Callable<Integer>>of(new CountedCall()).iterator();
	}

	/**
	 * Holds one task.
	 *
	 * @return one
	 */
	@Override
	public int size() {
		return 1;
	}
}
