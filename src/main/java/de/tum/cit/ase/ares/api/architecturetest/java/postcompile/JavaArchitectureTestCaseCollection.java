package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.google.common.collect.ImmutableMap;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchitectureTestCaseCollection {

    private JavaArchitectureTestCaseCollection() {
        throw new IllegalArgumentException("This class should not be instantiated");
    }

    /**
     * Map to store the forbidden methods for the supported architectural test cases
     */
    private static final ImmutableMap.Builder<String, Set<String>> FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE = ImmutableMap.builder();

    /**
     * Map to store the content of the architecture test case files
     */
    private static final ImmutableMap.Builder<String, String> ARCHITECTURAL_RULES_CONTENT_MAP = ImmutableMap.builder();

    /**
     * The packages that should not be accessed by the student submission.
     */
    private static final List<String> BANNED_FILESYSTEM_ACCESS_PACKAGES = List.of(
            "java.nio.file",
            "java.util.prefs",
            "sun.print",
            "sun.security",
            "java.util.jar",
            "java.util.zip",
            "sun.awt.X11",
            "javax.imageio",
            "javax.sound.midi",
            "javax.swing.filechooser",
            "java.awt.desktop");

    /**
     * Load pre file contents
     */
    public static void loadForbiddenMethodsFromFile(Path filePath, String key) throws IOException {
        Set<String> content = new HashSet<>(Files.readAllLines(filePath));
        FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.put(key, content);
    }

    /**
     * Load the content of the architecture test case files
     */
    public static void loadArchitectureRuleFileContent(Path filePath, String key) throws IOException {
        String content = Files.readString(filePath);
        ARCHITECTURAL_RULES_CONTENT_MAP.put(key, content);
    }

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

    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (BANNED_FILESYSTEM_ACCESS_PACKAGES.stream().anyMatch(p -> javaAccess.getTarget().getFullName().startsWith(p))) {
                        return true;
                    }

                    Optional<Set<String>> bannedMethods = Optional.ofNullable(JavaArchitectureTestCaseCollection.getForbiddenMethods(FILESYSTEM_INTERACTION.name()));
                    return bannedMethods.map(strings -> strings.contains(javaAccess.getTarget().getName())).orElse(false);
                }
            }, FILESYSTEM_INTERACTION));
}