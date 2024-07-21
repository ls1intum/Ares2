package de.tum.cit.ase.ares.api.aspectconfiguration;

/**
 * Interface for the aspect configurations in any programming language and abstract product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public interface AspectConfiguration {

    /**
     * Creates the content of the aspect configuration file in any programming language.
     *
     * @return Content of the aspect configuration file
     */
    String createAspectConfigurationFileContent();

    /**
     * Runs the aspect configuration in any programming language.
     */
    void runAspectConfiguration();
}
