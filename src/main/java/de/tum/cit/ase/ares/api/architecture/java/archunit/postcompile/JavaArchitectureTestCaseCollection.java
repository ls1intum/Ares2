package de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecture.java.archunit.FileHandlerConstants;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchitectureTestCaseCollection {
    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM = createNoClassShouldHaveMethodRule(
            "Accesses file system",
            FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">
    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
            "Accesses network",
            FileHandlerConstants.JAVA_NETWORK_ACCESS_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">

    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASSES_SHOULD_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
            "Terminates JVM",
            FileHandlerConstants.JAVA_JVM_TERMINATION_METHODS
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
     * Get the content of a file from the architectural rules storage
     */
    public static Set<String> getForbiddenMethods(Path filePath) {
        return new HashSet<>(List.of(FileTools.readFile(filePath).split("\r\n")));
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
                .dependOnClassesThat(new DescribedPredicate<>("imports a forbidden package package") {
                    @Override
                    public boolean test(JavaClass javaClass) {
                        return allowedPackages.stream().noneMatch(allowedPackage -> javaClass.getPackageName().startsWith(allowedPackage));
                    }
                })
                .as("Imports forbidden packages");
    }

    private static ArchRule createNoClassShouldHaveMethodRule(
            String ruleName,
            Path methodsFilePath
    ) {
        return ArchRuleDefinition.noClasses()
                .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
                    private Set<String> forbiddenMethods;

                    @Override
                    public boolean test(JavaAccess<?> javaAccess) {
                        if (forbiddenMethods == null) {
                            forbiddenMethods = getForbiddenMethods(methodsFilePath);
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
                }))
                .as(ruleName);
    }
    //</editor-fold>

    //<editor-fold desc="Reflection related rule">
    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASSES_SHOULD_USE_REFLECTION = createNoClassShouldHaveMethodRule(
            "Uses Reflection",
            FileHandlerConstants.JAVA_REFLECTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">
    public static final ArchRule NO_CLASSES_SHOULD_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
            "Executes commands",
            FileHandlerConstants.JAVA_COMMAND_EXECUTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">
    public static final ArchRule NO_CLASSES_SHOULD_CREATE_THREADS = createNoClassShouldHaveMethodRule(
            "Manipulates threads",
            FileHandlerConstants.JAVA_THREAD_CREATION_METHODS
    );
    //</editor-fold>
}