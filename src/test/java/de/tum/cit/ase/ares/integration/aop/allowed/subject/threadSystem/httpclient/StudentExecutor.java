package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.httpclient;

import java.util.concurrent.Executor;

/**
 * A student executor that starts a new thread for every task it gets.
 */
public final class StudentExecutor implements Executor {

	/**
	 * Runs the task on a new thread.
	 *
	 * @param task the task to run
	 */
	@Override
	public void execute(Runnable task) {
		new Thread(task).start();
	}
}
