package de.tum.cit.ase.ares.integration.testuser.subject.threads;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//REMOVED: Import of ArtemisSecurityManager

public final class ThreadPenguin extends Thread {

	public ThreadPenguin() {
		super("ThreadPenguin");
	}

	public static void sleepInCurrentThread(long millies) {
		try {
			Thread.sleep(millies);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static void tryStartTwoThreads() {
		Thread t1 = new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (@SuppressWarnings("unused") InterruptedException e) {
				// ignore
			}
		});
		t1.start();

		new Thread().start();
	}

	public static void tryBreakThreadGroup() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		for (;;) {
			ThreadGroup parent = threadGroup.getParent();
			if (parent == null)
				break;
			threadGroup = parent;
		}
		new Thread(threadGroup, () -> {
			// nothing
		}).start();
	}

	public static void spawnEndlessThreads() {
		try {
			Thread.sleep(2);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			// nothing
		}
		for (int i = 0; i < 2000; i++) {
			Thread t = new Thread(ThreadPenguin::spawnEndlessThreads);
			t.start();
		}
	}

	private static void verifyThreadWhitelisting(String message) throws Throwable {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread t = new Thread(() -> failure.set(new SecurityException(message)));
		t.setUncaughtExceptionHandler((t1, e) -> failure.set(e));
		t.start();
		t.join();
		if (failure.get() != null)
			throw failure.get();
	}

	public static void tryThreadWhitelisting() throws Throwable {
		verifyThreadWhitelisting("Thread not whitelisted");
	}

	void threadWhitelistingWithPathFail() throws Throwable {
		verifyThreadWhitelisting("Thread not whitelisted");
	}

	void commonPoolInterruptable() throws InterruptedException, ExecutionException {
		// check functionality
		var res = ForkJoinPool.commonPool().submit(() -> "A").get();
		// submit long-running task
		var task = ForkJoinPool.commonPool().submit(() -> {
			ThreadPenguin.sleepInCurrentThread(5_000);
		});
		// check that the task is still running after 100 ms
		try {
			Thread.sleep(100);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		// wait for task end
		ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS);
	}

	public static void something() {
		new ThreadPenguin().start();
	}

	@Override
	public void start() {
		super.start();
	}
}
