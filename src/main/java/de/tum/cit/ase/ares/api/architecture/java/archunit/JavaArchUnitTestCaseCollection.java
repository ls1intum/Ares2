package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;
import static de.tum.cit.ase.ares.api.util.FileTools.readMethodsFromGivenPath;
//</editor-fold>

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchUnitTestCaseCollection {

    //<editor-fold desc="Constructor">
    private JavaArchUnitTestCaseCollection() {
        throw new SecurityException(localize("security.general.utility.initialization", JavaArchUnitTestCaseCollection.class.getName()));
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    /**
     * Get the content of a file from the architectural rules storage
     */
    public static String getArchitectureRuleFileContent(String key) {
        // Construct the path in one step
        Path resolvedPath = Paths.get("de", "tum", "cit", "ase", "ares", "api",
                "templates", "architecture", "java", "archunit", "rules", key + ".txt");

        // Read the file content
        try (InputStream sourceStream = JavaArchUnitTestCaseCollection.class.getResourceAsStream("/" + resolvedPath)) {
            if (sourceStream == null) {
                throw new IOException("Resource not found: " + resolvedPath);
            }

            Scanner scanner = new Scanner(sourceStream, StandardCharsets.UTF_8);
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

        } catch (Exception e) {
            throw new SecurityException("Ares Security Error: Error reading file.", e);
        }
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
                            forbiddenMethods = readMethodsFromGivenPath(methodsFilePath);
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

    //<editor-fold desc="File System related rule">
    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM = createNoClassShouldHaveMethodRule(
            localize("security.architecture.file.system.access"),
            FileHandlerConstants.ARCHUNIT_FILESYSTEM_INTERACTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">
    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASSES_SHOULD_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
            localize("security.architecture.network.access"),
            FileHandlerConstants.ARCHUNIT_NETWORK_ACCESS_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">
    public static final ArchRule NO_CLASSES_SHOULD_CREATE_THREADS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.manipulate.threads"),
            FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">
    public static final ArchRule NO_CLASSES_SHOULD_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.execute.command"),
            FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Package Import related rule">
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
                .as(localize("security.architecture.package.import"));
    }
    //</editor-fold>

    //<editor-fold desc="Reflection related rule">
    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASSES_SHOULD_USE_REFLECTION = createNoClassShouldHaveMethodRule(
            localize("security.architecture.reflection.uses"),
            FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">
    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASSES_SHOULD_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
            localize("security.architecture.terminate.jvm"),
            FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Serialization related rule">
    public static final ArchRule NO_CLASSES_SHOULD_SERIALIZE = createNoClassShouldHaveMethodRule(
            localize("security.architecture.serialize"),
            FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="ClassLoader related rule">
    public static final ArchRule NO_CLASSES_SHOULD_USE_CLASSLOADERS = createNoClassShouldHaveMethodRule(
            "uses ClassLoaders",
            FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS
    );
    //</editor-fold>
}