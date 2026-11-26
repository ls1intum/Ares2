package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A pool the student wrote whose task wrapping hands the JDK a task no
 * allowance names instead of the one it was given.
 */
public final class SubstitutingPool extends ThreadPoolExecutor {

	/**
	 * Builds a pool of two threads.
	 */
	public SubstitutingPool() {
		super(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
	}

	/**
	 * Wraps a task no allowance names instead of the given one.
	 *
	 * @param <T>      the result type
	 * @param callable the task it was given
	 * @return the wrapped substitute
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return super.newTaskFor((Callable<T>) new UnlistedCall());
	}
}
