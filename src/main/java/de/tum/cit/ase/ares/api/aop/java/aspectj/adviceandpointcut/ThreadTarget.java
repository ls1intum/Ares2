package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved thread target.
 * <p>
 * Description: Wraps a nullable class name string. Used throughout the aspect
 * as the canonical representation of a thread endpoint, regardless of whether
 * it originated from a {@link Runnable}, a
 * {@link java.util.concurrent.Callable}, a lambda expression, or another
 * thread-capable object.
 * <p>
 * Design Rationale: Kept as its own top-level type (rather than a record nested
 * inside {@link JavaAspectJThreadSystemAdviceDefinitions}) to mirror the
 * structure of the instrumentation backend's
 * {@code de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.ThreadTarget}.
 * It is package-private because only the same-package aspect references it; the
 * instrumentation counterpart is {@code public} solely because its agent
 * injects it across packages, a constraint that does not apply to AspectJ
 * weaving.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
record ThreadTarget(@Nullable String className) {

	/**
	 * Returns a human-readable string representation of this thread target.
	 * <p>
	 * Description: Returns the class name, or {@code <unknown>} if the class name
	 * is {@code null}.
	 *
	 * @return non-null display string
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	String toDisplayString() {
		return className == null ? "<unknown>" : className;
	}
}
