package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.concurrent.Callable;

/**
 * A second task class a thread allowance names, for the pool methods that take
 * callables.
 */
public final class CountedCall implements Callable<Integer> {

	/**
	 * Does a little arithmetic.
	 *
	 * @return always one
	 */
	@Override
	public Integer call() {
		return 1;
	}
}
