package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import java.nio.file.Path;
import java.util.Set;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.util.FileTools.readMethodsFromGivenPath;
//</editor-fold>

/**
 * Collection of security test cases that analyze Java applications using WALA framework.
 * This class provides static methods to verify that analyzed code does not:
 * - Use reflection
 * - Access file system
 * - Access network
 * - Terminate JVM
 * - Execute system commands
 * - Create threads
 */
public class JavaWalaTestCaseCollection {

    //<editor-fold desc="Constructor">
    private JavaWalaTestCaseCollection() {
        throw new SecurityException(localize("security.general.utility.initialization", JavaWalaTestCaseCollection.class.getName()));
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Creates a rule that checks if a class has a forbidden method.
     */
    private static WalaRule createNoClassShouldHaveMethodRule(
            String ruleName,
            Path methodsFilePath
    ) {
        return new WalaRule(ruleName, readMethodsFromGivenPath(methodsFilePath));
    }
    //</editor-fold>

    //<editor-fold desc="Dynamic rules">
    //<editor-fold desc="File System related rule">

    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final WalaRule NO_CLASS_MUST_ACCESS_FILE_SYSTEM = createNoClassShouldHaveMethodRule(
            localize("security.architecture.file.system.access"),
            FileHandlerConstants.WALA_FILESYSTEM_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">

    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final WalaRule NO_CLASS_MUST_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
            localize("security.architecture.network.access"),
            FileHandlerConstants.WALA_NETWORK_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">

    /**
     * This method checks if any class in the given package creates threads.
     */
    public static final WalaRule NO_CLASS_MUST_CREATE_THREADS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.manipulate.threads"),
            FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">

    /**
     * This method checks if any class in the given package executes commands.
     */
    public static final WalaRule NO_CLASS_MUST_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.execute.command"),
            FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS
    );
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Semi-dynamic rules">
    //<editor-fold desc="Package Import related rule">

    /**
     * This method checks if any class in the given package imports forbidden packages.
     */
    public static void noClassMustImportForbiddenPackages(JavaClasses javaClasses, Set<PackagePermission> allowedPackages) {
        JavaArchUnitTestCaseCollection
                .noClassMustImportForbiddenPackages(allowedPackages)
                .check(javaClasses);
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Static rules">
    //<editor-fold desc="Reflection related rule">

    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final WalaRule NO_CLASS_MUST_USE_REFLECTION = createNoClassShouldHaveMethodRule(
            localize("security.architecture.reflection.uses"),
            FileHandlerConstants.WALA_REFLECTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">

    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final WalaRule NO_CLASS_MUST_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
            localize("security.architecture.terminate.jvm"),
            FileHandlerConstants.WALA_JVM_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Serialization related rule">

    /**
     * This method checks if any class in the given package uses serialization.
     */
    public static final WalaRule NO_CLASS_MUST_SERIALIZE = createNoClassShouldHaveMethodRule(
            localize("security.architecture.serialize"),
            FileHandlerConstants.WALA_SERIALIZATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Class Loading related rule">
    /**
     * This method checks if any class in the given package uses class loaders.
     */
    public static final WalaRule NO_CLASS_MUST_USE_CLASSLOADERS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.class.loading"),
            FileHandlerConstants.WALA_CLASSLOADER_METHODS
    );
    //</editor-fold>
    //</editor-fold>

}
