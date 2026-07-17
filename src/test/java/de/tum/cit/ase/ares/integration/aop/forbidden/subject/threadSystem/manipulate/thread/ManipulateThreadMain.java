package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.manipulate.thread;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public final class ManipulateThreadMain {

	private ManipulateThreadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	public static void notifyThread() {
		IllegalThread illegalThread = new IllegalThread();
		Thread thread = new Thread(illegalThread);
		synchronized (illegalThread) {
			thread.start();
			illegalThread.notify();
		}
	}

	public static void notifyThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			thread.notify();
		}
	}

	public static void notifyAllThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			thread.notifyAll();
		}
	}

	public static void waitThreadOnly() {
		Thread thread = new Thread(new IllegalThread());
		synchronized (thread) {
			try {
				thread.wait(1L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		}
	}

	public static void notifyCurrentThread() {
		Thread thread = Thread.currentThread();
		synchronized (thread) {
			thread.notify();
		}
	}

	public static void notifyAllCurrentThread() {
		Thread thread = Thread.currentThread();
		synchronized (thread) {
			thread.notifyAll();
		}
	}

	public static void waitCurrentThread() {
		waitProvidedThread(Thread.currentThread());
	}

	public static void notifyProvidedThread(Thread thread) {
		synchronized (thread) {
			thread.notify();
		}
	}

	public static void notifyAllProvidedThread(Thread thread) {
		synchronized (thread) {
			thread.notifyAll();
		}
	}

	public static void waitProvidedThread(Thread thread) {
		synchronized (thread) {
			try {
				thread.wait(1L, 1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		}
	}
}
