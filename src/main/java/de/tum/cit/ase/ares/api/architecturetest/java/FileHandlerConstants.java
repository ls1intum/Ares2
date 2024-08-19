package de.tum.cit.ase.ares.api.architecturetest.java;

import java.io.File;
import java.nio.file.Path;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    public static final Path JAVA_FILESYSTEM_INTERACTION_METHODS = Path.of("src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "methods" + File.separator + "file-system-access-methods.txt");
    public static final Path JAVA_JVM_TERMINATION_METHODS = Path.of("src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "methods" + File.separator + "jvm-termination-methods.txt");

    private FileHandlerConstants() {
        throw new IllegalArgumentException("FileHandlerConstants is a utility class and should not be instantiated");
    }
}
