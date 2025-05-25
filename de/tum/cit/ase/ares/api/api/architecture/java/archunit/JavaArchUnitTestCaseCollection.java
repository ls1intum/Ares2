package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
//</editor-fold>

/**
 * Collection of security test cases that analyze Java applications using ArchUnit framework.
 * This class provides static methods to verify that analyzed code does not:
 * - Use reflection
 * - Access file system
 * - Access network
 * - Terminate JVM
 * - Execute system commands
 * - Create threads
 */
public class JavaArchUnitTestCaseCollection {

    //<editor-fold desc="Constructor">
    private JavaArchUnitTestCaseCollection() {
        throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.general.utility.initialization", JavaArchUnitTestCaseCollection.class.getName()));
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    // Taken from FileTools, as we do not want to load the whole class in pre-compile mode
    /**
     * Reads the content of a file from the specified path.
     * <p>
     * This method reads the content of a file from the given source file path and returns it as a string.
     * Various exceptions that might occur during the file read process are caught and wrapped in a
     * {@link SecurityException}.
     * </p>
     *
     * @param sourceFilePath the {@link Path} of the file to read.
     * @return the content of the file as a {@link String}.
     * @throws SecurityException if an error occurs during the file read process.
     */
    public static String readFile(Path sourceFilePath) {
        try {

            InputStream sourceStream = JavaArchUnitTestCaseCollection.class.getResourceAsStream("/" + sourceFilePath.toString());

            if (sourceStream == null) {
                throw new IOException("Resource not found: " + sourceFilePath);
            }

            Scanner scanner = new Scanner(sourceStream, StandardCharsets.UTF_8);

            // Check if the scanner has any content
            if (scanner.hasNext()) {
                return scanner.useDelimiter("\\A").next();
            } else {
                return "";  // Return an empty string if the file is empty
            }

        } catch (IOException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.file-tools.read.content.failure"), e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Illegal format in content.", e);
        }
    }

    // Taken from FileTools, as we do not want to load the whole class in pre-compile mode
    /**
     * Reads the content of a file and returns it as a set of strings.
     * @param filePath The path to the file
     * @return a set of strings representing the content of the file
     */
    public static Set<String> readMethodsFromGivenPath(Path filePath) {
        String fileContent = readFile(filePath);
        String normalizedContent = fileContent.replace("\r\n", "\n").replace("\r", "\n");
        List<String> methods = Arrays.stream(normalizedContent.split("\n"))
                // Filter out comments
                .filter(str -> !str.startsWith("#"))
                .toList();
        return new HashSet<>(methods);
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

    //<editor-fold desc="Dynamic rules">
    //<editor-fold desc="File System related rule">
    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_MUST_ACCESS_FILE_SYSTEM = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.file.system.access"),
            FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">
    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASS_MUST_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.network.access"),
            FileHandlerConstants.ARCHUNIT_NETWORK_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">
    /**
     * This method checks if any class in the given package creates threads.
     */
    public static final ArchRule NO_CLASS_MUST_CREATE_THREADS = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.manipulate.threads"),
            FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">
    /**
     * This method checks if any class in the given package executes commands.
     */
    public static final ArchRule NO_CLASS_MUST_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.execute.command"),
            FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS
    );
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Semi-dynamic rules">
    //<editor-fold desc="Package Import related rule">
    /**
     * This method checks if any class in the given package imports forbidden packages.
     */
    public static ArchRule noClassMustImportForbiddenPackages(Set<PackagePermission> allowedPackages) {
        return ArchRuleDefinition.noClasses()
                .should()
                .dependOnClassesThat(new DescribedPredicate<>("imports a forbidden package package") {
                    @Override
                    public boolean test(JavaClass javaClass) {
                        return allowedPackages.stream().noneMatch(allowedPackage -> javaClass.getPackageName().startsWith(allowedPackage.importTheFollowingPackage()));
                    }
                })
                .as(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.package.import"));
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Static rules">
    //<editor-fold desc="Reflection related rule">
    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASS_MUST_USE_REFLECTION = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.reflection.uses"),
            FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">
    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASS_MUST_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.terminate.jvm"),
            FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Serialization related rule">
    /**
     * This method checks if any class in the given package uses serialization.
     */
    public static final ArchRule NO_CLASS_MUST_SERIALIZE = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.serialize"),
            FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Class Loading related rule">
    /**
     * This method checks if any class in the given package uses class loaders.
     */
    public static final ArchRule NO_CLASS_MUST_USE_CLASSLOADERS = createNoClassShouldHaveMethodRule(
            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.loading"),
            FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS
    );
    //</editor-fold>
    //</editor-fold>
}