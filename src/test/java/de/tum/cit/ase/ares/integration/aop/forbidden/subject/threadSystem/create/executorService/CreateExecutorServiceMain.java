package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.Collection;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateExecutorServiceMain {

    private CreateExecutorServiceMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ExecutorService.execute(java.lang.Runnable) method
     */
    public static void executeRunnable() {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new IllegalThread());
        } finally {

        }
    }

    /**
     * Tests ExecutorService.submit(Runnable) method
     */
    public static void submitRunnable() {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new IllegalThread());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.submit(Runnable, Object) method
     */
    public static void submitRunnableWithResult() {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            String result = "result";
            executorService.submit(new IllegalThread(), result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.submit(Callable) method
     */
    public static void submitCallable() {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Callable<String> callable = () -> "result";
            executorService.submit(callable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.invokeAll(Collection) method
     */
    public static void invokeAll() throws InterruptedException {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.invokeAll(Collection, long, TimeUnit) method
     */
    public static void invokeAllWithTimeout() throws InterruptedException {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAll(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.invokeAny(Collection) method
     */
    public static void invokeAny() throws Exception {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAny(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests ExecutorService.invokeAny(Collection, long, TimeUnit) method
     */
    public static void invokeAnyWithTimeout() throws Exception {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAny(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
