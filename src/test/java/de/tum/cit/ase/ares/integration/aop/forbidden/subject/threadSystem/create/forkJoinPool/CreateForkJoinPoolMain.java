package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.forkJoinPool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateForkJoinPoolMain {

    private CreateForkJoinPoolMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ForkJoinPool.execute(Runnable) method
     */
    public static void commonPoolExecute() {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            forkJoinPool.execute(new IllegalThread());
        }
    }

    /**
     * Tests ForkJoinPool.submit(Callable task) method
     */
    public static void commonPoolSubmitCallableTask() {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            Callable<String> task = () -> "test";
            forkJoinPool.submit(task);
        }
    }

    /**
     * Tests ForkJoinPool.submit(Runnable) method
     */
    public static void commonPoolSubmitRunnable() {
        try (ForkJoinPool forkJoinPool = new ForkJoinPool()) {
            forkJoinPool.submit(new IllegalThread());
        }
    }
}
