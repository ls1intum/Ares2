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
    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new abstract architecture test case with the specified parameters.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param architectureTestCaseSupported The type of architecture test case supported, determining which rules to apply
     */
    protected ArchitectureTestCase(
            @Nonnull ArchitectureTestCaseSupported architectureTestCaseSupported
    ) {
        this.architectureTestCaseSupported = Preconditions.checkNotNull(architectureTestCaseSupported, "architecturalTestCaseSupported must not be null");
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