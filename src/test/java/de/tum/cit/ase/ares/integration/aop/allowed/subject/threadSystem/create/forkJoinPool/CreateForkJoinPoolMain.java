package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.forkJoinPool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateForkJoinPoolMain {

    private CreateForkJoinPoolMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ForkJoinPool.commonPool().execute(Runnable) method
     */
    public static void commonPoolExecute() {
        ForkJoinPool.commonPool().execute(new LegalThread());
    }

    /**
     * Tests ForkJoinPool.commonPool().submit(Callable) method
     */
    public static void commonPoolSubmitCallableTask() {
        Callable<String> callable = () -> "test";
        ForkJoinPool.commonPool().submit(callable);
    }

    /**
     * Tests ForkJoinPool.commonPool().submit(Runnable) method
     */
    public static void commonPoolSubmitRunnable() {
        ForkJoinPool.commonPool().submit(new LegalThread());
    }
}
