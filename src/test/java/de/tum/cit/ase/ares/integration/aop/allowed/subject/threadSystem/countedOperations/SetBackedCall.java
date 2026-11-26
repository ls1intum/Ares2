package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * A callable task class the student wrote that is also a set; a thread
 * allowance names it.
 */
public final class SetBackedCall extends HashSet<Object> implements Callable<Integer> {

	/** The serial version, as for every set. */
	private static final long serialVersionUID = 1L;

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
