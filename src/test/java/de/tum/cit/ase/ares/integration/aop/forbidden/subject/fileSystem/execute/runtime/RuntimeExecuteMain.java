package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.runtime;

import java.io.IOException;

public class RuntimeExecuteMain {

    private RuntimeExecuteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static Process accessFileSystemViaRuntime() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
    }

    public static Process accessFileSystemViaRuntimeArray() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(new String[]{"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"});
    }
}