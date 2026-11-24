package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
}
