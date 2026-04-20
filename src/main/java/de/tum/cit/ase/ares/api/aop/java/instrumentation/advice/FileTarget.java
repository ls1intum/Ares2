package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved file system target.
 * <p>
 * Description: Wraps a nullable {@link Path} instance. Used throughout the
 * toolbox as the canonical representation of a file system endpoint,
 * regardless of whether it originated from a {@link Path}, a {@link java.io.File},
 * a {@link java.net.URI}, a {@link java.net.URL}, or a {@link String}.
 * <p>
 * Design Rationale: Extracted from
 * {@link JavaInstrumentationAdviceFileSystemToolbox} into its own top-level
 * class so that the JVM bootstrap class-loader injection performed by
 * {@link de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent}
 * can load it independently. Inner / nested classes compiled as separate
 * {@code .class} files are not picked up by
 * {@link net.bytebuddy.dynamic.ClassFileLocator.ForClassLoader#read(Class)}
 * when only the enclosing class is specified.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public record FileTarget(@Nullable Path path) {

	/**
	 * Returns a human-readable string representation of this file target.
	 * <p>
	 * Description: Returns the string form of the path, or {@code <unknown>}
	 * if the path is {@code null}.
	 *
	 * @return non-null display string
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	public String toDisplayString() {
		return path == null ? "<unknown>" : path.toString();
	}
}
