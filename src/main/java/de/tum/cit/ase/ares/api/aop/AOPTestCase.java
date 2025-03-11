package de.tum.cit.ase.ares.api.aop;

import javax.annotation.Nonnull;

/**
 * Interface for AOP test case configurations.
 *
 * <p>Description: Defines methods for generating and executing aspect configuration files that enforce security via aspect-oriented programming.</p>
 *
 * <p>Design Rationale: Abstracting AOP configurations into a unified interface allows for consistent integration and language-specific implementation of security measures.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface AOPTestCase {

    /**
     * Writes the content of the AOP test case configuration for the specified AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param aopMode the identifier for the AOP mode.
     * @return the AOP test case configuration content as a string.
     */
    @Nonnull String writeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);

    /**
     * Executes the AOP test case using the provided architecture mode and AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the identifier for the architecture mode.
     * @param aopMode the identifier for the AOP mode.
     */
    void executeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);
}