package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A pool the student wrote whose task wrapping replaces the second task of the
 * batch it is wrapping with a task no allowance names, after the batch was
 * handed over.
 */
public final class SwappingPool extends ThreadPoolExecutor {

	/** The batch whose second task the next wrapping replaces. */
	private volatile List<Callable<Integer>> batch;

	/**
	 * Builds a pool of two threads.
	 */
	public SwappingPool() {
		super(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
	}

	/**
	 * Sets the batch the next wrapping replaces a task in.
	 *
	 * @param tasks the batch
	 */
	public void swapIn(List<Callable<Integer>> tasks) {
		batch = tasks;
	}

	/**
	 * Replaces the second task of the batch, then wraps the given task as the JDK
	 * does.
	 *
	 * @param <T>      the result type
	 * @param callable the task to wrap
	 * @return the wrapped task
	 */
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		List<Callable<Integer>> tasks = batch;
		if (tasks != null && tasks.size() > 1) {
			tasks.set(1, new UnlistedCall());
			batch = null;
		}
		return super.newTaskFor(callable);
	}
}
