package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved thread target.
 * <p>
 * Description: Wraps a nullable class name string. Used throughout the
 * toolbox as the canonical representation of a thread endpoint,
 * regardless of whether it originated from a {@link Runnable}, a
 * {@link java.util.concurrent.Callable}, a lambda expression, or another
 * thread-capable object.
 * <p>
 * Design Rationale: Extracted from
 * {@link JavaInstrumentationAdviceThreadSystemToolbox} into its own top-level
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
public record ThreadTarget(@Nullable String className) {

	/**
	 * Returns a human-readable string representation of this thread target.
	 * <p>
	 * Description: Returns the class name, or {@code <unknown>}
	 * if the class name is {@code null}.
	 *
	 * @return non-null display string
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	public String toDisplayString() {
		return className == null ? "<unknown>" : className;
	}
}
