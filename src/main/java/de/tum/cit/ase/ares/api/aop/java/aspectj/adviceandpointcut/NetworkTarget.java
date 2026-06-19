package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved network target.
 * <p>
 * Description: Pairs a nullable hostname string with an integer port number.
 * Used throughout the aspect as the canonical representation of a network
 * endpoint, regardless of whether it originated from a {@link java.net.Socket},
 * a {@link java.net.URI}, a {@link java.net.http.HttpRequest}, or another
 * network-capable object.
 * <p>
 * Design Rationale: Kept as its own top-level type (rather than a record nested
 * inside {@link JavaAspectJNetworkSystemAdviceDefinitions}) to mirror the
 * structure of the instrumentation backend's
 * {@code de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.NetworkTarget}.
 * It is package-private because only the same-package aspect references it; the
 * instrumentation counterpart is {@code public} solely because its agent
 * injects it across packages, a constraint that does not apply to AspectJ
 * weaving.
 *
 * @since 2.0.0
 * @author Kevin Fischer
 */
record NetworkTarget(@Nullable String host, int port) {

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
