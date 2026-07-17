package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationThreadSystemCallSite;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateThreadMain {

	private CreateThreadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Tests Thread.start() method
	 */
	public static void startThread() {
		Runnable r = new IllegalThread();
		Thread thread = new Thread(r);
		thread.start();
	}

	/**
	 * Tries to bypass ordinary call-site rewriting by invoking its public
	 * replacement directly.
	 */
	public static void startThreadThroughInstrumentationCallSite() {
		Runnable runnable = new IllegalThread();
		Thread thread = new Thread(runnable);
		JavaInstrumentationThreadSystemCallSite.start(thread);
	}
}
