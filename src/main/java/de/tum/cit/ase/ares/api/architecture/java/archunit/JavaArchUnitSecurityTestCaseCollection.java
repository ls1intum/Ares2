package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.wala.ReachabilityChecker;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import java.nio.file.Path;
import java.util.Set;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.util.FileTools.readMethodsFromGivenPath;
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
public class JavaArchUnitSecurityTestCaseCollection {

    //<editor-fold desc="Constructor">
    private JavaArchUnitSecurityTestCaseCollection() {
        throw new SecurityException(localize("security.general.utility.initialization", JavaArchUnitSecurityTestCaseCollection.class.getName()));
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
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
            localize("security.architecture.file.system.access"),
            FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">
    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final ArchRule NO_CLASS_MUST_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
            localize("security.architecture.network.access"),
            FileHandlerConstants.ARCHUNIT_NETWORK_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">
    /**
     * This method checks if any class in the given package creates threads.
     */
    public static final ArchRule NO_CLASS_MUST_CREATE_THREADS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.manipulate.threads"),
            FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">
    /**
     * This method checks if any class in the given package executes commands.
     */
    public static final ArchRule NO_CLASS_MUST_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.execute.command"),
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
                .as(localize("security.architecture.package.import"));
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Static rules">
    //<editor-fold desc="Reflection related rule">
    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final ArchRule NO_CLASS_MUST_USE_REFLECTION = createNoClassShouldHaveMethodRule(
            localize("security.architecture.reflection.uses"),
            FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">
    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final ArchRule NO_CLASS_MUST_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
            localize("security.architecture.terminate.jvm"),
            FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Serialization related rule">
    /**
     * This method checks if any class in the given package uses serialization.
     */
    public static final ArchRule NO_CLASS_MUST_SERIALIZE = createNoClassShouldHaveMethodRule(
            localize("security.architecture.serialize"),
            FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS
    );
    //</editor-fold>

    //<editor-fold desc="Class Loading related rule">
    /**
     * This method checks if any class in the given package uses class loaders.
     */
    public static final ArchRule NO_CLASS_MUST_USE_CLASSLOADERS = createNoClassShouldHaveMethodRule(
            localize("security.architecture.class.loading"),
            FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS
    );
    //</editor-fold>
    //</editor-fold>
}