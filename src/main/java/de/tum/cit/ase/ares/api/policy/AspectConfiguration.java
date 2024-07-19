package de.tum.cit.ase.ares.api.policy;

public interface AspectConfiguration {
    void createAspectConfiguration(SecurityPolicy securityPolicy);

    String createAspectConfigurationFileContent(SecurityPolicy securityPolicy);

    void runAspectConfiguration(SecurityPolicy securityPolicy);
}
