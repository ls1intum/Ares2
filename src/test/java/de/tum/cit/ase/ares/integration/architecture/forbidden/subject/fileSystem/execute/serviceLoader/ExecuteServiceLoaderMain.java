package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.execute.serviceLoader;

import java.util.ServiceLoader;

public class ExecuteServiceLoaderMain {

    private ExecuteServiceLoaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ServiceLoader} for execution.
     */
    public static void accessFileSystemViaServiceLoader() {
        ServiceLoader<Runnable> loader = ServiceLoader.load(Runnable.class);
        for (Runnable runnable : loader) {
            runnable.run();
        }
    }
}