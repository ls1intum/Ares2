package de.tum.cit.ase.ares.api.architecturetest;

import com.google.common.collect.ImmutableMap;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ArchitectureTestCaseStorage {

    private ArchitectureTestCaseStorage() {
        throw new IllegalArgumentException("Util class");
    }

    public static final ImmutableMap.Builder<String, Set<String>> FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE = ImmutableMap.builder();

    public static final ImmutableMap.Builder<String, String> ARCHITECTURAL_RULES_CONTENT_MAP = ImmutableMap.builder();

    static {
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
     * Get the content of a file from the architectural rules storage
     */
    private static Set<String> getForbiddenMethods(String key) {
        return FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.build().get(key);
    }

    private static void loadArchitectureRuleFileContent(String filePath, String key) throws IOException {
        String content = Files.readString(Path.of(filePath));
        ARCHITECTURAL_RULES_CONTENT_MAP.put(key, content);
    }

    /**
     * Get the content of a file from the architectural rules storage
     */
    private static String getArchitectureRuleFileContent(String key) {
        return ARCHITECTURAL_RULES_CONTENT_MAP.build().get(key);
    }
}
