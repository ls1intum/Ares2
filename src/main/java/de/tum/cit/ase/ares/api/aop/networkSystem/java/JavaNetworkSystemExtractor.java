package de.tum.cit.ase.ares.api.aop.networkSystem.java;

import de.tum.cit.ase.ares.api.aop.networkSystem.NetworkSystemExtractor;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

public class JavaNetworkSystemExtractor implements NetworkSystemExtractor {

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Constructs a new JavaNetworkConnectionExtractor with the specified resource access supplier.
     *
     * @param resourceAccessSupplier the supplier for the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaNetworkSystemExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
        this.resourceAccessSupplier = resourceAccessSupplier;
    }

    //<editor-fold desc="Network Connections related methods">

    /**
     * Extracts the permitted network hosts from the provided configurations based on the given predicate.
     *
     * @param configs   the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which hosts are permitted, must not be null.
     * @return a list of permitted hosts.
     */
    @Nonnull
    public static List<String> extractHosts(@Nonnull List<NetworkPermission> configs, @Nonnull Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Extracts the permitted network ports from the provided configurations based on the given predicate.
     *
     * @param configs   the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which ports are permitted, must not be null.
     * @return a list of permitted ports.
     */
    @Nonnull
    public static List<String> extractPorts(@Nonnull List<NetworkPermission> configs, @Nonnull Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(predicate)
                .map(NetworkPermission::onThePort)
                .map(String::valueOf)
                .toList();
    }

    /**
     * Retrieves the list of network hosts that are permitted for the given permission type.
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network hosts for the specified network permission type.
     */
    @Nonnull
    public List<String> getPermittedNetworkHosts(@Nonnull String networkPermission) {
        @Nonnull Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.network.permission", networkPermission));
        };
        return ((List<NetworkPermission>) resourceAccessSupplier.get())
                .stream()
                .filter(filter)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Retrieves the list of network ports that are permitted for the given permission type.
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network ports for the specified network permission type.
     */
    @Nonnull
    public List<Integer> getPermittedNetworkPorts(@Nonnull String networkPermission) {
        @Nonnull Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.network.permission", networkPermission));
        };
        return ((List<NetworkPermission>) resourceAccessSupplier.get())
                .stream()
                .filter(filter)
                .map(NetworkPermission::onThePort)
                .toList();
    }
    //</editor-fold>
}
