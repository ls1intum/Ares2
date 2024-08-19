package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.google.common.collect.ImmutableMap;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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
     * Load pre file contents
     */
    public static void loadForbiddenMethodsFromFile(Path filePath, String key) throws IOException {
        Set<String> content = new HashSet<>(Files.readAllLines(filePath));
        FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.put(key, content);
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
    public static String getArchitectureRuleFileContent(String key) throws IOException {
        return Files.readString(Path.of("src" + File.separator + "main" + File.separator + "resources" + File.separator + "archunit" + File.separator + "files" + File.separator + "java" + File.separator + "rules" + File.separator + "%s.txt".formatted(key)));
    }

    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                private Set<String> forbiddenMethods;

                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (forbiddenMethods == null) {
                        try {
                            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS, JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION.name());
                        } catch (IOException e) {
                            throw new IllegalStateException("Could not load the architecture rule file content", e);
                        }
                        forbiddenMethods = getForbiddenMethods(FILESYSTEM_INTERACTION.name());
                    }

                    Optional<Set<String>> methods = Optional.ofNullable(forbiddenMethods);
                    return methods.map(strings -> strings.stream().anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method))).orElse(false);
                }
            }));

    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_NETWORK = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<JavaAccess<?>>("accesses network") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return javaAccess.getTarget().getFullName().startsWith("java.net");
                }
            }));

    public static final ArchRule NO_CLASSES_SHOULD_IMPORT_FORBIDDEN_PACKAGES = ArchRuleDefinition.noClasses()
            .should()
            .transitivelyDependOnClassesThat(new DescribedPredicate<>("imports package") {
                @Override
                public boolean test(JavaClass javaClass) {
                    // Here the classes or packages that it should not depend on did not understand this completely????
                    return false;
                }
            });

    public static final ArchRule NO_CLASSES_SHOULD_USE_REFLECTION = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<JavaAccess<?>>("uses reflection") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return javaAccess.getTarget().getFullName().startsWith("java.lang.reflect");
                }
            }));

    public static final ArchRule NO_CLASSES_SHOULD_TERMINATE_JVM = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition((new DescribedPredicate<>("terminates JVM") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return javaAccess.getTarget().getFullName().startsWith("java.lang.System.exit")
                            || javaAccess.getTarget().getFullName().startsWith("java.lang.Runtime.exit");
                }
            })));
}