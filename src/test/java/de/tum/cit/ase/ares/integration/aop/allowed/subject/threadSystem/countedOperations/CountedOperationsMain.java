package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

	/**
	 * Submits one allowed task to a pool of two threads with a result value, and
	 * waits for it.
	 *
	 * @param result the value the future returns
	 * @return one, the number of tasks that ran
	 * @throws Exception if the task fails or the wait is interrupted
	 */
	public static int submitWithResult(Object result) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			pool.submit(new CountedTask(), result).get();
			return 1;
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Submits one allowed task whose result is a set class the student wrote.
	 *
	 * @return one, the number of tasks that ran
	 * @throws Exception if the task fails or the wait is interrupted
	 */
	public static int submitWithAStudentSetResult() throws Exception {
		return submitWithResult(new StudentResultSet());
	}

	/**
	 * Submits one allowed task whose result is a JDK set that holds a task the
	 * policy does not allow, which never runs.
	 *
	 * @return one, the number of tasks that ran
	 * @throws Exception if the task fails or the wait is interrupted
	 */
	public static int submitWithAJdkSetOfTasksResult() throws Exception {
		return submitWithResult(new HashSet<>(Set.<Callable<Integer>>of(() -> 1)));
	}

	/**
	 * Hands a JDK set holding a task the policy does not allow to
	 * {@code invokeAll}.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAJdkSetWithAnUnallowedTask() throws InterruptedException {
		return invokeAll(new HashSet<>(Set.<Callable<Integer>>of(() -> 1)));
	}

	/**
	 * Hands the student's own task set to {@code invokeAll} inside a JDK wrapper
	 * that only passes the iteration on.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAWrappedStudentSet() throws InterruptedException {
		StudentTaskSet.reset();
		return invokeAll(Collections.unmodifiableSet(new StudentTaskSet()));
	}

	/**
	 * Hands one allowed task to {@code invokeAll} in an unmodifiable view of a JDK
	 * list.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAWrappedList() throws InterruptedException {
		return invokeAll(Collections.unmodifiableList(List.of(new CountedCall())));
	}

	/**
	 * Hands one allowed task to {@code invokeAll} in an {@code ArrayList}.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAnArrayList() throws InterruptedException {
		return invokeAll(new ArrayList<>(List.of(new CountedCall())));
	}

	/**
	 * Hands one allowed task to {@code invokeAll} in a list built by
	 * {@code List.of}.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromAListOf() throws InterruptedException {
		return invokeAll(List.of(new CountedCall()));
	}

	/**
	 * Submits the given tasks to a pool of two threads, one call each, and waits
	 * for them.
	 *
	 * @param tasks the tasks
	 * @return how many tasks ran
	 * @throws Exception if a task fails or the wait is interrupted
	 */
	public static int submitEach(Runnable... tasks) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			for (Runnable task : tasks) {
				pool.submit(task).get();
			}
			return tasks.length;
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Submits the given number of list-backed tasks of the allowed class, one call
	 * each.
	 *
	 * @param tasks how many tasks to submit
	 * @return how many tasks ran
	 * @throws Exception if a task fails or the wait is interrupted
	 */
	public static int submitListBackedTasks(int tasks) throws Exception {
		Runnable[] submitted = new Runnable[tasks];
		for (int i = 0; i < tasks; i++) {
			submitted[i] = new ListBackedTask();
		}
		return submitEach(submitted);
	}

	/**
	 * Submits one list-backed task of a class no allowance names.
	 *
	 * @return how many tasks ran
	 * @throws Exception if the task fails or the wait is interrupted
	 */
	public static int submitUnlistedListTask() throws Exception {
		return submitEach(new UnlistedListTask());
	}

	/**
	 * Hands a range view of a sorted set of two allowed tasks to {@code invokeAll},
	 * with the student's comparator armed only after the view was built.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllFromATreeSetRange() throws InterruptedException {
		StudentTaskComparator.arm(false);
		TreeSet<Callable<Integer>> tasks = new TreeSet<>(new StudentTaskComparator());
		tasks.add(new CountedCall());
		tasks.add(new CountedCall());
		NavigableSet<Callable<Integer>> range = tasks.subSet(tasks.first(), true, tasks.last(), true);
		StudentTaskComparator.arm(true);
		try {
			return invokeAll(range);
		} finally {
			StudentTaskComparator.arm(false);
		}
	}

	/**
	 * Hands a batch of allowed tasks to {@code invokeAll} in a {@code List.of}
	 * list.
	 *
	 * @param tasks how many tasks the batch holds
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllBatch(int tasks) throws InterruptedException {
		List<Callable<Integer>> batch = new ArrayList<>();
		for (int i = 0; i < tasks; i++) {
			batch.add(new CountedCall());
		}
		return invokeAll(List.copyOf(batch));
	}

	/**
	 * Hands the given number of set-backed allowed tasks to {@code invokeAll} in a
	 * {@code List.of} list.
	 *
	 * @param tasks how many tasks the batch holds
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllSetBackedCalls(int tasks) throws InterruptedException {
		List<Callable<Integer>> batch = new ArrayList<>();
		for (int i = 0; i < tasks; i++) {
			batch.add(new SetBackedCall());
		}
		return invokeAll(List.copyOf(batch));
	}

	/**
	 * Hands two allowed tasks to the student's pool whose wrapping replaces the
	 * second one with a task no allowance names.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllWithASwappingPool() throws InterruptedException {
		SwappingPool pool = new SwappingPool();
		try {
			List<Callable<Integer>> batch = Arrays.asList(new CountedCall(), new CountedCall());
			pool.swapIn(batch);
			return pool.invokeAll(batch).size();
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Hands a batch to a pool whose only thread is busy, so the pool runs each task
	 * on the calling thread; the first task replaces the second one with a task no
	 * allowance names before the pool reaches it.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if a wait is interrupted
	 */
	public static int invokeAllWithAReplacingTask() throws InterruptedException {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
				new ThreadPoolExecutor.CallerRunsPolicy());
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch released = new CountDownLatch(1);
		try {
			pool.execute(new LatchedTask(started, released));
			started.await();
			List<Callable<Integer>> batch = Arrays.asList(new ReplacingCall(), new CountedCall());
			ReplacingCall.belongTo(batch);
			return pool.invokeAll(batch).size();
		} finally {
			released.countDown();
			pool.shutdownNow();
		}
	}

	/**
	 * Hands two allowed tasks in an {@code ArrayList} to the student's pool that
	 * wraps them as the JDK does.
	 *
	 * @return how many tasks ran
	 * @throws InterruptedException if the wait is interrupted
	 */
	public static int invokeAllWithAPlainSubclassPool() throws InterruptedException {
		PlainSubclassPool pool = new PlainSubclassPool();
		try {
			return pool.invokeAll(new ArrayList<>(List.of(new CountedCall(), new CountedCall()))).size();
		} finally {
			pool.shutdownNow();
		}
	}

	/**
	 * Hands two allowed tasks to {@code invokeAny} on a pool of two threads.
	 *
	 * @return the result of the task that finished first
	 * @throws Exception if no task finishes or the wait is interrupted
	 */
	public static int invokeAnyBatch() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			return pool.invokeAny(List.of(new CountedCall(), new CountedCall()));
		} finally {
			pool.shutdownNow();
		}
	}
}
