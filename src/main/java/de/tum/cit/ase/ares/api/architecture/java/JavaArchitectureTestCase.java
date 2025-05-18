package de.tum.cit.ase.ares.api.architecture.java;

import com.google.common.base.Preconditions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaSecurityTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Architecture test case for the Java programming language.
 *
 * <p>Description: This class provides methods to write and execute architecture test cases that verify code compliance with security policies.
 * It supports different static analysis approaches including ArchUnit and WALA, with appropriate delegation to specialized
 * implementations based on the selected architecture mode.</p>
 *
 * <p>Design Rationale: Implements a shared interface for architecture testing strategies in different programming languages, facilitating
 * consistent security enforcement across varying analysis techniques while decoupling the client code from specific implementations.</p>
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class JavaArchitectureTestCase extends ArchitectureTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Set of package permissions that are allowed to be imported or accessed by the code under test in this test case.
     * When empty, no package restrictions are enforced.
     */
    @Nonnull
    private final Set<PackagePermission> allowedPackages;

    // The following attributes are used for caching
    /**
     * Collection of Java classes to be analyzed by this test case.
     * Contains metadata and structure of loaded Java classes for static analysis.
     * This data is cached after initial creation to improve performance on subsequent analyses.
     */
    @Nonnull
    private final JavaClasses javaClasses;

    /**
     * Call graph of the analyzed Java classes in this test case.
     * Represents method caller-callee relationships in the code,
     * enabling analysis of method invocation patterns and paths.
     * May be null for analysis modes that don't require call graph information (e.g., ARCHUNIT).
     */
    @Nullable
    private final CallGraph callGraph;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new Java architecture test case with the specified parameters.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param javaArchitectureTestCaseSupported The type of architecture test case supported, determining which rules to apply
     * @param javaClasses Collection of Java classes to be analyzed by the test case
     * @param callGraph Call graph representing caller-callee relationships in the code (may be null for ARCHUNIT mode)
     * @param allowedPackages Set of package permissions that are allowed in the analyzed code (may be null for no restrictions)
     */
    public JavaArchitectureTestCase(
            @Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
            @Nonnull JavaClasses javaClasses,
            @Nullable CallGraph callGraph,
            @Nonnull Set<PackagePermission> allowedPackages
    ) {
        super(javaArchitectureTestCaseSupported);
        this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
        this.callGraph = callGraph;
        this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    // TODO Markus: Move to better fitting class
    /**
     * Parses the error message of an assertion error to provide more descriptive security violation messages.
     * Extracts relevant information from ArchUnit's error messages and creates a standardized security exception.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param e The assertion error produced by an architectural rule violation
     * @throws SecurityException with a formatted error message describing the security violation
     */
    public static void parseErrorMessage(@Nonnull AssertionError e) {
        Preconditions.checkNotNull(e, "error must not be null");
        @Nullable String message = e.getMessage();
        Preconditions.checkNotNull(message, "message must not be null");
        @Nullable String[] messageParts = e.getMessage().split("\n");
        Preconditions.checkNotNull(messageParts, "messageParts must not be null");
        if (messageParts.length < 2) {
            throw new SecurityException(Messages.localized("security.archunit.illegal.execution", e.getMessage()));
        }
        @Nonnull String replacementIdentifier = ".*?'(.*?)'.*\r*";
        throw new SecurityException(Messages.localized("security.archunit.violation.error", messageParts[0].replaceAll(replacementIdentifier, "$1"), messageParts[1]));
    }
    //</editor-fold>

    //<editor-fold desc="Write architecture test case methods">
    /**
     * Generates the architecture test case code as a string based on the specified architecture mode.
     * Delegates to the appropriate implementation based on the architecture mode parameter.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param architectureMode The mode of architecture analysis (e.g., "WALA" or "ARCHUNIT")
     * @param aopMode The aspect-oriented programming mode to use
     * @return A string containing the generated architecture test case code
     * @throws SecurityException if the specified architecture mode is not supported
     */
    @Nonnull
    @Override
    public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Execute architecture test case methods">
    /**
     * Executes the architecture test case to validate code against security policies.
     * Delegates to the appropriate implementation based on the specified architecture mode.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param architectureMode The mode of architecture analysis (e.g., "WALA" or "ARCHUNIT")
     * @param aopMode The aspect-oriented programming mode to use
     * @throws SecurityException if validation fails or if required resources are missing for the selected mode
     */
    @Override
    public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        ArchitectureTestCaseSupported protectedArchitectureTestCaseSupported = Preconditions.checkNotNull(architectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null");
        Preconditions.checkArgument(architectureTestCaseSupported instanceof JavaArchitectureTestCaseSupported, "javaArchitecturalTestCaseSupported must be an instance of JavaArchitectureTestCaseSupported");
        JavaArchitectureTestCaseSupported protectedJavaArchitectureTestCaseSupported = (JavaArchitectureTestCaseSupported) protectedArchitectureTestCaseSupported;
        Set<PackagePermission> protectedAllowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
        JavaClasses protectedJavaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
        switch (architectureMode) {
            case "WALA" -> JavaWalaSecurityTestCase.builder()
                    .javaArchitectureTestCaseSupported(protectedJavaArchitectureTestCaseSupported)
                    .allowedPackages(protectedAllowedPackages)
                    .javaClasses(protectedJavaClasses)
                    .callGraph(callGraph)
                    .build()
                    .executeArchitectureTestCase();
            case "ARCHUNIT" -> JavaArchUnitSecurityTestCase.builder()
                    .javaArchitectureTestCaseSupported(protectedJavaArchitectureTestCaseSupported)
                    .allowedPackages(protectedAllowedPackages)
                    .javaClasses(protectedJavaClasses)
                    .build()
                    .executeArchitectureTestCase();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">

    /**
     * Creates a new builder instance for constructing JavaArchitectureTestCase objects.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @return A new Builder instance
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the Java architecture test case.
     */
    public static class Builder {
        @Nullable
        private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
        @Nullable
        private JavaClasses javaClasses;
        @Nullable
        private CallGraph callGraph;
        @Nullable
        private Set<PackagePermission> allowedPackages;

        /**
         * Sets the architecture test case type supported by this instance.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param javaArchitectureTestCaseSupported The type of architecture test case to support
         * @return This builder instance for method chaining
         * @throws SecurityException if the parameter is null
         */
        @Nonnull
        public Builder javaArchitecturalTestCaseSupported(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null");
            return this;
        }

        /**
         * Sets the Java classes to be analyzed by this architecture test case.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param javaClasses Collection of Java classes for analysis
         * @return This builder instance for method chaining
         */
        @Nonnull
        public Builder javaClasses(@Nonnull JavaClasses javaClasses) {
            this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
            return this;
        }

        /**
         * Sets the call graph to be used for analysis.
         * Required for WALA mode but not for ARCHUNIT mode.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param callGraph Call graph representing method relationships
         * @return This builder instance for method chaining
         */
        @Nonnull
        public Builder callGraph(@Nullable CallGraph callGraph) {
            this.callGraph = callGraph;
            return this;
        }

        /**
         * Sets the allowed package permissions.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param allowedPackages Set of package permissions that should be allowed
         * @return This builder instance for method chaining
         */
        @Nonnull
        public Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
            this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
            return this;
        }

        /**
         * Builds and returns a new JavaArchitectureTestCase instance with the configured properties.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @return A new JavaArchitectureTestCase instance
         * @throws SecurityException if required parameters are missing
         */
        @Nonnull
        public JavaArchitectureTestCase build() {
            return new JavaArchitectureTestCase(
                    Preconditions.checkNotNull(javaArchitectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null"),
                    Preconditions.checkNotNull(javaClasses, "javaClasses must not be null"),
                    callGraph,
                    Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null")
            );
        }
    }
    //</editor-fold>

}
