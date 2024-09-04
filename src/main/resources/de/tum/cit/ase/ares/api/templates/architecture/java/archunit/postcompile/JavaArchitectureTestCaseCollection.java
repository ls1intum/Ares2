package %s.architecture.java.archunit.postcompile;

import com.google.common.collect.ImmutableMap;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import %s.architecture.java.archunit.FileHandlerConstants;
import %s.architecture.java.archunit.JavaArchUnitTestCaseSupported;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static %s.architecture.java.archunit.JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION;
import static %s.architecture.java.archunit.JavaArchUnitTestCaseSupported.NETWORK_CONNECTION;

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
    public static String getArchitectureRuleFileContent(String key) {
        // Construct the path in one step
        Path resolvedPath = Paths.get("de", "tum", "cit", "ase", "ares", "api",
                "templates", "architecture", "java", "archunit", "rules", key + ".txt");

        // Read the file content
        try (InputStream sourceStream = JavaArchitectureTestCaseCollection.class.getResourceAsStream("/" + resolvedPath)) {
            if (sourceStream == null) {
                throw new IOException("Resource not found: " + resolvedPath);
            }

            Scanner scanner = new Scanner(sourceStream, StandardCharsets.UTF_8);
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

        } catch (Exception e) {
            throw new SecurityException("Ares Security Error: Error reading file.", e);
        }
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
                            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS, JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION.name());
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
                            loadForbiddenMethodsFromFile(FileHandlerConstants.JAVA_NETWORK_ACCESS_METHODS, JavaArchUnitTestCaseSupported.NETWORK_CONNECTION.name());
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