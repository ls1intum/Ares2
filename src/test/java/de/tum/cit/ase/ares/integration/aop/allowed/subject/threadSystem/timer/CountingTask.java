package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.timer;

import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * A timer task that only counts that it ran.
 */
public final class CountingTask extends TimerTask {

	/**
	 * The latch counted down when the task runs.
	 */
	private final CountDownLatch latch;

	/**
	 * Builds a task that counts down the given latch.
	 *
	 * @param latch the latch to count down
	 */
	public CountingTask(CountDownLatch latch) {
		this.latch = latch;
	}

	/**
	 * Counts down the latch.
	 */
	@Override
	public void run() {
		latch.countDown();
	}
}
