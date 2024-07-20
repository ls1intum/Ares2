package de.tum.cit.ase.ares.api.aspectConfiguration;

/**
 * Interface for the aspect configurations in any programming language and abstract product of the abstract factory design pattern.
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
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
