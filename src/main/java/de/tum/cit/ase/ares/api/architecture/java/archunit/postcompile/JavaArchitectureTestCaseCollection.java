package de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile;

import com.google.common.collect.ImmutableMap;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecture.java.archunit.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION;
import static de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported.NETWORK_CONNECTION;

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchitectureTestCaseCollection {

    //<editor-fold desc="Attributes">
    public static final String LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED = "Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not load the architecture rule file content";
    /**
     * Map to store the forbidden methods for the supported architectural test cases
     */
    private static final ImmutableMap.Builder<String, Set<String>> FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE = ImmutableMap.builder();


    //<editor-fold desc="File System Interactions related rule">
    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM = createNoClassShoudHaveMethodRule(
            "accesses file system",
            FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS,
            JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION.name(),
            FILESYSTEM_INTERACTION.name()
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">
    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_NETWORK = createNoClassShoudHaveMethodRule(
            "accesses network",
            FileHandlerConstants.JAVA_NETWORK_ACCESS_METHODS,
            JavaArchUnitTestCaseSupported.NETWORK_CONNECTION.name(),
            NETWORK_CONNECTION.name()
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">

    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASSES_SHOULD_TERMINATE_JVM = createNoClassShoudHaveMethodRule(
            "terminates JVM",
            FileHandlerConstants.JAVA_JVM_TERMINATION_METHODS,
            "JVM_TERMINATION",
            "JVM_TERMINATION"
    );
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Constructor">
    private JavaArchitectureTestCaseCollection() {
        throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaArchitectureTestCaseCollection is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

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

    private static ArchRule createNoClassShoudHaveMethodRule(
            String ruleName,
            Path methodsFilePath,
            String javaArchUnitTestCaseSupportedName,
            String forbiddenMethodsKey
    ) {
        return ArchRuleDefinition.noClasses()
                .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
                    // TODO: When is this not null?
                    private Set<String> forbiddenMethods;
                    @Override
                    public boolean test(JavaAccess<?> javaAccess) {
                        if (forbiddenMethods == null) {
                            try {
                                loadForbiddenMethodsFromFile(methodsFilePath, javaArchUnitTestCaseSupportedName);
                            } catch (IOException e) {
                                throw new IllegalStateException(LOAD_FORBIDDEN_METHODS_FROM_FILE_FAILED, e);
                            }
                            forbiddenMethods = getForbiddenMethods(forbiddenMethodsKey);
                        }
                        return forbiddenMethods
                                .stream()
                                .anyMatch(
                                        method -> javaAccess
                                                .getTarget()
                                                .getFullName()
                                                .startsWith(method)
                                );
                    }
                }));
    }
    //</editor-fold>

    //<editor-fold desc="Reflection related rule">
    // TODO: Adjust it to make it work with createNoClassShoudHaveMethodRule
    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASSES_SHOULD_USE_REFLECTION = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("uses reflection") {
                private final Set<String> forbiddenPackages = Set.of("java.lang.reflect", "sun.reflect.misc");

                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return forbiddenPackages
                            .stream()
                            .anyMatch(
                                    forbiddenPackage -> javaAccess
                                            .getTarget()
                                            .getFullName()
                                            .startsWith(forbiddenPackage)
                            );
                }
            }));
    //</editor-fold>
}