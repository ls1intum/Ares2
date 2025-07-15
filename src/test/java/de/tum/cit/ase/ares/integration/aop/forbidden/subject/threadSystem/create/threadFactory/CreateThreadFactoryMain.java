package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateThreadFactoryMain {

    private CreateThreadFactoryMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ThreadFactory.newThread(Runnable) method
     */
    public static void newThread() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        Thread thread = threadFactory.newThread(new IllegalThread());
        thread.start();
    }
}
