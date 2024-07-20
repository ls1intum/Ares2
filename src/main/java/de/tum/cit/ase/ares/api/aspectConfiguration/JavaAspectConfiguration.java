package de.tum.cit.ase.ares.api.aspectConfiguration;

import de.tum.cit.ase.ares.api.architectureTest.JavaSupportedArchitectureTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public class JavaAspectConfiguration implements AspectConfiguration {

    private JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private SecurityPolicy securityPolicy;

    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
    }

    @Override
    public String createAspectConfigurationFileContent() {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public void runAspectConfiguration() {
        throw new RuntimeException("Not implemented yet!");
    }
}
