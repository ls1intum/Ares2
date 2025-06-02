package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.execute.urlClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ExecuteURLClassLoaderMain {

    private ExecuteURLClassLoaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link URLClassLoader} for execution.
     */
    public static void accessFileSystemViaURLClassLoader() throws Exception {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        URL url = file.toURI().toURL();
        
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{url})) {
            Class<?> loadedClass = classLoader.loadClass("com.example.CustomExecutable");
            Object instance = loadedClass.getDeclaredConstructor().newInstance();
            Method method = loadedClass.getMethod("execute");
            method.invoke(instance);
        }
    }
}