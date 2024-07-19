package de.tum.cit.ase.ares.api.policy;

public record NetworkConnection(
        String forThisDomain,
        String forThisIPAddress,
        int iAllowTheStudentsToAccessThePort
) {
}
