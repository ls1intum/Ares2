package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseCollection.getArchitectureRuleFileContent;
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
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

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
    public JavaArchUnitSecurityTestCase(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
        super();
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.allowedPackages = new HashSet<>();
    }

    public JavaArchUnitSecurityTestCase(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported, Set<PackagePermission> packages) {
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
    public void executeArchitectureTestCase(Object classes) {
        // check if instance of object
        if (!(classes instanceof JavaClasses javaClasses)) {
            throw new IllegalArgumentException(localized("security.archunit.invalid.argument"));
        }

        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case PACKAGE_IMPORT ->
                        JavaArchitectureTestCaseCollection
                                .noClassesShouldImportForbiddenPackages(allowedPackages)
                                .check(javaClasses);
                case REFLECTION ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASSES_SHOULD_USE_REFLECTION
                                .check(javaClasses);
                case TERMINATE_JVM ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASSES_SHOULD_TERMINATE_JVM
                                .check(javaClasses);
                case FILESYSTEM_INTERACTION ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM
                                .check(javaClasses);
                case NETWORK_CONNECTION ->
                        JavaArchitectureTestCaseCollection
                                .NO_CLASSES_SHOULD_ACCESS_NETWORK
                                .check(javaClasses);
                case THREAD_CREATION ->
                        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_CREATE_THREADS.check(javaClasses);
                case COMMAND_EXECUTION ->
                        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_EXECUTE_COMMANDS.check(javaClasses);
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            String[] split = null;
            if (e.getMessage() == null || e.getMessage().split("\n").length < 2) {
                throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage()));
            }
            split = e.getMessage().split("\n");
            throw new SecurityException(localized("security.archunit.violation.error", split[0].replaceAll(".*?'(.*?)'.*\r", "$1"), split[1]));
        }
    }
    //</editor-fold>
}
