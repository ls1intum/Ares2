package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * An allowed callable task the student wrote that, while it runs, puts itself
 * in place of the second task of the batch it belongs to, so it would run
 * twice.
 */
public final class RepeatingCall implements Callable<Integer> {

	/** The batch this task belongs to, set before the batch is handed over. */
	private static volatile List<Callable<Integer>> batch;

	/**
	 * Sets the batch the next run puts this task into again.
	 *
	 * @param tasks the batch
	 */
	public static void belongTo(List<Callable<Integer>> tasks) {
		batch = tasks;
	}

	/**
	 * Puts this task in place of the second task of the batch.
	 *
	 * @return always one
	 */
	@Override
	public Integer call() {
		batch.set(1, this);
		return 1;
	}
}
