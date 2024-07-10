package de.tum.cit.ase.ares.api.archunit;

import org.apiguardian.api.API;

/**
 * This class parses and validates the configuration file within the test repository.
 */
@API(status = API.Status.INTERNAL)
public class ConfigurationParser {

    private ConfigurationParser() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Parses the configuration file and returns the configuration object
     * @return The configuration object
     */
    public static ArchSecurityConfiguration parseConfiguration() {
        // TODO: Implement configuration parsing after the configuration file is defined
        return new ArchSecurityConfiguration("Java", false, null);
    }
}
