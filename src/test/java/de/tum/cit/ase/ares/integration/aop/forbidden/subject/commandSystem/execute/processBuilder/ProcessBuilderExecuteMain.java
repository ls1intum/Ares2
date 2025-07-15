package de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder;

import java.io.IOException;

public class ProcessBuilderExecuteMain {

    private ProcessBuilderExecuteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Execute a forbidden command using the {@link ProcessBuilder} class.
     */
    public static Process executeCommandViaProcessBuilder() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("forbidden-command");
        return processBuilder.start();
    }
}
