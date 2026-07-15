package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread;

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
	 * Tests Thread.notify() method
	 */
	public static void notifyThread() {
		IllegalThread illegalThread = new IllegalThread();
		Thread thread = new Thread(illegalThread);
		synchronized (illegalThread) {
			thread.start();
			illegalThread.notify();
		}
	}

	/**
	 * Tests Thread.notify() without a preceding start(), so notify() is the thread
	 * operation actually exercised rather than being masked by an earlier start().
	 */
	public static void notifyThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			thread.notify();
		}
	}

	/**
	 * Tests Thread.notifyAll(), the sibling wake operation of notify().
	 */
	public static void notifyAllThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			thread.notifyAll();
		}
	}

	/**
	 * Tests the parameterised Object.wait(long) monitor operation on a Thread.
	 */
	public static void waitThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			try {
				thread.wait(1L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
