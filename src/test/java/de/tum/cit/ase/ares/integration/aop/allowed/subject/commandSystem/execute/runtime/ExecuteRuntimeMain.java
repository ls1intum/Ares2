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

    /**
     * Execute a command using the {@link Runtime} class without parameters.
     */
    @SuppressWarnings("deprecation")
    public static Process executeCommandViaRuntime() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("echo hello");
    }

    /**
     * Execute a command using the {@link Runtime} class with arguments.
     */
    @SuppressWarnings("deprecation")
    public static Process executeCommandViaRuntimeWithArgs() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("echo hello", new String[]{"world"});
    }

    /**
     * Execute a command using the {@link Runtime} class with array parameter.
     */
    public static Process executeCommandViaRuntimeArray() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(new String[]{"echo", "hello"});
    }

    /**
     * Execute a command using the {@link Runtime} class with array and arguments.
     */
    public static Process executeCommandViaRuntimeArrayWithArgs() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(new String[]{"echo", "hello"}, new String[]{"world"});
    }
}
