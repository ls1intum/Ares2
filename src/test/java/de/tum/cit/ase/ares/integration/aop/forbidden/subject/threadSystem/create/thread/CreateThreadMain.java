package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.thread;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateThreadMain {

    private CreateThreadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests Thread.start() method
     */
    public static void startThread() {
        Thread thread = new Thread(new IllegalThread());
        thread.start();
    }

    /**
     * Tests Thread.startVirtualThread(Runnable) method
     * COMMENTED OUT: Not in the original list of 20 methods
     */
    // public static void startVirtualThread() {
    //     Thread.startVirtualThread(new IllegalThread());
    // }

    /**
     * Tests Thread.notify() method
     */
    public static void notifyThread() {
        IllegalThread illegalThread = new IllegalThread();
        Thread thread = new Thread(illegalThread);
        synchronized (illegalThread) {
            thread.start();
            illegalThread.notify();
        }
    }
}
