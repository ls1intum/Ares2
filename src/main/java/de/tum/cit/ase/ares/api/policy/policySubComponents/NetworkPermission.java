package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
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
@Nonnull
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
        Objects.requireNonNull(onTheHost, "Host must not be null");
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
     * @param host the host where the restriction applies.
     * @param port the port number where the restriction applies.
     * @return a new NetworkPermission instance with all operations denied.
     */
    public static NetworkPermission createRestrictive(String host, int port) {
        return new NetworkPermission(host, port, false, false, false);
    }

    /**
     * Returns a builder for creating a NetworkPermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new NetworkPermission.Builder instance.
     */
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

        private boolean openConnections;
        private boolean sendData;
        private boolean receiveData;
        private String host;
        private int port;

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
         * Sets the host where these operations are permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param host the host.
         * @return the updated Builder.
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port number where these operations are permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param port the port number.
         * @return the updated Builder.
         */
        public Builder port(int port) {
            this.port = port;
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
            return new NetworkPermission(host, port, openConnections, sendData, receiveData);
        }
    }
}
