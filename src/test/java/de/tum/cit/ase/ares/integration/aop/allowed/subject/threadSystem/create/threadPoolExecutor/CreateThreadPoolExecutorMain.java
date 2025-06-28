package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadPoolExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateThreadPoolExecutorMain {

    private CreateThreadPoolExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ThreadPoolExecutor.execute(Runnable) method
     */
    public static void executeRunnable() {
        try (ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
        )) {
            threadPoolExecutor.execute(new LegalThread());
        }
    }

    /**
     * Tests ThreadPoolExecutor.submit(Runnable) method
     */
    // public static void submitRunnable() {
    //     try (ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
    //             1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
    //     )) {
    //         threadPoolExecutor.submit(new LegalThread());
    //     }
    // }

    /**
     * Tests ThreadPoolExecutor.submit(Callable) method
     */
    // public static void submitCallable() {
    //     try (ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
    //             1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
    //     )) {
    //         Callable<String> callable = () -> "test";
    //         threadPoolExecutor.submit(callable);
    //     }
    // }
}
