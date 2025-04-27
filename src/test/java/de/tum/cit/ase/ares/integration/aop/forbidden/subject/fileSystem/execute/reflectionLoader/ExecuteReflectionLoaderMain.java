package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.reflectionLoader;

import java.lang.reflect.Method;

public class ExecuteReflectionLoaderMain {

    private ExecuteReflectionLoaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using reflection for execution.
     */
    public static void accessFileSystemViaReflectionLoader() throws Exception {
        Class<?> clazz = Class.forName("com.example.ExecutableClass");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod("execute");
        method.invoke(instance);
    }
}