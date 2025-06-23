package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Allowed network operations.
 *
 * <p>Description: Specifies permissions for opening connections, sending data, and receiving data on a specified host and port.
 *
 * <p>Design Rationale: Clearly defining network permissions helps protect against unauthorised network interactions.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param openConnections whether opening network connections is permitted.
 * @param sendData whether sending data is permitted.
 * @param receiveData whether receiving data is permitted.
 * @param onTheHost the host where these operations are permitted; must not be null.
 * @param onThePort the port number where these operations are permitted.
 */
public record NetworkPermission(
        @Nonnull String onTheHost,
        int onThePort,
        boolean openConnections,
        boolean sendData,
        boolean receiveData
) {

    /**
     * Constructs a NetworkPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public NetworkPermission {
        Objects.requireNonNull(onTheHost, "onTheHost must not be null");
        if (onTheHost.isBlank()) {
            throw new IllegalArgumentException("onTheHost must not be blank");
        }
        if (onThePort < 0 || onThePort > 65535) {
            throw new IllegalArgumentException("onThePort must be between 0 and 65535");
        }
    }

    /**
     * Creates a restrictive network permission with all operations denied.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param onTheHost the host where the restriction applies.
     * @param onThePort the port number where the restriction applies.
     * @return a new NetworkPermission instance with all operations denied.
     */
    @Nonnull
    public static NetworkPermission createRestrictive(@Nonnull String onTheHost, int onThePort) {
        return builder().onTheHost(Objects.requireNonNull(onTheHost, "onTheHost must not be null")).onThePort(onThePort).openConnections(false).receiveData(false).sendData(false).build();
    }

    /**
     * Returns a builder for creating a NetworkPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new NetworkPermission.Builder instance.
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for NetworkPermission.
     *
     * <p>Description: Provides a fluent API to construct a NetworkPermission instance.
     *
     * <p>Design Rationale: This builder facilitates stepwise configuration of network permissions.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static class Builder {

        /**
         * The host where these operations are permitted.
         */
        @Nullable
        private String onTheHost;
        /**
         * The port number where these operations are permitted.
         */
        private int onThePort;
        /**
         * Whether opening connections is permitted.
         */
        private boolean openConnections;
        /**
         * Whether sending data is permitted.
         */
        private boolean sendData;
        /**
         * Whether receiving data is permitted.
         */
        private boolean receiveData;

        /**
         * Sets the host where these operations are permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param onTheHost the host.
         * @return the updated Builder.
         */
        public Builder onTheHost(String onTheHost) {
            this.onTheHost = onTheHost;
            return this;
        }

        /**
         * Sets the port number where these operations are permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param onThePort the port number.
         * @return the updated Builder.
         */
        public Builder onThePort(int onThePort) {
            this.onThePort = onThePort;
            return this;
        }

        /**
         * Sets whether opening connections is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param openConnections whether opening connections is permitted.
         * @return the updated Builder.
         */
        public Builder openConnections(boolean openConnections) {
            this.openConnections = openConnections;
            return this;
        }

        /**
         * Sets whether sending data is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param sendData whether sending data is permitted.
         * @return the updated Builder.
         */
        public Builder sendData(boolean sendData) {
            this.sendData = sendData;
            return this;
        }

        /**
         * Sets whether receiving data is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param receiveData whether receiving data is permitted.
         * @return the updated Builder.
         */
        public Builder receiveData(boolean receiveData) {
            this.receiveData = receiveData;
            return this;
        }

        /**
         * Builds a new NetworkPermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new NetworkPermission instance.
         */
        public NetworkPermission build() {
            return new NetworkPermission(Objects.requireNonNull(onTheHost, "onTheHost must not be null"), onThePort, openConnections, sendData, receiveData);
        }
    }
}
