package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.scheduledThreadPoolExecutor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Callable;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateScheduledThreadPoolExecutorMain {

    private CreateScheduledThreadPoolExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    // COMMENTED OUT: Not in the original list of 20 methods
    // /**
    //  * Tests ScheduledThreadPoolExecutor.execute(Runnable) method
    //  */
    // public static void executeRunnable() {
    //     ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    //     try {
    //         scheduledThreadPoolExecutor.execute(new IllegalThread());
    //     } finally {
    //         scheduledThreadPoolExecutor.shutdown();
    //     }
    // }

    // COMMENTED OUT: Not in the original list of 20 methods
    // /**
    //  * Tests ScheduledThreadPoolExecutor.submit(Runnable) method
    //  */
    // public static void submitRunnable() {
    //     ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    //     try {
    //         scheduledThreadPoolExecutor.submit(new IllegalThread());
    //     } finally {
    //         scheduledThreadPoolExecutor.shutdown();
    //     }
    // }

    // COMMENTED OUT: Not in the original list of 20 methods
    // /**
    //  * Tests ScheduledThreadPoolExecutor.submit(Callable) method
    //  */
    // public static void submitCallable() {
    //     ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    //     Callable<String> callable = () -> "test";
    //     try {
    //         scheduledThreadPoolExecutor.submit(callable);
    //     } finally {
    //         scheduledThreadPoolExecutor.shutdown();
    //     }
    // }
}
