package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;
import static de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseCollection.getArchitectureRuleFileContent;
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
public class JavaArchUnitSecurityTestCase {
    private static final Logger log = LoggerFactory.getLogger(JavaArchUnitSecurityTestCase.class);

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
    private final Set<String> allowedPackages;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Constructor for JavaArchUnitSecurityTestCase.
     *
     * @param builder Selects the supported architecture test case in the Java programming language
     */
    private JavaArchUnitSecurityTestCase(@Nonnull Builder builder) {
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.allowedPackages = builder.allowedPackages;
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Nonnull
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
                case FILESYSTEM_INTERACTION -> JavaArchUnitTestCaseCollection
                        .NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM
                        .check(javaClasses);
                case NETWORK_CONNECTION -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_ACCESS_NETWORK
                        .check(javaClasses);
                case THREAD_CREATION -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_CREATE_THREADS
                        .check(javaClasses);
                case COMMAND_EXECUTION -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_EXECUTE_COMMANDS
                        .check(javaClasses);
                case PACKAGE_IMPORT -> JavaArchUnitTestCaseCollection
                        .noClassesShouldImportForbiddenPackages(allowedPackages)
                        .check(javaClasses);
                case REFLECTION -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_USE_REFLECTION
                        .check(javaClasses);
                case TERMINATE_JVM -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_TERMINATE_JVM
                        .check(javaClasses);
                case SERIALIZATION -> JavaArchUnitTestCaseCollection
                        .NO_CLASSES_SHOULD_SERIALIZE
                        .check(javaClasses);
                // Classloading included in the Reflection test case
                case CLASS_LOADING -> {}
                default ->
                        throw new SecurityException(localized("security.common.unsupported.operation", this.javaArchitectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            parseErrorMessage(e);
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
        private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
        private Set<String> allowedPackages = new HashSet<>();

        public Builder javaArchitecturalTestCaseSupported(JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
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

        public JavaArchUnitSecurityTestCase build() {
            return new JavaArchUnitSecurityTestCase(this);
        }
    }
    //</editor-fold>
}
