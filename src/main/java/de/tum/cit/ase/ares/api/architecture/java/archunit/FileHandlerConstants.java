package de.tum.cit.ase.ares.api.architecture.java.archunit;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {
    
    public static final Path JAVA_FILESYSTEM_INTERACTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "file-system-access-methods.txt");
    public static final Path JAVA_NETWORK_ACCESS_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "network-access-methods.txt");
    public static final Path JAVA_JVM_TERMINATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "jvm-termination-methods.txt");
    public static final Path JAVA_REFLECTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "reflection-methods.txt");
    public static final Path JAVA_COMMAND_EXECUTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "command-execution-methods.txt");
    public static final Path JAVA_THREAD_CREATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "thread-creation-methods.txt");
    public static final Path JAVA_CLASSLOADER_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "classloader-methods.txt");;

    private FileHandlerConstants() {
        throw new UnsupportedOperationException(localized("security.general.utility.initialization"));
    }
}
