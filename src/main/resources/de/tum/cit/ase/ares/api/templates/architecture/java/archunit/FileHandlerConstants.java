package %s.api.architecture.java.archunit;

import java.io.File;
import java.nio.file.Path;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    private static final String JAVA_METHODS_DIRECTORY = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "methods" + File.separator;
    public static final Path JAVA_FILESYSTEM_INTERACTION_METHODS = Path.of(JAVA_METHODS_DIRECTORY + "file-system-access-methods.txt");
    public static final Path JAVA_NETWORK_ACCESS_METHODS = Path.of(JAVA_METHODS_DIRECTORY + "network-access-methods.txt");
    public static final Path JAVA_JVM_TERMINATION_METHODS = Path.of(JAVA_METHODS_DIRECTORY + "jvm-termination-methods.txt");
    public static final Path JAVA_REFLECTION_METHODS = Path.of(JAVA_METHODS_DIRECTORY + "reflection-methods.txt");
    public static final Path JAVA_COMMAND_EXECUTION_METHODS = Path.of(JAVA_METHODS_DIRECTORY + "command-execution-methods.txt");

    private FileHandlerConstants() {
        throw new UnsupportedOperationException("FileHandlerConstants is a utility class and should not be instantiated");
    }
}
