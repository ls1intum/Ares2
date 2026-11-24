package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

/**
 * The task class a thread allowance names.
 */
public final class CountedTask implements Runnable {

	/**
	 * Does a little arithmetic.
	 */
	@Override
	public void run() {
		Math.sqrt(2);
	}
}
