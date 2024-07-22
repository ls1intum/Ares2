package de.tum.cit.ase.ares.api.aspectconfiguration;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.nio.file.Path;

/**
 * Aspect configuration for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaAspectConfiguration implements AspectConfiguration {

    private JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private SecurityPolicy securityPolicy;
    private Path withinPath;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaSupportedAspectConfiguration Selects the supported aspect configuration in the Java programming language
     * @param securityPolicy                   Security policy for the aspect configuration
     */
    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy, Path withinPath) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
        this.withinPath = withinPath;
    }

    /**
     * Creates the content of the aspect configuration file in the Java programming language.
     *
     * @return Content of the aspect configuration file
     */
    @Override
    public String createAspectConfigurationFileContent() {
        throw new RuntimeException("Not implemented yet!");
    }

    /**
     * Runs the aspect configuration in Java programming language.
     */
    @Override
    public void runAspectConfiguration() {
        throw new RuntimeException("Not implemented yet!");
    }
}
