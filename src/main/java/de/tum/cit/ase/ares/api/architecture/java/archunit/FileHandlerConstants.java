package de.tum.cit.ase.ares.api.architecture.java.archunit;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {
    
    public static final Path JAVA_FILESYSTEM_INTERACTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "file-system-access-methods.txt");
    public static final Path JAVA_NETWORK_ACCESS_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "network-access-methods.txt");
    public static final Path JAVA_JVM_TERMINATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "jvm-termination-methods.txt");
    public static final Path JAVA_REFLECTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "reflection-methods.txt");
    public static final Path JAVA_COMMAND_EXECUTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "command-execution-methods.txt");

    private FileHandlerConstants() {
        throw new UnsupportedOperationException("Ares Security Error (Reason: Ares-Code; Stage: Execution): FileHandlerConstants is a utility class and should not be instantiated.");
    }
}
