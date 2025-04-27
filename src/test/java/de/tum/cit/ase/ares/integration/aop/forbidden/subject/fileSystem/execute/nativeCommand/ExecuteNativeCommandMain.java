package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.nativeCommand;

import java.io.IOException;

public class ExecuteNativeCommandMain {

    private ExecuteNativeCommandMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system by executing native commands.
     */
    public static void accessFileSystemViaNativeCommand() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("windows")) {
            Runtime.getRuntime().exec("cmd.exe /c dir");
        } else {
            Runtime.getRuntime().exec("ls -la");
        }
    }
}