package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.currentThread.interrupt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Student code that restores the interrupt flag in an error path. The thread
 * rule still refuses it, since a call to {@code interrupt()} cannot be told
 * apart from interrupting a stored foreign thread.
 */
public final class InterruptCurrentThreadMain {

	/**
	 * Refuses instantiation, since the class only offers a static method.
	 */
	private InterruptCurrentThreadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Waits for a result and restores the interrupt flag when interrupted.
	 *
	 * @param result the pending result
	 * @return the result
	 */
	public static String awaitResult(Future<String> result) {
		try {
			return result.get(1, TimeUnit.SECONDS);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(interrupted);
		} catch (ExecutionException | TimeoutException failed) {
			throw new IllegalStateException(failed);
		}
	}
}
