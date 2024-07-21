package de.tum.cit.ase.ares.api.policy;

/**
 * This class represents the network connection part of the security policy.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public record NetworkConnection(
        /**
         * The domain the students are allowed to connect to.
         */
        String forThisDomain,
        /**
         * The IP address the students are allowed to connect to.
         */
        String forThisIPAddress,
        /**
         * The port the students are allowed to connect to.
         */
        int iAllowTheStudentsToAccessThePort
) {
}
