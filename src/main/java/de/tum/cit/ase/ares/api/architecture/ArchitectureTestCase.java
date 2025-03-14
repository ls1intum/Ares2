package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import javax.annotation.Nonnull;

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
public interface ArchitectureTestCase {

    /**
     * Generates the content of the architecture test case file for the specified architecture mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the architecture mode identifier.
     * @return the architecture test case file content as a string.
     */
    @Nonnull String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);

    /**
     * Executes the architecture test case using the provided architecture mode and AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the identifier for the architecture testing mode.
     * @param aopMode the identifier for the AOP mode.
     */
    void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);
}