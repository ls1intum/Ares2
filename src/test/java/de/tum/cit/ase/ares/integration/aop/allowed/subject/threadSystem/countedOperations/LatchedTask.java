package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.concurrent.CountDownLatch;

/**
 * An allowed task the student wrote that keeps the only thread of a pool busy:
 * it says when it has started and waits until it is released.
 */
public final class LatchedTask implements Runnable {

	/** Counted down once this task runs. */
	private final CountDownLatch started;

	/** Awaited before this task ends. */
	private final CountDownLatch released;

	/**
	 * Builds the task.
	 *
	 * @param started  counted down once the task runs
	 * @param released awaited before the task ends
	 */
	public LatchedTask(CountDownLatch started, CountDownLatch released) {
		this.started = started;
		this.released = released;
	}

	/**
	 * Says it has started and waits for its release.
	 */
	@Override
	public void run() {
		started.countDown();
		try {
			released.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
