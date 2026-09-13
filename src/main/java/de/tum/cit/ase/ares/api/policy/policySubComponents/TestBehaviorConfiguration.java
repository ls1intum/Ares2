package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Wraps the behavioural test-lifecycle settings a policy configures, parallel
 * to {@link ResourceAccesses} on {@link SupervisedCode}. Carries no category
 * yet; a behavioural-feature policy field is added here as its own record
 * component, one per feature.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
public record TestBehaviorConfiguration() {

	/**
	 * Fully-qualified name of the generated, compiled class that carries this
	 * configuration forward for a precompile deployment, as literal
	 * {@code public static final} fields - the same way file/network/command/thread
	 * permissions already reach enforcement code, so nothing re-reads or re-parses
	 * a policy artefact at test-run time. A class under this exact name only exists
	 * on the classpath once a category actually contributes a field to it.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.api.policy.policySubComponents.GeneratedTestBehaviorSettings";

	/**
	 * Literal field assignments every configured category contributes, one entry
	 * per field, each already formatted by {@code JavaAOPTestCaseToolbox}'s
	 * public-static-final assignment helpers. Empty here, since no category is
	 * configured yet; a category adds its own contribution once it exists.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return the literal field assignments to write into the generated settings
	 *         class; empty when nothing is configured.
	 */
	@Nonnull
	public List<String> literalFieldAssignments() {
		return List.of();
	}

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
