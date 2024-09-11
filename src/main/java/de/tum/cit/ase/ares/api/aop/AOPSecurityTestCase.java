package de.tum.cit.ase.ares.api.aop;

import javax.annotation.Nonnull;

/**
 * Interface for aspect-oriented programming (AOP) security test case configurations across various programming languages.
 * <p>
 * This interface serves as the abstract product in the Abstract Factory Design Pattern,
 * requiring the implementation of methods for generating and executing aspect configuration
 * files tailored to any programming languages.
 * </p>
 *
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public interface AOPSecurityTestCase {

    /**
     * Writes the content of the aspect configuration file for any programming language.
     * <p>
     * This method is responsible for creating the necessary content to configure aspect-oriented
     * programming (AOP) within the target programming language. The generated configuration
     * content is typically language-specific and must ensure that the relevant security aspects
     * are defined and properly set up.
     * </p>
     *
     * @return a {@link String} representing the content of the aspect configuration file.
     */
    @Nonnull String writeAOPSecurityTestCase();

    /**
     * Executes the aspect configuration in any programming language.
     * <p>
     * This method should apply the generated aspect configuration, integrating the defined
     * aspects into the application. It is expected to manage any language-specific execution
     * details, ensuring the aspects are correctly applied and the application behaves as intended
     * with the security configurations in place.
     * </p>
     */
    void executeAOPSecurityTestCase();
}