package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.scheduledThreadPoolExecutor;

public class CreateScheduledThreadPoolExecutorMain {

    private CreateScheduledThreadPoolExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ScheduledThreadPoolExecutor.execute(Runnable) method
     */
    // public static void executeRunnable() {
    //     ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    //     try {
    //         scheduledThreadPoolExecutor.execute(new LegalThread());
    //     } finally {
    //         scheduledThreadPoolExecutor.shutdown();
    //     }
    // }

    /**
     * Tests ScheduledThreadPoolExecutor.submit(Runnable) method
     */
    // public static void submitRunnable() {
    //     ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    //     try {
    //         scheduledThreadPoolExecutor.submit(new LegalThread());
    //     } finally {
    //         scheduledThreadPoolExecutor.shutdown();
    //     }
    // }

    /**
     * Tests ScheduledThreadPoolExecutor.submit(Callable) method
     */
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
