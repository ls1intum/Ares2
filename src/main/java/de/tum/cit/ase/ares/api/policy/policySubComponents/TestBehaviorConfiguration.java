package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;

/**
 * Wraps the behavioural test-lifecycle settings a policy configures, parallel
 * to {@link ResourceAccesses} on {@link SupervisedCode}. Every field is
 * optional, and omitting one means that feature was never configured.
 *
 * @since 2.2.0
 * @author Luka Petrovic
 * @param regardingStrictTimeouts the policy-level timeout default; null when
 *                                not configured.
 */
public record TestBehaviorConfiguration(@Nullable StrictTimeoutsConfiguration regardingStrictTimeouts) {

	/**
	 * Preserves the public empty configuration constructor for existing callers.
	 */
	public TestBehaviorConfiguration() {
		this(null);
	}

	/**
	 * Fully-qualified name of the class a precompile run generates to carry this
	 * configuration as literal constants. Its package is deliberately not one of
	 * the packages inside the Ares JAR: those are sealed, so a class compiled into
	 * the exercise could not be loaded there next to the JAR.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings";

	/** Name of the generated field holding the strict timeout's amount. */
	public static final String STRICT_TIMEOUTS_TIMEOUT_FIELD_NAME = "REGARDING_STRICT_TIMEOUTS_THE_TIMEOUT_IS";

	/** Name of the generated field holding the strict timeout's unit. */
	public static final String STRICT_TIMEOUTS_TIME_UNIT_FIELD_NAME = "REGARDING_STRICT_TIMEOUTS_THE_TIME_UNIT_IS";

	/**
	 * Name of the generated field holding the grace period's amount, {@code -1}
	 * when not configured.
	 */
	public static final String STRICT_TIMEOUTS_GRACE_FIELD_NAME = "REGARDING_STRICT_TIMEOUTS_THE_TERMINATION_GRACE_IS";

	/** Name of the generated field holding the grace period's unit. */
	public static final String STRICT_TIMEOUTS_GRACE_UNIT_FIELD_NAME = "REGARDING_STRICT_TIMEOUTS_THE_TERMINATION_GRACE_UNIT_IS";

	/** The generated grace amount that means "not configured". */
	public static final long STRICT_TIMEOUTS_GRACE_NOT_CONFIGURED = -1;

	/**
	 * Literal field assignments every configured category contributes, one entry
	 * per field, each already formatted by {@code JavaAOPTestCaseToolbox}'s
	 * public-static-final assignment helpers. Empty when nothing is configured.
	 *
	 * @since 2.2.0
	 * @author Luka Petrovic
	 * @return the literal field assignments to write into the generated settings
	 *         class; empty when nothing is configured.
	 */
	@Nonnull
	public List<String> literalFieldAssignments() {
		if (regardingStrictTimeouts == null) {
			return List.of();
		}
		return List.of(
				JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(STRICT_TIMEOUTS_TIMEOUT_FIELD_NAME,
						regardingStrictTimeouts.theTimeoutIs()),
				JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(STRICT_TIMEOUTS_TIME_UNIT_FIELD_NAME,
						regardingStrictTimeouts.theTimeUnitIs().name()),
				JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(STRICT_TIMEOUTS_GRACE_FIELD_NAME,
						Objects.requireNonNullElse(regardingStrictTimeouts.theTerminationGraceIs(),
								STRICT_TIMEOUTS_GRACE_NOT_CONFIGURED)),
				JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(STRICT_TIMEOUTS_GRACE_UNIT_FIELD_NAME,
						regardingStrictTimeouts.theTerminationGraceUnitIs().name()));
	}

	/**
	 * Returns a builder for creating a TestBehaviorConfiguration instance.
	 *
	 * @since 2.2.0
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
	 * @since 2.2.0
	 * @author Luka Petrovic
	 */
	public static class Builder {

		/** The timeout category to build with, or null to build with none. */
		@Nullable
		private StrictTimeoutsConfiguration regardingStrictTimeouts;

		/**
		 * Sets the strict-timeout category.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param regardingStrictTimeouts the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingStrictTimeouts(@Nullable StrictTimeoutsConfiguration regardingStrictTimeouts) {
			this.regardingStrictTimeouts = regardingStrictTimeouts;
			return this;
		}

		/**
		 * Builds a new TestBehaviorConfiguration instance.
		 *
		 * @since 2.2.0
		 * @author Luka Petrovic
		 * @return a new TestBehaviorConfiguration instance.
		 */
		@Nonnull
		public TestBehaviorConfiguration build() {
			return new TestBehaviorConfiguration(regardingStrictTimeouts);
		}
	}
}
