package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;

/**
 * Wraps the behavioural test-lifecycle settings a policy configures, parallel
 * to {@link ResourceAccesses} on {@link SupervisedCode}. Carries no category
 * yet; a behavioral-feature policy field is added here as its own record
 * component, one per feature.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
public record TestBehaviorConfiguration() {

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

		/**
		 * Builds a new TestBehaviorConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new TestBehaviorConfiguration instance.
		 */
		@Nonnull
		public TestBehaviorConfiguration build() {
			return new TestBehaviorConfiguration();
		}
	}
}
