package de.tum.cit.ase.ares.api.aspect;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public interface AspectConfiguration {

    void runAspectJConfiguration(SecurityPolicy securityPolicy);

    String createAspectConfiguration(SecurityPolicy securityPolicy);

}
