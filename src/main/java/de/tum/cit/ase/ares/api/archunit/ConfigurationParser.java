package de.tum.cit.ase.ares.api.archunit;

import org.apiguardian.api.API;

/**
 * This class parses and validates the configuration file within the test repository.
 */
@API(status = API.Status.INTERNAL)
public class ConfigurationParser {

    /**
     * Parses the configuration file and returns the configuration object
     * @return The configuration object
     */
    public ArchSecurityConfiguration parseConfiguration() {
        return new ArchSecurityConfiguration();
    }
}
