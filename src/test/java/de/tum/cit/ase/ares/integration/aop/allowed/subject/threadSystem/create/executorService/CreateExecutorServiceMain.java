package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executorService;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.Collection;
import java.util.Arrays;

public class CreateExecutorServiceMain {

    private CreateExecutorServiceMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ExecutorService.execute(java.lang.Runnable) method
     */
    public static void executeRunnable() {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.execute(new LegalThread());
        }
    }

    /**
     * Tests ExecutorService.submit(Runnable) method
     */
    public static void submitRunnable() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(new LegalThread());
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.submit(Runnable, Object) method
     */
    public static void submitRunnableWithResult() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            String result = "result";
            executorService.submit(new LegalThread(), result);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.submit(Callable) method
     */
    public static void submitCallable() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Callable<String> callable = () -> "test";
            executorService.submit(callable);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.invokeAll(Collection) method
     */
    public static void invokeAll() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAll(tasks);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.invokeAll(Collection, long, TimeUnit) method
     */
    public static void invokeAllWithTimeout() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAll(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.invokeAny(Collection) method
     */
    public static void invokeAny() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAny(tasks);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Tests ExecutorService.invokeAny(Collection, long, TimeUnit) method
     */
    public static void invokeAnyWithTimeout() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Collection<Callable<String>> tasks = Arrays.asList(
                    () -> "task1",
                    () -> "task2"
            );
            executorService.invokeAny(tasks, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } finally {
            executorService.shutdown();
        }
    }
}
