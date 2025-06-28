package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledExecutorService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateScheduledExecutorServiceMain {

    private CreateScheduledExecutorServiceMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ScheduledExecutorService.schedule(Runnable, long, TimeUnit) method
     */
    public static void scheduleRunnable() {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1)) {
            scheduledExecutorService.schedule(new IllegalThread(), 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Tests ScheduledExecutorService.schedule(Callable, long, TimeUnit) method
     */
    public static void scheduleCallable() {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1)) {
            scheduledExecutorService.schedule(() -> "result", 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Tests ScheduledExecutorService.scheduleAtFixedRate(Runnable, long, long, TimeUnit) method
     */
    public static void scheduleAtFixedRate() {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1)) {
            scheduledExecutorService.scheduleAtFixedRate(new IllegalThread(), 1, 2, TimeUnit.SECONDS);
        }
    }

    /**
     * Tests ScheduledExecutorService.scheduleWithFixedDelay(Runnable, long, long, TimeUnit) method
     */
    // public static void scheduleWithFixedDelay() {
    //     ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    //     scheduledExecutorService.scheduleWithFixedDelay(new IllegalThread(), 1, 2, TimeUnit.SECONDS);
    // }
}
