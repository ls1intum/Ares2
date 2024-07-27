package de.tum.cit.ase.ares.api.architecturetest.java;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Storage for the architecture test cases
 */
public class ArchitectureTestCaseStorage { // TODO: What is the reason to separate JavaArchitectureTestCase and ArchitectureTestCaseStorage into two different classes?

    private ArchitectureTestCaseStorage() {
        throw new IllegalArgumentException("Do not instantiate this class");
    }

    /**
     * Map to store the forbidden methods for the supported architectural test cases
     */
    public static final ImmutableMap.Builder<String, Set<String>> FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE = ImmutableMap.builder();

    /**
     * Map to store the content of the architecture test case files
     */
    public static final ImmutableMap.Builder<String, String> ARCHITECTURAL_RULES_CONTENT_MAP = ImmutableMap.builder();

    /**
     * Load the content of the architecture test case files
     */
    static { // TODO: Can we postpone doing this until we are sure it is needed (as it is a quite costly operation)?
        try {
            // Add further for other supported architecture test cases
            loadArchitectureRuleFileContent(FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_CONTENT, JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION.name());
            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS, JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION.name());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Load pre file contents
     */
    private static void loadForbiddenMethodsFromFile(String filePath, String key) throws IOException {
        Set<String> content = new HashSet<>(Files.readAllLines(Path.of(filePath)));
        FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.put(key, content);
    }

    /**
     * Load the content of the architecture test case files
     */
    private static void loadArchitectureRuleFileContent(String filePath, String key) throws IOException {
        String content = Files.readString(Path.of(filePath));
        ARCHITECTURAL_RULES_CONTENT_MAP.put(key, content);
    }

    //TODO: What is the difference between ForbiddenMethods and ArchitectureRuleFileContent?

    /**
     * Get the content of a file from the architectural rules storage
     */
    public static Set<String> getForbiddenMethods(String key) {
        return FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.build().get(key);
    }

    /**
     * Get the content of a file from the architectural rules storage
     */
    public static String getArchitectureRuleFileContent(String key) {
        return ARCHITECTURAL_RULES_CONTENT_MAP.build().get(key);
    }
}
