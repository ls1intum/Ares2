package de.tum.cit.ase.ares.api.aspectConfiguration;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

/**
 * Aspect configuration for the Java programming language and concrete product of the abstract factory design pattern.
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 */
public class JavaAspectConfiguration implements AspectConfiguration {

    private JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private SecurityPolicy securityPolicy;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaSupportedAspectConfiguration Selects the supported aspect configuration in the Java programming language
     * @param securityPolicy Security policy for the aspect configuration
     */
    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
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
