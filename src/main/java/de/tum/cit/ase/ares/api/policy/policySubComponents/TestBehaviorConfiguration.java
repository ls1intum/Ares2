package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper for the behavioural test-lifecycle features a policy configures.
 * <p>
 * Description: Parallel to {@link ResourceAccesses} on {@link SupervisedCode},
 * but for features that are test-lifecycle/reporting concerns rather than
 * resource-access permissions. Holds one optional category per behavioural
 * feature; this record currently exposes only
 * {@link #regardingPrivilegedExceptions()}, with room for sibling categories
 * later.
 * <p>
 * Design Rationale: A policy omitting this wrapper entirely, or omitting one of
 * its categories, parses and behaves exactly as if that feature were never
 * mentioned - every field here is optional on purpose.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param regardingPrivilegedExceptions the policy-level default for
 *                                      privileged-exceptions-only reporting;
 *                                      null when not configured.
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
