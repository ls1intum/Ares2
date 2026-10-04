package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;

/**
 * Wraps the behavioural test-lifecycle settings a policy configures, parallel
 * to {@link ResourceAccesses} on {@link SupervisedCode}. Every field is
 * optional, and omitting one means that feature was never configured.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param regardingHiddenTests the policy-level hidden-test schedule; null when
 *                             not configured.
 */
public record TestBehaviorConfiguration(@Nullable HiddenTestsConfiguration regardingHiddenTests) {

	/**
	 * Fully-qualified name of the class a precompile run generates to carry this
	 * configuration as literal constants. Its package is deliberately not one of
	 * the packages inside the Ares JAR: those are sealed, so a class compiled into
	 * the exercise could not be loaded there next to the JAR.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings";

	/**
	 * Name of the generated field holding the effective deadline, extension
	 * included, in epoch milliseconds.
	 */
	public static final String HIDDEN_TESTS_DEADLINE_FIELD_NAME = "REGARDING_HIDDEN_TESTS_THE_DEADLINE_IS";

	/**
	 * Name of the generated field holding the always-run-before date in epoch
	 * milliseconds, {@link Long#MIN_VALUE} when not configured.
	 */
	public static final String HIDDEN_TESTS_ALWAYS_RUN_BEFORE_FIELD_NAME = "REGARDING_HIDDEN_TESTS_ALWAYS_RUN_BEFORE";

	/**
	 * Name of the generated field holding the hidden tests, one entry per line. A
	 * single string, because only a constant is copied into the generated hook.
	 */
	public static final String HIDDEN_TESTS_LIST_FIELD_NAME = "REGARDING_HIDDEN_TESTS_THE_FOLLOWING_TESTS_ARE_HIDDEN";

	/**
	 * Literal field assignments every configured category contributes, one entry
	 * per field, each already formatted by {@code JavaAOPTestCaseToolbox}'s
	 * public-static-final assignment helpers, defaults already applied. Empty when
	 * nothing is configured.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return the literal field assignments to write into the generated settings
	 *         class; empty when nothing is configured.
	 */
	@Nonnull
	public List<String> literalFieldAssignments() {
		if (regardingHiddenTests == null) {
			return List.of();
		}
		return List.of(
				JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(HIDDEN_TESTS_DEADLINE_FIELD_NAME,
						regardingHiddenTests.effectiveDeadline().toInstant().toEpochMilli()),
				JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(HIDDEN_TESTS_ALWAYS_RUN_BEFORE_FIELD_NAME,
						regardingHiddenTests.alwaysRunBefore().map(date -> date.toInstant().toEpochMilli())
								.orElse(Long.MIN_VALUE)),
				JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(HIDDEN_TESTS_LIST_FIELD_NAME,
						String.join("\n", regardingHiddenTests.theFollowingTestsAreHidden())));
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

		/** The hidden-test category to build with, or null to build with none. */
		@Nullable
		private HiddenTestsConfiguration regardingHiddenTests;

		/**
		 * Sets the hidden-test category.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param regardingHiddenTests the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingHiddenTests(@Nullable HiddenTestsConfiguration regardingHiddenTests) {
			this.regardingHiddenTests = regardingHiddenTests;
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
			return new TestBehaviorConfiguration(regardingHiddenTests);
		}
	}
}
