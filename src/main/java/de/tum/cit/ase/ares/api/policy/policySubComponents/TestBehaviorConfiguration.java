package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps the behavioural test-lifecycle settings a policy configures, parallel
 * to {@link ResourceAccesses} on {@link SupervisedCode}. Exposes one category
 * today; every field is optional, and omitting one means that feature was never
 * configured.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param regardingPrivilegedExceptions the policy-level default; null when not
 *                                      configured.
 */
public record TestBehaviorConfiguration(@Nullable PrivilegedExceptionsConfiguration regardingPrivilegedExceptions) {

	/**
	 * Classpath-relative location of the generated, project-level resource that
	 * carries this configuration forward for a precompile deployment, where nothing
	 * dynamically re-reads the original policy file after the generator process
	 * ends.
	 */
	public static final String GENERATED_RESOURCE_PATH = "ares/api/policy/policySubComponents/TestBehaviorConfiguration.properties";

	/**
	 * Returns a builder for creating a TestBehaviorConfiguration instance.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for TestBehaviorConfiguration.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public static class Builder {
		/** The category to build with, or null to build with none configured. */
		@Nullable
		private PrivilegedExceptionsConfiguration regardingPrivilegedExceptions;

		/**
		 * Sets the privileged-exceptions category.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param regardingPrivilegedExceptions the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingPrivilegedExceptions(
				@Nullable PrivilegedExceptionsConfiguration regardingPrivilegedExceptions) {
			this.regardingPrivilegedExceptions = regardingPrivilegedExceptions;
			return this;
		}

		/**
		 * Builds a new TestBehaviorConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new TestBehaviorConfiguration instance.
		 */
		@Nonnull
		public TestBehaviorConfiguration build() {
			return new TestBehaviorConfiguration(regardingPrivilegedExceptions);
		}
	}
}
