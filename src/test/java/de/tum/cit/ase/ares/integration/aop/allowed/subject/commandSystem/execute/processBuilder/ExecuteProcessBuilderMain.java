package de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.processBuilder;

import java.io.IOException;

public class ExecuteProcessBuilderMain {

    private ExecuteProcessBuilderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Execute a command using the {@link ProcessBuilder} class.
     */
    public static Process executeCommandViaProcessBuilder(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return processBuilder.start();
    }

    /**
     * Execute a command using the {@link ProcessBuilder} class with default command.
     */
    public static Process executeCommandViaProcessBuilder() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("echo", "hello");
        return processBuilder.start();
    }
}
