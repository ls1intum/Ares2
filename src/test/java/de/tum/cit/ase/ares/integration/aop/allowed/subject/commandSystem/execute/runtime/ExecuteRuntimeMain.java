package de.tum.cit.ase.ares.integration.aop.allowed.subject.commandSystem.execute.runtime;

import java.io.IOException;

public class ExecuteRuntimeMain {

    private ExecuteRuntimeMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Execute a command using the {@link Runtime} class.
     */
    @SuppressWarnings("deprecation")
    public static Process executeCommandViaRuntime(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(command);
    }
}
