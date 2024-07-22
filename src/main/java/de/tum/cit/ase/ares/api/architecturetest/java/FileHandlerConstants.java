package de.tum.cit.ase.ares.api.architecturetest.java;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    private FileHandlerConstants() {
        throw new IllegalArgumentException("FileHandlerConstants is a utility class and should not be instantiated");
    }

    public static final String JAVA_FILESYSTEM_INTERACTION_METHODS = "src/main/resources/archunit/files/java/file-system-access-methods.txt";
    public static final String JAVA_FILESYSTEM_INTERACTION_CONTENT = "src/main/resources/archunit/files/java/file-system-arch-rule.txt";
}
