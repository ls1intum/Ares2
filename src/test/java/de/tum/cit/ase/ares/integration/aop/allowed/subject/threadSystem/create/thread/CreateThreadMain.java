package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.thread;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateThreadMain {

	private CreateThreadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Tests Thread.start() method
	 */
	public static void startThread() {
		Thread thread = new Thread(new LegalThread());
		thread.start();
	}

	/**
	 * Tests Thread.notify() method
	 */
	public static void notifyThread() {
		Thread thread = new Thread(new LegalThread());
		synchronized (thread) {
			thread.start();
			thread.notify();
		}
	}

	/**
	 * Tests Thread.notify() without a preceding start(), so notify() is the thread
	 * operation actually exercised rather than being masked by an earlier start().
	 */
	public static void notifyThreadOnly() {
		Thread thread = new Thread(new LegalThread());
		synchronized (thread) {
			thread.notify();
		}
	}

	/**
	 * Tests Thread.notifyAll(), the sibling wake operation of notify().
	 */
	public static void notifyAllThreadOnly() {
		Thread thread = new Thread(new LegalThread());
		synchronized (thread) {
			thread.notifyAll();
		}
	}

	/**
	 * Tests the parameterised Object.wait(long) monitor operation on a Thread whose
	 * class is allowed to be created.
	 */
	public static void waitThreadOnly() {
		Thread thread = new Thread(new LegalThread());
		synchronized (thread) {
			try {
				thread.wait(1L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
