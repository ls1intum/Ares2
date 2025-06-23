package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.threadBuilder;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateThreadBuilderMain {

    private CreateThreadBuilderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests Thread.Builder.start(Runnable) method
     */
    public static void createThreadBuilder() {
        Thread.Builder.OfPlatform builder = Thread.ofPlatform();
        builder.start(new LegalThread());
    }
}
