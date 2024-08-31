package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.google.common.collect.ImmutableMap;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;
import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.NETWORK_CONNECTION;

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchitectureTestCaseCollection {

    private JavaArchitectureTestCaseCollection() {
        throw new IllegalArgumentException("This class should not be instantiated");
    }

    public static final String LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED = "Could not load the architecture rule file content";
    /**
     * Map to store the forbidden methods for the supported architectural test cases
     */
    private static final ImmutableMap.Builder<String, Set<String>> FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE = ImmutableMap.builder();


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
        return Files.readString(Paths.get("src", "main", "resources", "archunit", "files", "java", "rules", "%s.txt".formatted(key)));
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
                            throw new IllegalStateException(LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED, e);
                        }
                        forbiddenMethods = getForbiddenMethods(FILESYSTEM_INTERACTION.name());
                    }

                    return forbiddenMethods.stream().anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
                }
            }));

    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_NETWORK = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses network") {
                private Set<String> forbiddenMethods;

                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (forbiddenMethods == null) {
                        try {
                            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_NETWORK_ACCESS_METHODS, JavaSupportedArchitectureTestCase.NETWORK_CONNECTION.name());
                        } catch (IOException e) {
                            throw new IllegalStateException(LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED, e);
                        }
                        forbiddenMethods = getForbiddenMethods(NETWORK_CONNECTION.name());
                    }

                    return forbiddenMethods.stream().anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
                }
            }));

    /**
     * This method checks if any class in the given package imports forbidden packages.
     */
    public static ArchRule noClassesShouldImportForbiddenPackages(Set<String> allowedPackages) {
        return ArchRuleDefinition.noClasses()
                .should()
                .transitivelyDependOnClassesThat(new DescribedPredicate<>("imports package") {
                    @Override
                    public boolean test(JavaClass javaClass) {
                        return allowedPackages.stream().allMatch(allowedPackage -> allowedPackage.startsWith(javaClass.getPackageName()));
                    }
                });
    }

    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASSES_SHOULD_USE_REFLECTION = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("uses reflection") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return javaAccess.getTarget().getFullName().startsWith("java.lang.reflect")
                            || javaAccess.getTarget().getFullName().startsWith("sun.reflect.misc");
                }
            }));

    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASSES_SHOULD_TERMINATE_JVM = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition((new DescribedPredicate<>("terminates JVM") {
                private Set<String> forbiddenMethods;

                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (forbiddenMethods == null) {
                        try {
                            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_JVM_TERMINATION_METHODS, "JVM_TERMINATION");
                        } catch (IOException e) {
                            throw new IllegalStateException(LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED, e);
                        }
                        forbiddenMethods = getForbiddenMethods("JVM_TERMINATION");
                    }

                    return forbiddenMethods.stream().anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
                }
            })));
}