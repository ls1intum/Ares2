package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateExecutorServiceMain {

	private CreateExecutorServiceMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Tests ExecutorService.execute(java.lang.Runnable) method
	 */
	public static void executeRunnable() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new IllegalThread());
	}

	/**
	 * Tests ExecutorService.submit(Runnable) method
	 */
	public static void submitRunnable() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new IllegalThread());
	}

	/**
	 * Tests ExecutorService.submit(Runnable, Object) method
	 */
	public static void submitRunnableWithResult() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		String result = "result";
		executorService.submit(new IllegalThread(), result);
	}

	/**
	 * Tests ExecutorService.submit(Callable) method
	 */
	public static void submitCallable() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Callable<String> callable = () -> "result";
		executorService.submit(callable);
	}

	/**
	 * Tests ExecutorService.invokeAll(Collection) method
	 */
	public static void invokeAll() throws InterruptedException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Collection<Callable<String>> tasks = Arrays.asList(() -> "task1", () -> "task2");
		executorService.invokeAll(tasks);
	}

	/**
	 * Tests ExecutorService.invokeAll(Collection, long, TimeUnit) method
	 */
	public static void invokeAllWithTimeout() throws InterruptedException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Collection<Callable<String>> tasks = Arrays.asList(() -> "task1", () -> "task2");
		executorService.invokeAll(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests ExecutorService.invokeAny(Collection) method. invokeAny wraps a failing
	 * task's exception in an ExecutionException, so the resulting RuntimeException
	 * is inherent to the API rather than a gratuitous wrap.
	 */
	public static void invokeAny() throws Exception {
		try {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Collection<Callable<String>> tasks = Arrays.asList(() -> "task1", () -> "task2");
			executorService.invokeAny(tasks);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Tests ExecutorService.invokeAny(Collection, long, TimeUnit) method. invokeAny
	 * wraps a failing task's exception in an ExecutionException, so the resulting
	 * RuntimeException is inherent to the API rather than a gratuitous wrap.
	 */
	public static void invokeAnyWithTimeout() throws Exception {
		try {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Collection<Callable<String>> tasks = Arrays.asList(() -> "task1", () -> "task2");
			executorService.invokeAny(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}
}
