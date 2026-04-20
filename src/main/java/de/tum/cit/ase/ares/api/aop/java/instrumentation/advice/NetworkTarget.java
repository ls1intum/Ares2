package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved network target.
 * <p>
 * Description: Pairs a nullable hostname string with an integer port number.
 * Used throughout the toolbox as the canonical representation of a network
 * endpoint, regardless of whether it originated from a {@link java.net.Socket},
 * a {@link java.net.URI}, a {@link java.net.http.HttpRequest}, or another
 * network-capable object.
 * <p>
 * Design Rationale: Extracted from
 * {@link JavaInstrumentationAdviceNetworkSystemToolbox} into its own top-level
 * class so that the JVM bootstrap class-loader injection performed by
 * {@link de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent}
 * can load it independently. Inner / nested classes compiled as separate
 * {@code .class} files are not picked up by
 * {@link net.bytebuddy.dynamic.ClassFileLocator.ForClassLoader#read(Class)}
 * when only the enclosing class is specified.
 *
 * @since 2.0.0
 * @author Kevin Fischer
 */
public record NetworkTarget(@Nullable String host, int port) {

	/**
	 * Returns a human-readable string representation of this network target.
	 * <p>
	 * Description: Formats the host and port as {@code host:port}. If the host
	 * is {@code null} or blank, substitutes the placeholder {@code <unknown>}.
	 *
	 * @return non-null display string in the form {@code host:port}
	 * @since 2.0.0
	 * @author Kevin Fischer
	 */
	@Nonnull
	public String toDisplayString() {
		String normalizedHost = host == null || host.isBlank() ? "<unknown>" : host;
		return normalizedHost + ":" + port;
	}
}
