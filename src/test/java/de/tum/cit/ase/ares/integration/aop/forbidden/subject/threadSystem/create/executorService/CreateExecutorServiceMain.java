package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.Collection;
import java.util.Arrays;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateExecutorServiceMain {

    private CreateExecutorServiceMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }    public static void createExecutorService() {
        @SuppressWarnings("resource")
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                java.util.concurrent.TimeUnit.MILLISECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>()
        );
        threadPoolExecutor.submit(new IllegalThread());
    }

    /**
     * Tests ExecutorService.submit(Callable) method
     */
    public static void submitCallable() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<String> callable = () -> "test";
        executorService.submit(callable);
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
     * Tests ExecutorService.submit(Runnable) method
     */
    public static void submitRunnable() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new IllegalThread());
    }

    /**
     * Tests ExecutorService.invokeAll(Collection) method
     */
    public static void invokeAll() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Collection<Callable<String>> tasks = Arrays.asList(
            () -> "task1",
            () -> "task2"
        );
        executorService.invokeAll(tasks);
    }

    /**
     * Tests ExecutorService.invokeAny(Collection) method
     */
    public static void invokeAny() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Collection<Callable<String>> tasks = Arrays.asList(
            () -> "task1",
            () -> "task2"
        );
        executorService.invokeAny(tasks);
    }
}
