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
 * @param regardingOutputMirroring the policy-level output default; null when
 *                                 not configured.
 */
public record TestBehaviorConfiguration(@Nullable OutputMirroringConfiguration regardingOutputMirroring) {

	/**
	 * Fully-qualified name of the class a precompile run generates to carry this
	 * configuration as literal constants. Its package is deliberately not one of
	 * the packages inside the Ares JAR: those are sealed, so a class compiled into
	 * the exercise could not be loaded there next to the JAR.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings";

	/** Name of the generated field holding whether output is mirrored. */
	public static final String OUTPUT_MIRRORING_MIRRORED_FIELD_NAME = "REGARDING_OUTPUT_MIRRORING_THE_OUTPUT_IS_MIRRORED";

	/** Name of the generated field holding the output limit per stream. */
	public static final String OUTPUT_MIRRORING_LIMIT_FIELD_NAME = "REGARDING_OUTPUT_MIRRORING_THE_MAXIMUM_CHARACTER_COUNT_IS";

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
		if (regardingOutputMirroring == null) {
			return List.of();
		}
		return List.of(
				JavaAOPTestCaseToolbox.getPublicStaticFinalBooleanAssignment(OUTPUT_MIRRORING_MIRRORED_FIELD_NAME,
						regardingOutputMirroring.mirrored()),
				JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(OUTPUT_MIRRORING_LIMIT_FIELD_NAME,
						regardingOutputMirroring.maximumCharacterCount()));
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

		/** The output category to build with, or null to build with none. */
		@Nullable
		private OutputMirroringConfiguration regardingOutputMirroring;

		/**
		 * Sets the output-mirroring category.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param regardingOutputMirroring the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingOutputMirroring(@Nullable OutputMirroringConfiguration regardingOutputMirroring) {
			this.regardingOutputMirroring = regardingOutputMirroring;
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
			return new TestBehaviorConfiguration(regardingOutputMirroring);
		}
	}
}
