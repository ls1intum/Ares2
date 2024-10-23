package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.JavaArchitectureTestCaseCollection.getArchitectureRuleFileContent;
import static de.tum.cit.ase.ares.api.localization.Messages.localized;
//</editor-fold>

/**
 * Architecture test case for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaArchUnitSecurityTestCase implements ArchitectureSecurityTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    private final Set<String> allowedPackages;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    /**
     * Constructor for JavaArchUnitSecurityTestCase.
     *
     * @param javaArchitectureTestCaseSupported Selects the supported architecture test case in the Java programming language
     */
    public JavaArchUnitSecurityTestCase(@Nonnull JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported) {
        super();
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.allowedPackages = new HashSet<>();
    }

    public JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported, Set<PackagePermission> packages) {
        super();
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.allowedPackages = packages.stream().map(PackagePermission::importTheFollowingPackage).collect(Collectors.toSet());
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    public String writeArchitectureTestCase() {
        try {
            return getArchitectureRuleFileContent(this.javaArchitectureTestCaseSupported.name());
        } catch (AssertionError e) {
            throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): Illegal Statement found: " + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">
    /**
     * Runs the architecture test case in the Java programming language.
     */
    @Override
    public void executeArchitectureTestCase(JavaClasses classes) {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case PACKAGE_IMPORT ->
                        JavaArchitectureTestCaseCollection
                                .noClassesShouldImportForbiddenPackages(allowedPackages)
                                .check(classes);
                case FILESYSTEM_INTERACTION ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM
                                .check(classes);
                case NETWORK_CONNECTION ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASSES_SHOULD_ACCESS_NETWORK
                                .check(classes);
                case THREAD_CREATION ->
                        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_CREATE_THREADS.check(classes);
                case COMMAND_EXECUTION ->
                        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_EXECUTE_COMMANDS.check(classes);
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            if (e.getMessage() == null || e.getMessage().split("\n").length < 2) {
                throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage()));
            }
            throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage().split("\n")[1]));
        }
    }
    //</editor-fold>
}
