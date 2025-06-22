package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledThreadPoolExecutor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateScheduledThreadPoolExecutorMain {

    private CreateScheduledThreadPoolExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ScheduledThreadPoolExecutor.execute(Runnable) method
     */
    public static void executeRunnable() {
        @SuppressWarnings("resource")
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.execute(new LegalThread());
    }

    /**
     * Tests ScheduledThreadPoolExecutor.submit(Runnable) method
     */
    public static void submitRunnable() {
        @SuppressWarnings("resource")
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.submit(new LegalThread());
    }

    /**
     * Tests ScheduledThreadPoolExecutor.submit(Callable) method
     */
    public static void submitCallable() {
        @SuppressWarnings("resource")
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        Callable<String> callable = () -> "test";
        scheduledThreadPoolExecutor.submit(callable);
    }
}
