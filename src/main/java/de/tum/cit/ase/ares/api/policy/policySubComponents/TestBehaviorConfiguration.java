package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;

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
	 * Fully-qualified name of the generated, compiled class that carries this
	 * configuration forward for a precompile deployment, as literal
	 * {@code public static final} fields - the same way file/network/command/thread
	 * permissions already reach enforcement code, so nothing re-reads or re-parses
	 * a policy artefact at test-run time. A class under this exact name only exists
	 * on the classpath once a category actually contributes a field to it.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.api.policy.policySubComponents.GeneratedTestBehaviorSettings";

	/**
	 * Fully-qualified name of the boolean field {@link #literalFieldAssignments()}
	 * contributes for
	 * {@link PrivilegedExceptionsConfiguration#onlyPrivilegedExceptionsAreReported()},
	 * exposed so {@code ConfigurationUtils} can read it back by name.
	 */
	public static final String PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME = "REGARDING_PRIVILEGED_EXCEPTIONS_ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED";

	/**
	 * Fully-qualified name of the string field {@link #literalFieldAssignments()}
	 * contributes for
	 * {@link PrivilegedExceptionsConfiguration#theFailureMessageIs()}, exposed so
	 * {@code ConfigurationUtils} can read it back by name.
	 */
	public static final String PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME = "REGARDING_PRIVILEGED_EXCEPTIONS_THE_FAILURE_MESSAGE_IS";

	/**
	 * Literal field assignments every configured category contributes, one entry
	 * per field, each already formatted by {@code JavaAOPTestCaseToolbox}'s
	 * public-static-final assignment helpers. Empty when
	 * {@link #regardingPrivilegedExceptions()} is null, since nothing is
	 * configured.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return the literal field assignments to write into the generated settings
	 *         class; empty when nothing is configured.
	 */
	@Nonnull
	public List<String> literalFieldAssignments() {
		if (regardingPrivilegedExceptions == null) {
			return List.of();
		}
		return List.of(
				JavaAOPTestCaseToolbox.getPublicStaticFinalBooleanAssignment(PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME,
						regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported()),
				JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME,
						regardingPrivilegedExceptions.theFailureMessageIs()));
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
