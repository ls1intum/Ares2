package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.ArrayList;

/**
 * A task class the student wrote that is also a list; no thread allowance names
 * it.
 */
public final class UnlistedListTask extends ArrayList<Object> implements Runnable {

	/** The serial version, as for every list. */
	private static final long serialVersionUID = 1L;

	/**
	 * Does a little arithmetic.
	 */
	@Override
	public void run() {
		Math.sqrt(2);
	}
}
