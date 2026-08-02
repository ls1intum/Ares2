package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Permitted execution time.
 * <p>
 * Description: Specifies, in milliseconds, how long the supervised code may run
 * before it is terminated.
 * <p>
 * Design Rationale: Bounding the execution time keeps a submission that never
 * terminates, whether by mistake or deliberately, from occupying the grading
 * infrastructure indefinitely. Where a policy declares several of these
 * permissions, the smallest value wins, so adding one can only tighten the
 * limit and never relax it.
 *
 * @since 2.0.0
 * @author Ajayvir Singh
 * @param timeout the permitted execution time in milliseconds; must be greater
 *                than zero.
 */
public record ResourceLimitsPermission(long timeout) {

	/**
	 * Constructs a ResourceLimitsPermission instance.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @throws IllegalArgumentException if the timeout is zero or negative, as
	 *                                  neither bounds an execution usefully.
	 */
	public ResourceLimitsPermission {
		if (timeout <= 0) {
			throw new IllegalArgumentException(Messages.localized("policy.permission.timeout.positive"));
		}
	}

	/**
	 * Creates the execution-time permission that a restrictive policy grants.
	 * <p>
	 * Unlike the other restrictive factories in this package, this one cannot deny
	 * everything: a policy without any execution limit would leave a
	 * non-terminating submission running forever. It therefore grants ten seconds.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new ResourceLimitsPermission instance with the restrictive
	 *         execution time.
	 */
	@Nonnull
	public static ResourceLimitsPermission createRestrictive() {
		return builder().withTimeout(10000).build();
	}

	/**
	 * Returns a builder for creating a ResourceLimitsPermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new ResourceLimitsPermission.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for ResourceLimitsPermission.
	 * <p>
	 * Description: Provides a fluent API to construct a ResourceLimitsPermission
	 * instance.
	 * <p>
	 * Design Rationale: This builder enables step-by-step configuration of the
	 * execution limit.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 */
	public static class Builder {

		/**
		 * The permitted execution time in milliseconds. Has no default, so
		 * {@link #build()} rejects a builder on which it was never set.
		 */
		private long timeout;

		/**
		 * Sets the permitted execution time.
		 *
		 * @since 2.0.0
		 * @author Ajayvir Singh
		 * @param timeout the permitted execution time in milliseconds; must be greater
		 *                than zero, which {@link #build()} enforces.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder withTimeout(long timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * Builds a new ResourceLimitsPermission instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new ResourceLimitsPermission instance.
		 * @throws IllegalArgumentException if no positive execution time was set.
		 */
		@Nonnull
		public ResourceLimitsPermission build() {
			return new ResourceLimitsPermission(timeout);
		}
	}
}
