package de.tum.cit.ase.ares.api.archunit;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArchitectureRulesStorage {

    private ArchitectureRulesStorage() {
        throw new AssertionError("This class should not be instantiated");
    }

    private static final Map<String, Set<String>> architecturalRules = new HashMap<>();

    public static final String PATH_TO_FORBIDDEN_METHODS = "src/main/resources/archunit/files/file-system-access-methods.txt";

    public static final String TEST_FILE_NAME = "FileSystemSecurityRulesTest.java";

    static {
        try {
            loadFileContents(PATH_TO_FORBIDDEN_METHODS, TEST_FILE_NAME);
            // Add more files and keys as needed
        } catch (IOException e) {
            throw new AssertionError("Failed to initialize secure sandbox rules, contact a supervisor");
        }
    }

    /**
     * Load the contents of a file into the architectural rules storage
     */
    private static void loadFileContents(String filePath, String key) throws IOException {
        Files.readAllLines();
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        architecturalRules.put(key, content);
    }

    /**
     * Get the content of a file from the architectural rules storage
     */
    public static String getFileContent(String key) {
        return architecturalRules.get(key);
    }
}
