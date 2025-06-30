package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executor;

public class CreateExecutorMain {

    private CreateExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests Executor.execute(Runnable) method
     */
    // public static void executeRunnable() {
    //     Executor executor = Executors.newSingleThreadExecutor();
    //     executor.execute(new LegalThread());
    // }
}
