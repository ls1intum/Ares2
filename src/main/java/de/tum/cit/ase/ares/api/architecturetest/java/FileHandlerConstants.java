package de.tum.cit.ase.ares.api.architecturetest.java;

import java.io.File;

/**
 * Constants for the path of the files used in the ArchitectureTestCaseStorage
 */
public class FileHandlerConstants {

    public static final String JAVA_FILESYSTEM_INTERACTION_METHODS = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "methods" + File.separator + "file-system-access-methods.txt";
    public static final String JAVA_FILESYSTEM_INTERACTION_CONTENT = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "rules" + File.separator + "file-system-arch-rule.txt";

    private FileHandlerConstants() {
        throw new IllegalArgumentException("FileHandlerConstants is a utility class and should not be instantiated");
    }
}
