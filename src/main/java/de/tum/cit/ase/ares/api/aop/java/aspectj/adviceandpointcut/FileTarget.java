package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved file system target.
 * <p>
 * Description: Wraps a nullable {@link Path} instance. Used throughout the
 * aspect as the canonical representation of a file system endpoint, regardless
 * of whether it originated from a {@link Path}, a {@link java.io.File}, a
 * {@link java.net.URI}, a {@link java.net.URL}, or a {@link String}.
 * <p>
 * Design Rationale: Kept as its own top-level type (rather than a record nested
 * inside {@link JavaAspectJFileSystemAdviceDefinitions}) to mirror the
 * structure of the instrumentation backend's
 * {@code de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.FileTarget}.
 * It is package-private because only the same-package aspect references it; the
 * instrumentation counterpart is {@code public} solely because its agent
 * injects it across packages, a constraint that does not apply to AspectJ
 * weaving.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
record FileTarget(@Nullable Path path) {

	/**
	 * Returns a human-readable string representation of this file target.
	 * <p>
	 * Description: Returns the string form of the path, or {@code <unknown>} if the
	 * path is {@code null}.
	 *
	 * @return non-null display string
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	String toDisplayString() {
		return path == null ? "<unknown>" : path.toString();
	}
}
