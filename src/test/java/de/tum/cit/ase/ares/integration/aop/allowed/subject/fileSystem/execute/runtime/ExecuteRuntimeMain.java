package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.runtime;

import java.io.IOException;

public class ExecuteRuntimeMain {

    private ExecuteRuntimeMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Runtime} class for execution.
     */
    public static void accessFileSystemViaRuntime(String filePath) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(filePath);
        // We could also wait for the process to complete
        // process.waitFor();
    }
}