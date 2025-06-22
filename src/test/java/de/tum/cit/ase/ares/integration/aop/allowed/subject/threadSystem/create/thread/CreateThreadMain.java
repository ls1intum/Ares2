package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.thread;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateThreadMain {

    private CreateThreadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests Thread.start() method
     */
    public static void startThread() {
        Thread thread = new Thread(new LegalThread());
        thread.start();
    }

    /**
     * Tests Thread.startVirtualThread(Runnable) method
     */
    public static void startVirtualThread() {
        Thread.startVirtualThread(new LegalThread());
    }
}
