package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateExecutorMain {

    private CreateExecutorMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    // COMMENTED OUT: Not in the original list of 20 methods
    // /**
    //  * Tests Executor.execute(Runnable) method
    //  */
    // public static void executeRunnable() {
    //     Executor executor = Executors.newSingleThreadExecutor();
    //     executor.execute(new IllegalThread());
    // }
}
