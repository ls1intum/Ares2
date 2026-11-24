package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import java.net.InetAddress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A network endpoint as the network checks compare it: a host name or text, a
 * port, and the address itself when the call named one.
 *
 * @param host    the host name or text, or {@code null}
 * @param port    the port, or -1 if unknown
 * @param address the address the call named, or {@code null} for a name only
 */
record NetworkTarget(@Nullable String host, int port, @Nullable InetAddress address) {

	/**
	 * Creates a target given only by name or text, with no address of its own.
	 *
	 * @param host the host name or text
	 * @param port the port, or -1 if unknown
	 */
	NetworkTarget(@Nullable String host, int port) {
		this(host, port, null);
	}

	/**
	 * Returns a human-readable string representation of this network target.
	 * <p>
	 * Description: Formats the host and port as {@code host:port}. If the host is
	 * {@code null} or blank, substitutes the placeholder {@code <unknown>}.
	 *
	 * @return non-null display string in the form {@code host:port}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nonnull
	String toDisplayString() {
		String normalizedHost = host == null || host.isBlank() ? "<unknown>" : host;
		return normalizedHost + ":" + port;
	}
}
