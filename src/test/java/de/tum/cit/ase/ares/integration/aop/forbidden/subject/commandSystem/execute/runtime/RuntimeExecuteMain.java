package de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime;

import java.io.File;
import java.io.IOException;

public class RuntimeExecuteMain {

    private RuntimeExecuteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Execute a forbidden command using the {@link Runtime} class.
     */
    @SuppressWarnings("deprecation")
    public static Process executeCommandViaRuntime() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("forbidden-command");
    }

    public static Process executeCommandViaRuntimeWithArgs() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("forbidden-command", new String[]{"x"});
    }

    public static Process executeCommandViaRuntimeArray() throws IOException {
       Runtime runtime = Runtime.getRuntime();
       return runtime.exec(new String[]{"forbidden-command"});
    }

    public static Process executeCommandViaRuntimeArrayWithArgs() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("forbidden-command", new String[]{"x"});
    }
}
