package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * Constants for the paths storing the methods that are not allowed to be used in the Java architecture.
 * These methods are used to create the rules for the architecture tests.
 * The paths are used to read the methods from the files.
 */
public class FileHandlerConstants {

    //<editor-fold desc="Java ArchUnit Methods">
    public static final Path ARCHUNIT_FILESYSTEM_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "file-system-access-methods.txt");
    public static final Path ARCHUNIT_NETWORK_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "network-access-methods.txt");
    public static final Path ARCHUNIT_JVM_TERMINATION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "jvm-termination-methods.txt");
    public static final Path ARCHUNIT_REFLECTION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "reflection-methods.txt");
    public static final Path ARCHUNIT_COMMAND_EXECUTION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "command-execution-methods.txt");
    public static final Path ARCHUNIT_THREAD_MANIPULATION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "thread-manipulation-methods.txt");
    public static final Path ARCHUNIT_SERIALIZATION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "serializable-methods.txt");
    public static final Path ARCHUNIT_CLASSLOADER_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "classloader-methods.txt");
    //</editor-fold>

    //<editor-fold desc="WALA Methods">
    public static final Path WALA_CLASSLOADER_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "classloader.txt");
    public static final Path WALA_FILESYSTEM_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "file-system-access-methods.txt");
    public static final Path WALA_NETWORK_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "network-access-methods.txt");
    public static final Path WALA_JVM_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "jvm-termination-methods.txt");
    public static final Path WALA_REFLECTION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "reflection-methods.txt");
    public static final Path WALA_COMMAND_EXECUTION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "command-execution-methods.txt");
    public static final Path WALA_SERIALIZATION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "serializable-methods.txt");
    public static final Path WALA_THREAD_MANIPULATION_METHODS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "thread-manipulation.txt");
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    public static final Path FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS = FileTools.resolveOnPackage("templates", "architecture" , "java", "wala", "false-positives", "false-positives-file.txt");
    //</editor-fold>



    private FileHandlerConstants() {
        throw new SecurityException(localized("security.general.utility.initialization", FileHandlerConstants.class.getName()));
    }
}
