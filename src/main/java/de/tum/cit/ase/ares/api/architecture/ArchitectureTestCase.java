package de.tum.cit.ase.ares.api.architecture;

import com.google.common.base.Preconditions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface for architecture test case configurations.
 *
 * <p>Description: Defines methods to generate and execute architecture test cases that validate architectural constraints across various programming languages.</p>
 *
 * <p>Design Rationale: By applying the Abstract Factory Design Pattern, this interface enables language-specific implementations while maintaining a consistent interface for test case generation and execution.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class ArchitectureTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Defines the type of architecture test case used in this test case.
     * Determines which architectural rules and validations will be applied during analysis of this test case.
     */
    @Nonnull
    protected final ArchitectureTestCaseSupported architectureTestCaseSupported;

    /**
     * Set of package permissions that are allowed to be imported or accessed by the code under test in this test case.
     * When empty, no package restrictions are enforced.
     */
    @Nonnull
    protected final Set<PackagePermission> allowedPackages;

    // The following attributes are used for caching
    /**
     * Collection of Java classes to be analyzed by this test case.
     * Contains metadata and structure of loaded Java classes for static analysis.
     * This data is cached after initial creation to improve performance on subsequent analyses.
     */
    @Nonnull
    protected final JavaClasses javaClasses;

    /**
     * Call graph of the analyzed Java classes in this test case.
     * Represents method caller-callee relationships in the code,
     * enabling analysis of method invocation patterns and paths.
     * May be null for analysis modes that don't require call graph information (e.g., ARCHUNIT).
     */
    @Nullable
    protected final CallGraph callGraph;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new abstract architecture test case with the specified parameters.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param architectureTestCaseSupported The type of architecture test case supported, determining which rules to apply
     * @param allowedPackages                   Set of package permissions that are allowed in the analyzed code (may be null for no restrictions)
     * @param javaClasses                       Collection of Java classes to be analyzed by the test case
     * @param callGraph                         Call graph representing caller-callee relationships in the code (may be null for ARCHUNIT mode)
     */
    protected ArchitectureTestCase(
            @Nonnull ArchitectureTestCaseSupported architectureTestCaseSupported,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull JavaClasses javaClasses,
            @Nullable CallGraph callGraph
    ) {
        this.architectureTestCaseSupported = Preconditions.checkNotNull(architectureTestCaseSupported, "architecturalTestCaseSupported must not be null");
        this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
        this.callGraph = callGraph;
        this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
    }
    //</editor-fold>

    //<editor-fold desc="Getter methods">
    @Nonnull
    public ArchitectureTestCaseSupported getArchitectureTestCaseSupported() {
        return architectureTestCaseSupported;
    }

    @Nonnull
    public Set<PackagePermission> getAllowedPackages() {
        return allowedPackages;
    }

    @Nonnull
    public JavaClasses getJavaClasses() {
        return javaClasses;
    }

    @Nullable
    public CallGraph getCallGraph() {
        return callGraph;
    }
    //</editor-fold>


    //<editor-fold desc="Abstract Methods">

    /**
     * Generates the content of the architecture test case file for the specified architecture mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the architecture mode identifier.
     * @return the architecture test case file content as a string.
     */
    @Nonnull
    public abstract String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);

    /**
     * Executes the architecture test case using the provided architecture mode and AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the identifier for the architecture testing mode.
     * @param aopMode the identifier for the AOP mode.
     */
    public abstract void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);
    //</editor-fold>
}