package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Supervised code that hands tasks to a thread pool or to
 * {@code CompletableFuture}, and sorts an array in parallel, so a test can
 * check how the thread allowances count them.
 */
public final class CountedOperationsMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private CountedOperationsMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Submits the given number of {@link CountedTask}s to a pool of two threads and
	 * waits for all of them.
	 *
	 * @param tasks how many tasks to submit
	 * @return how many tasks ran
	 * @throws Exception if a task fails or the wait is interrupted
	 */
	public static int submitToPool(int tasks) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			List<Future<?>> futures = new ArrayList<>();
			for (int i = 0; i < tasks; i++) {
				futures.add(pool.submit(new CountedTask()));
			}
			for (Future<?> future : futures) {
				future.get();
			}
			return futures.size();
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Runs one {@link CountedTask} through {@code CompletableFuture.runAsync} and
	 * waits for it.
	 *
	 * @throws Exception if the task fails or the wait is interrupted
	 */
	public static void runAsync() throws Exception {
		CompletableFuture.runAsync(new CountedTask()).get();
	}

	/**
	 * Sorts a large array with {@code Arrays.parallelSort}.
	 *
	 * @return the smallest element after sorting
	 */
	public static int parallelSort() {
		int[] values = new int[20000];
		for (int i = 0; i < values.length; i++) {
			values[i] = values.length - i;
		}
		Arrays.parallelSort(values);
		return values[0];
	}

	/**
	 * Hands the given tasks to a pool of two threads with {@code invokeAll} and
	 * waits for all of them.
	 *
	 * @param tasks the tasks
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAll(Collection<? extends Callable<Integer>> tasks) throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			return pool.invokeAll(tasks).size();
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Hands a JDK set of one allowed task to {@code invokeAll}.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAJdkSet() throws InterruptedException {
		return invokeAll(new HashSet<>(List.of(new CountedCall())));
	}

	/**
	 * Hands the student's own task set to {@code invokeAll}.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAStudentSet() throws InterruptedException {
		StudentTaskSet.reset();
		return invokeAll(new StudentTaskSet());
	}

	/**
	 * Sorts student lists by size with {@code Arrays.parallelSort}.
	 *
	 * @return the size of the smallest list after sorting
	 */
	public static int parallelSortStudentLists() {
		StudentList[] lists = { new StudentList(2), new StudentList(1) };
		Arrays.parallelSort(lists, (left, right) -> Integer.compare(left.size(), right.size()));
		return lists[0].size();
	}

	/**
	 * Sorts an array that holds itself with {@code Arrays.parallelSort}.
	 *
	 * @return the length of the array
	 */
	public static int parallelSortCyclicArray() {
		Object[] cyclic = new Object[2];
		cyclic[0] = cyclic;
		cyclic[1] = cyclic;
		Arrays.parallelSort(cyclic, (left, right) -> 0);
		return cyclic.length;
	}
}
