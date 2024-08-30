package de.tum.cit.ase.ares.api.aspectconfiguration;

/**
 * Interface for aspect configurations in any programming language.
 * <p>
 * This interface represents the abstract product in the Abstract Factory Design Pattern.
 * It defines the methods that must be implemented by any concrete aspect configuration,
 * regardless of the programming language.
 * </p>
 *
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public interface AspectConfiguration {

    /**
     * Creates the content of the aspect configuration file in the specific programming language.
     * <p>
     * This method should generate the configuration content necessary for applying aspect-oriented
     * programming (AOP) in the target programming language.
     * </p>
     *
     * @return the content of the aspect configuration file as a {@link String}.
     */
    String createAspectConfigurationFileContent();

    /**
     * Executes the aspect configuration in the specific programming language.
     * <p>
     * This method is responsible for running or applying the aspect configuration,
     * ensuring that the desired aspects are correctly integrated into the application.
     * </p>
     */
    void runAspectConfiguration();
}