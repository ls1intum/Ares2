package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledExecutorService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateScheduledExecutorServiceMain {

    private CreateScheduledExecutorServiceMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ScheduledExecutorService.schedule(Runnable, long, TimeUnit) method
     */
    public static void scheduleRunnable() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(new LegalThread(), 1, TimeUnit.SECONDS);
    }

    /**
     * Tests ScheduledExecutorService.schedule(Callable, long, TimeUnit) method
     */
    public static void scheduleCallable() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Callable<String> callable = () -> "test";
        scheduledExecutorService.schedule(callable, 1, TimeUnit.SECONDS);
    }

    /**
     * Tests ScheduledExecutorService.scheduleAtFixedRate(Runnable, long, long, TimeUnit) method
     */
    public static void scheduleAtFixedRate() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new LegalThread(), 1, 2, TimeUnit.SECONDS);
    }

    /**
     * Tests ScheduledExecutorService.scheduleWithFixedDelay(Runnable, long, long, TimeUnit) method
     */
    // public static void scheduleWithFixedDelay() {
    //     ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    //     scheduledExecutorService.scheduleWithFixedDelay(new LegalThread(), 1, 2, TimeUnit.SECONDS);
    // }
}
