package de.tum.cit.ase.ares.api.aop.networkConnection;

import javax.annotation.Nonnull;
import java.util.List;

public interface NetworkConnectionExtractor {

    /**
     * Retrieves the list of network hosts that are permitted for the given permission type.
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network hosts for the specified network permission type.
     */
    @Nonnull
    public abstract List<String> getPermittedNetworkHosts(@Nonnull String networkPermission);

    /**
     * Retrieves the list of network ports that are permitted for the given permission type.
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network ports for the specified network permission type.
     */
    @Nonnull
    public abstract List<Integer> getPermittedNetworkPorts(@Nonnull String networkPermission);
}
