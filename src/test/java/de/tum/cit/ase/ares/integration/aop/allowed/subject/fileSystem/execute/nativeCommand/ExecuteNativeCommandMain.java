package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.nativeCommand;

import java.io.IOException;

public class ExecuteNativeCommandMain {

    private ExecuteNativeCommandMain() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system by executing native commands.
     */
    public static void accessFileSystemViaNativeCommand(String filePath) throws IOException {
        Runtime.getRuntime().exec(filePath);
    }
}