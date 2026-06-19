package de.tum.cit.ase.ares.integration.testuser.subject.threads;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
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

	public static void threadAccess() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(() -> {
			try {
				Thread.sleep(1000);
			} catch (@SuppressWarnings("unused") InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		executor.submit(() -> {
			try {
				Thread.sleep(1000);
			} catch (@SuppressWarnings("unused") InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		executor.shutdown();
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
		Thread firstThread = new Thread(() -> sleepInCurrentThread(100));
		Thread secondThread = new Thread(() -> {
			// nothing
		});
		firstThread.start();
		secondThread.start();
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

	static void commonPoolInterruptable() throws InterruptedException, ExecutionException {
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

	public static void runItself() {
		new ThreadPenguin().start();
	}

	@Override
	public void start() {
		super.start();
	}

	public static void executorServiceSlide8() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Runnable task1 = () -> System.out.println("Task 1 executed");
		Runnable task2 = () -> System.out.println("Task 2 executed");
		executor.submit(task1);
		executor.submit(task2);
		executor.shutdown();
	}

	public static void executorServiceSlide9() {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		for (int orderId = 1; orderId <= 5; orderId++) {
			int finalOrderId = orderId; // Effectively final for lambda
			executor.submit(() -> {
				System.out.println("Processing Order #" + finalOrderId + " by " + Thread.currentThread().getName());
			});
		}

		executor.shutdown();
	}

	public static void executorServiceSlide10() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Callable<String> fetchUser = () -> {
			Thread.sleep(2000); // Simulate database delay
			return "User Data for User ID 42";
		};
		Future<String> result = executor.submit(fetchUser);

		executor.shutdown();
	}

	public static void executorServiceSlide11() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		Runnable sendNotification = () -> System.out.println("Notification sent at " + System.currentTimeMillis());
		scheduler.scheduleAtFixedRate(sendNotification, 0, 5, TimeUnit.SECONDS);
		// Schedule shutdown after 20 seconds for demonstration
		scheduler.schedule(() -> scheduler.shutdown(), 20, TimeUnit.SECONDS);
	}

	public static void executorServiceFuture1() throws ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Integer> future = executor.submit(() -> {
			Thread.sleep(2000); // Simulate long-running task
			return 42;
		});
		System.out.println("Task submitted...");
		System.out.println("Result: " + future.get()); // Blocks until the result is available
		executor.shutdown();
	}

	public static void completableFutureSlide1() {
		CompletableFuture.supplyAsync(() -> {
			System.out.println("Task running...");
			return "Hello";
		}).thenApply(result -> result + " World").thenAccept(System.out::println);
	}

	public static void completableFutureSlide2() {
		CompletableFuture.supplyAsync(() -> "Task 1")
				.thenCompose(task1Result -> CompletableFuture.supplyAsync(() -> task1Result + " + Task 2"))
				.thenAccept(System.out::println);
	}

	public static void completableFutureSlide3Combine() {
		CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> 20);
		CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> 22);
		task1.thenCombine(task2, Integer::sum).thenAccept(result -> System.out.println("Sum: " + result));
	}

	public static void parallelStreams1() {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
		numbers.parallelStream().map(n -> {
			System.out.println("Processing " + n + " on thread " + Thread.currentThread().getName());
			return n * n;
		}).forEach(System.out::println);
	}

	public static void publisherSubscriber() {
		// A real reactive publisher delivers items asynchronously on the common
		// ForkJoinPool,
		// so submit() schedules work onto a pool thread. The previous bespoke publisher
		// ran
		// entirely synchronously in the calling thread and therefore performed no
		// thread-system
		// operation at all, making the "must be blocked" assertion unreachable.
		try (SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>()) {
			TemperatureSubscriber subscriber = new TemperatureSubscriber();
			publisher.subscribe(subscriber);
			for (int i = 0; i < 10; i++) {
				publisher.submit((int) (Math.random() * 100));
			}
		}
	}

	static class TemperatureSubscriber implements Flow.Subscriber<Integer> {
		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			System.out.println("Subscribed to temperature readings.");
			subscription.request(10); // Request 10 readings at a time
		}

		@Override
		public void onNext(Integer temperature) {
			System.out.println("Received temperature: " + temperature + "°C");
			if (temperature > 70) {
				System.out.println("Warning: High temperature detected!");
			}
		}

		@Override
		public void onError(Throwable throwable) {
			System.err.println("Error occurred: " + throwable.getMessage());
		}

		@Override
		public void onComplete() {
			System.out.println("All temperature readings processed.");
		}
	}
}
