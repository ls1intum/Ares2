package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    //<editor-fold desc="Java ArchUnit Methods">
    public static final Path JAVA_FILESYSTEM_INTERACTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "file-system-access-methods.txt");
    public static final Path JAVA_NETWORK_ACCESS_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "network-access-methods.txt");
    public static final Path JAVA_JVM_TERMINATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "jvm-termination-methods.txt");
    public static final Path JAVA_REFLECTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "reflection-methods.txt");
    public static final Path JAVA_COMMAND_EXECUTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "command-execution-methods.txt");
    public static final Path JAVA_THREAD_MANIPULATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "thread-manipulation-methods.txt");
    public static final Path JAVA_SERIALIZATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "archunit", "methods", "serializable-methods.txt");
    //</editor-fold>

    //<editor-fold desc="WALA Methods">
    public static final Path WALA_CLASSLOADER_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "classloader.txt");
    public static final Path WALA_FILESYSTEM_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "file-system-access-methods.txt");
    public static final Path WALA_NETWORK_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "network-access-methods.txt");
    public static final Path WALA_JVM_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "jvm-termination-methods.txt");
    public static final Path WALA_REFLECTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "reflection.txt");
    public static final Path WALA_COMMAND_EXECUTION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "command-execution-methods.txt");
    public static final Path WALA_SERIALIZATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "serializable-methods.txt");
    public static final Path WALA_THREAD_MANIPULATION_METHODS = FileTools.resolveOnResources("templates", "architecture" , "java", "wala", "methods", "thread-manipulation.txt");
    //</editor-fold>



    private FileHandlerConstants() {
        throw new UnsupportedOperationException(localized("security.general.utility.initialization"));
    }
}
