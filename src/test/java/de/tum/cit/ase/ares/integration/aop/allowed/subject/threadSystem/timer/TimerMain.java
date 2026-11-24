package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Student code that starts timers and schedules tasks on them.
 */
public final class TimerMain {

	/**
	 * How long to wait for the scheduled tasks, in seconds.
	 */
	private static final long WAIT_SECONDS = 30;

	/**
	 * A utility class is not built.
	 */
	private TimerMain() {
	}

	/**
	 * Starts timers, schedules tasks on each and waits for the tasks to run.
	 *
	 * @param timers        how many timers to start
	 * @param tasksPerTimer how many tasks to schedule on each timer
	 * @return how many tasks ran
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static long scheduleOnTimers(int timers, int tasksPerTimer) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(timers * tasksPerTimer);
		List<Timer> started = new ArrayList<>();
		try {
			for (int i = 0; i < timers; i++) {
				Timer timer = new Timer(true);
				started.add(timer);
				for (int j = 0; j < tasksPerTimer; j++) {
					timer.schedule(new CountingTask(latch), 0L);
				}
			}
			latch.await(WAIT_SECONDS, TimeUnit.SECONDS);
			return timers * (long) tasksPerTimer - latch.getCount();
		} finally {
			for (Timer timer : started) {
				timer.cancel();
			}
		}
	}
}
