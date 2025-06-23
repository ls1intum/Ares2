package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.threadGroup;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateThreadGroupMain {

    private CreateThreadGroupMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests ThreadGroup new Thread creation
     */
    public static void createThreadInGroup() {
        ThreadGroup threadGroup = new ThreadGroup("TestGroup");
        Thread thread = new Thread(threadGroup, new IllegalThread(), "TestThread");
        thread.start();
    }
}
