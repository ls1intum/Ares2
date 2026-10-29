package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;

/**
 * Allowed network operations on a host and port.
 * <p>
 * An entry granting nothing overrides no other entry. A DNS name also covers
 * its subdomains: {@code example.com} permits {@code api.example.com} too.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param onTheHost       a DNS name, an IP address, {@code localhost} or
 *                        {@code *}.
 * @param onThePort       the port; {@code 0} means every port.
 * @param openConnections may open connections.
 * @param sendData        may send data.
 * @param receiveData     may receive data.
 */
public record NetworkPermission(@Nonnull String onTheHost, int onThePort, boolean openConnections, boolean sendData,
		boolean receiveData) {

	/**
	 * Constructs a NetworkPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException     if the host is null.
	 * @throws IllegalArgumentException if the host is not a DNS name, an IP
	 *                                  address, {@code localhost} or {@code *}, or
	 *                                  if the port lies outside 0 to 65535.
	 */
	public NetworkPermission {
		Objects.requireNonNull(onTheHost, "onTheHost must not be null");
		PolicyValueValidator.requireMatch("onTheHost", onTheHost, PolicyValueValidator.HOST_PATTERN);
		if (onThePort < 0 || onThePort > 65535) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.network.port.invalid"));
		}
	}

	/**
	 * Creates a restrictive network permission with all operations denied.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param onTheHost the host where the restriction applies; must not be null.
	 * @param onThePort the port where the restriction applies.
	 * @return a new NetworkPermission instance with all operations denied.
	 * @throws NullPointerException     if the host is null.
	 * @throws IllegalArgumentException if the host or the port is not valid.
	 */
	@Nonnull
	public static NetworkPermission createRestrictive(@Nonnull String onTheHost, int onThePort) {
		return builder().onTheHost(Objects.requireNonNull(onTheHost, "onTheHost must not be null")).onThePort(onThePort)
				.openConnections(false).receiveData(false).sendData(false).build();
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
	 * <p>
	 * Description: Provides a fluent API to construct a NetworkPermission instance.
	 * <p>
	 * Design Rationale: This builder facilitates stepwise configuration of network
	 * permissions.
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
		 * The port where these operations are permitted.
		 * <p>
		 * Held as a boxed value with no default on purpose. A primitive would default
		 * to {@code 0}, which does not mean "no port chosen" but "every port", so
		 * forgetting to set it would widen the permission instead of narrowing it.
		 * {@link #build()} therefore rejects a builder on which it was never set.
		 */
		@Nullable
		private Integer onThePort;
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
		 * @param onTheHost the host; may be left null here, but {@link #build()} then
		 *                  rejects the builder.
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
		 * @param onThePort the port, from 0 to 65535, where {@code 0} permits every
		 *                  port. There is no default: {@link #build()} rejects a
		 *                  builder on which this was never set, so permitting every
		 *                  port is always a deliberate choice.
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
		 * @return a new NetworkPermission instance. Every operation not set defaults to
		 *         denied.
		 * @throws NullPointerException     if no host or no port was set.
		 * @throws IllegalArgumentException if the host or the port is not valid.
		 */
		public NetworkPermission build() {
			return new NetworkPermission(Objects.requireNonNull(onTheHost, "onTheHost must not be null"),
					Objects.requireNonNull(onThePort, "onThePort must not be null"), openConnections, sendData,
					receiveData);
		}
	}
}
