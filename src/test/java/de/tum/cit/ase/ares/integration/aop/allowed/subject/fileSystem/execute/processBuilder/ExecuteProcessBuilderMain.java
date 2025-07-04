package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.processBuilder;

import java.io.IOException;

public class ExecuteProcessBuilderMain {

    private ExecuteProcessBuilderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ProcessBuilder} class for execution.
     */
    public static void accessFileSystemViaProcessBuilder(String filePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(filePath);
        processBuilder.start();
    }

    /**
     * Access the file system using the {@link ProcessBuilder} class for execution with default command.
     */
    public static void accessFileSystemViaProcessBuilder() throws IOException {
        accessFileSystemViaProcessBuilder("echo");
    }
}