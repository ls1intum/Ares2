package de.tum.cit.ase.ares.api.architecturetest.java;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    public static final String JAVA_FILESYSTEM_INTERACTION_METHODS = "src/main/resources/archunit/files/java/methods/file-system-access-methods.txt"; // TODO: Remove slash (will not work on Windows)
    public static final String JAVA_FILESYSTEM_INTERACTION_CONTENT = "src/main/resources/archunit/files/java/rules/file-system-arch-rule.txt"; // TODO: Remove slash (will not work on Windows)

    private FileHandlerConstants() {
        throw new IllegalArgumentException("FileHandlerConstants is a utility class and should not be instantiated");
    }
}
