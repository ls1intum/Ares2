package de.tum.cit.ase.ares.api.aspectConfiguration;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public interface AspectConfiguration {

    String createAspectConfigurationFileContent();

    void runAspectConfiguration();
}
