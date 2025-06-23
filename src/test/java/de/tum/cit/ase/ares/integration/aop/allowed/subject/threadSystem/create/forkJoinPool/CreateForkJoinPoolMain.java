package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.forkJoinPool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateForkJoinPoolMain {

    private CreateForkJoinPoolMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ForkJoinPool.execute(Runnable) method
     */
    public static void executeRunnable() {
        @SuppressWarnings("resource")
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.execute(new LegalThread());
    }

    /**
     * Tests ForkJoinPool.submit(Runnable) method
     */
    public static void submitRunnable() {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            forkJoinPool.submit(new LegalThread());
        }
    }

    /**
     * Tests ForkJoinPool.submit(Callable) method
     */
    public static void submitCallable() {
        @SuppressWarnings("resource")
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Callable<String> callable = () -> "test";
        forkJoinPool.submit(callable);
    }
}
