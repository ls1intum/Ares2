package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.concurrent.Callable;

/**
 * A callable task class the student wrote that no thread allowance names.
 */
public final class UnlistedCall implements Callable<Integer> {

	/**
	 * Does a little arithmetic.
	 *
	 * @return always two
	 */
	@Override
	public Integer call() {
		return 2;
	}
}
