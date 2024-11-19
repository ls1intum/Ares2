package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;
import static de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchitectureTestCaseCollection.getArchitectureRuleFileContent;
//</editor-fold>

/**
 * Architecture test case for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaArchUnitSecurityTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    @Nonnull
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    private final Set<String> allowedPackages;

    /**
     * Flag to determine if the error message should be long or parsed for fewer details.
     */
    private final boolean longError;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    /**
     * Constructor for JavaArchUnitSecurityTestCase.
     *
     * @param builder Selects the supported architecture test case in the Java programming language
     */
    private JavaArchUnitSecurityTestCase(Builder builder) {
        if (builder.javaArchitectureTestCaseSupported == null) {
            throw new IllegalArgumentException("javaArchitectureTestCaseSupported must not be null");
        }
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.allowedPackages = builder.allowedPackages;
        this.longError = builder.longError;
    }

    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
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
    public void executeArchitectureTestCase(JavaClasses javaClasses) {
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
            parseErrorMessage(e, longError);
        }
    }
    //</editor-fold>


    // Static method to start the builder
    public static Builder builder() {
        return new Builder();
    }


    // Static Builder class
    public static class Builder {
        private JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;
        private Set<String> allowedPackages = new HashSet<>();
        private boolean longError = false;

        public Builder javaArchitecturalTestCaseSupported(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
            return this;
        }

        public Builder allowedPackages(Set<PackagePermission> packages) {
            if (packages != null) {
                this.allowedPackages = packages.stream()
                        .map(PackagePermission::importTheFollowingPackage)
                        .collect(Collectors.toSet());
            }
            return this;
        }

        public Builder longError(boolean longError) {
            this.longError = longError;
            return this;
        }

        public JavaArchUnitSecurityTestCase build() {
            return new JavaArchUnitSecurityTestCase(this);
        }
    }
}
