package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.record;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

/**
 * A student record that tries to start a thread.
 *
 * @param name a component, so the record has generated methods to build
 */
public record CreateThreadFromRecordMain(String name) {

	/**
	 * Starts a thread from inside the record.
	 */
	public static void startThread() {
		Thread thread = new Thread(new IllegalThread());
		thread.start();
	}
}
