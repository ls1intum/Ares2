package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import com.google.common.base.Preconditions;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
//</editor-fold>

/**
 * Architecture test case for the Java programming language using ArchUnit and concrete product of the abstract factory design pattern.
 *
 * @author Sarp Sahinalp
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
    private final JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    @Nonnull
    private final Set<PackagePermission> allowedPackages;


    @Nonnull
    private final JavaClasses javaClasses;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    public JavaArchUnitSecurityTestCase(
            @Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull JavaClasses javaClasses
    ) {
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.allowedPackages = allowedPackages;
        this.javaClasses = javaClasses;
    }

    //</editor-fold>

    //<editor-fold desc="Write security test case methods">

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Nonnull
    public String writeArchitectureTestCase() {
        try {
            return FileTools.readRuleFile(
                    Paths.get("de", "tum", "cit", "ase", "ares", "api",
                            "templates", "architecture", "java", "archunit", "rules", this.javaArchitectureTestCaseSupported.name() + ".txt")
            ).stream().reduce("", (acc, line) -> acc + line + "\n");
        } catch (AssertionError | IOException e) {
            throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): Illegal Statement found: " + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Executes the architecture test case.
     */
    public void executeArchitectureTestCase() {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case FILESYSTEM_INTERACTION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_ACCESS_FILE_SYSTEM
                        .check(javaClasses);
                case NETWORK_CONNECTION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_ACCESS_NETWORK
                        .check(javaClasses);
                case THREAD_CREATION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_CREATE_THREADS
                        .check(javaClasses);
                case COMMAND_EXECUTION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_EXECUTE_COMMANDS
                        .check(javaClasses);
                case PACKAGE_IMPORT -> JavaArchUnitSecurityTestCaseCollection
                        .noClassMustImportForbiddenPackages(allowedPackages)
                        .check(javaClasses);
                case REFLECTION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_USE_REFLECTION
                        .check(javaClasses);
                case TERMINATE_JVM -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_TERMINATE_JVM
                        .check(javaClasses);
                case SERIALIZATION -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_SERIALIZE
                        .check(javaClasses);
                case CLASS_LOADING -> JavaArchUnitSecurityTestCaseCollection
                        .NO_CLASS_MUST_USE_CLASSLOADERS
                        .check(javaClasses);
                default ->
                        throw new SecurityException(Messages.localized("security.common.unsupported.operation", this.javaArchitectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            JavaArchitectureTestCase.parseErrorMessage(e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">
    // Static method to start the builder
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    // Static Builder class
    public static class Builder {
        @Nullable
        private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
        @Nullable
        private Set<PackagePermission> allowedPackages;
        @Nullable
        private JavaClasses javaClasses;

        public Builder javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported, "javaArchitectureTestCaseSupported must not be null");
            return this;
        }

        public Builder allowedPackages(Set<PackagePermission> allowedPackages) {
            this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
            return this;
        }

        public Builder javaClasses(JavaClasses javaClasses) {
            this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
            return this;
        }

        public JavaArchUnitSecurityTestCase build() {
            Preconditions.checkState(javaArchitectureTestCaseSupported != null, "javaArchitectureTestCaseSupported must not be null");
            Preconditions.checkState(allowedPackages != null, "allowedPackages must not be null");
            Preconditions.checkState(javaClasses != null, "javaClasses must not be null");
            return new JavaArchUnitSecurityTestCase(javaArchitectureTestCaseSupported, allowedPackages, javaClasses);
        }
    }
    //</editor-fold>
}
