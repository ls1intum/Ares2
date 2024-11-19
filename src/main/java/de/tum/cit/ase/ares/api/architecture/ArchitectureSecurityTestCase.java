package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;

/**
 * Interface for defining architecture security test cases in various programming languages.
 * <p>
 * This interface serves as an abstract product in the Abstract Factory Design Pattern,
 * outlining the required methods for generating and executing architecture test cases
 * tailored to any programming languages.
 * </p>
 *
 * @version 2.0.0
 * @author Markus Paulsen
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public interface ArchitectureSecurityTestCase {

    /**
     * Generates the content of the architecture test case file in any programming language.
     * <p>
     * This method is responsible for creating the necessary content for testing the architecture
     * of an application. The content should be tailored to the target programming language and
     * should include all necessary details to enforce the architectural constraints.
     * </p>
     *
     * @return a {@link String} representing the content of the architecture test case file.
     */
    String writeArchitectureTestCase();

    /**
     * Executes the architecture test case using the provided set of Java classes.
     * <p>
     * This method applies the architecture test case to the given {@link JavaClasses}, ensuring
     * that the architectural constraints are validated within the specified codebase. It is expected
     * to handle any language-specific execution details required to run the test case.
     * </p>
     *
     * @param classes a set of {@link JavaClasses} representing the codebase to which the test case will be applied.
     */
    void executeArchitectureTestCase(JavaArchitectureMode architectureMode);
}