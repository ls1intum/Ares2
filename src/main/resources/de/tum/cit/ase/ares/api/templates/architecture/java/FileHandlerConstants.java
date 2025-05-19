package %s.api.architecture.java;

import %s.api.localization.Messages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Constants for the paths storing the methods that are not allowed to be used in the Java architecture.
 * These methods are used to create the rules for the architecture tests.
 * The paths are used to read the methods from the files.
 */
public class FileHandlerConstants {

    //<editor-fold desc="Tools">
    /**
     * Resolves a path based on the target and additional path parts.
     *
     * @param target the base path to resolve.
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnTarget(Path target, String... furtherPathParts) {
        return Stream
                .of(furtherPathParts)
                .reduce(target, Path::resolve, Path::resolve);
    }

    /**
     * Resolves a path based on "de/tum/cit/ase/ares/api" and additional path parts.
     *
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnPackage(String... furtherPathParts) {
        Path target = Paths.get("de","tum","cit","ase","ares","api");
        return resolveOnTarget(target, furtherPathParts);
    }
    //</editor-fold>

    //<editor-fold desc="Java ArchUnit Methods">
    public static final Path ARCHUNIT_FILESYSTEM_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "file-system-access-methods.txt");
    public static final Path ARCHUNIT_NETWORK_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "network-access-methods.txt");
    public static final Path ARCHUNIT_JVM_TERMINATION_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "jvm-termination-methods.txt");
    public static final Path ARCHUNIT_REFLECTION_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "reflection-methods.txt");
    public static final Path ARCHUNIT_COMMAND_EXECUTION_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "command-execution-methods.txt");
    public static final Path ARCHUNIT_THREAD_MANIPULATION_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "thread-manipulation-methods.txt");
    public static final Path ARCHUNIT_SERIALIZATION_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "serializable-methods.txt");
    public static final Path ARCHUNIT_CLASSLOADER_METHODS = resolveOnPackage("templates", "architecture" , "java", "archunit", "methods", "classloader-methods.txt");
    //</editor-fold>

    //<editor-fold desc="WALA Methods">
    public static final Path WALA_CLASSLOADER_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "classloader-methods.txt");
    public static final Path WALA_FILESYSTEM_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "file-system-access-methods.txt");
    public static final Path WALA_NETWORK_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "network-access-methods.txt");
    public static final Path WALA_JVM_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "jvm-termination-methods.txt");
    public static final Path WALA_REFLECTION_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "reflection-methods.txt");
    public static final Path WALA_COMMAND_EXECUTION_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "command-execution-methods.txt");
    public static final Path WALA_SERIALIZATION_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "serializable-methods.txt");
    public static final Path WALA_THREAD_MANIPULATION_METHODS = resolveOnPackage("templates", "architecture" , "java", "wala", "methods", "thread-manipulation-methods.txt");
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    public static final Path FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS = resolveOnPackage("templates", "architecture" , "java", "wala", "false-positives", "false-positives-file.txt");
    //</editor-fold>



    private FileHandlerConstants() {
        throw new SecurityException(Messages.localized("security.general.utility.initialization", FileHandlerConstants.class.getName()));
    }
}
