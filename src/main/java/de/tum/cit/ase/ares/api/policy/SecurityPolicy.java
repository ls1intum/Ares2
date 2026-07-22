package de.tum.cit.ase.ares.api.policy;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Immutable security policy for supervised code execution.
 * <p>
 * Description: This record encapsulates all necessary details regarding
 * supervised code, including its programming language configuration, permitted
 * resource accesses, and additional metadata. By using the immutable record
 * pattern, it guarantees thread-safety and reduces boilerplate. Validation
 * happens in the canonical constructor, so no instance can exist that violates
 * its invariants, regardless of whether it was created directly, through the
 * builder, or by deserialising a policy file.
 * <p>
 * Design Rationale: Leveraging modern Java features such as records and
 * nullability annotations enforces immutability and clarity. Placing the
 * validation in the canonical constructor rather than in the factory methods
 * makes it impossible to bypass. The supervised code itself is modelled as a
 * separate record in its own right, which keeps this record responsible only
 * for the policy envelope: the declared format version and the supervised code
 * it applies to.
 *
 * @since 2.0.0
 * @version 2.1.0
 * @author Markus Paulsen
 * @param thisPolicyFileCompliesToThePolicyVersion the policy-format version the
 *                                                 policy declares; must be
 *                                                 between
 *                                                 {@value AresConstants#MINIMUM_POLICY_VERSION}
 *                                                 and
 *                                                 {@value AresConstants#MAXIMUM_POLICY_VERSION},
 *                                                 both inclusive.
 * @param regardingTheSupervisedCode               the details of the supervised
 *                                                 code; must not be null.
 */
@SuppressWarnings("unused")
public record SecurityPolicy(int thisPolicyFileCompliesToThePolicyVersion,
		@Nonnull SupervisedCode regardingTheSupervisedCode) {

	/**
	 * Constructs a SecurityPolicy instance with a validated policy-format version
	 * and validated supervised code.
	 * <p>
	 * The range check is written out here rather than delegated, so that reading
	 * this constructor shows the whole invariant. The builder's version setter
	 * carries the same check; the two must be kept in step by hand, and the
	 * {@code PolicyValueContractTest} covers both.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws IllegalArgumentException if the declared policy-format version lies
	 *                                  outside
	 *                                  [{@value AresConstants#MINIMUM_POLICY_VERSION},
	 *                                  {@value AresConstants#MAXIMUM_POLICY_VERSION}].
	 * @throws NullPointerException     if the supervised code is null.
	 */
	public SecurityPolicy {
		if (thisPolicyFileCompliesToThePolicyVersion < AresConstants.MINIMUM_POLICY_VERSION
				|| thisPolicyFileCompliesToThePolicyVersion > AresConstants.MAXIMUM_POLICY_VERSION) {
			throw new IllegalArgumentException("thisPolicyFileCompliesToThePolicyVersion must be between "
					+ AresConstants.MINIMUM_POLICY_VERSION + " and " + AresConstants.MAXIMUM_POLICY_VERSION
					+ " (inclusive), but was " + thisPolicyFileCompliesToThePolicyVersion);
		}
		Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null");
	}

	/**
	 * Creates a restrictive security policy with all permissions denied by default.
	 * <p>
	 * The returned policy declares {@value AresConstants#MAXIMUM_POLICY_VERSION} as
	 * its policy-format version.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param programmingLanguageConfiguration the programming language
	 *                                         configuration for the restrictive
	 *                                         policy; must not be null.
	 * @return a new SecurityPolicy instance.
	 * @throws NullPointerException if the programming language configuration is
	 *                              null.
	 */
	@Nonnull
	public static SecurityPolicy createRestrictive(
			@Nonnull ProgrammingLanguageConfiguration programmingLanguageConfiguration) {
		return builder().regardingTheSupervisedCode(SupervisedCode.createRestrictive(Objects
				.requireNonNull(programmingLanguageConfiguration, "programmingLanguageConfiguration must not be null")))
				.build();
	}

	/**
	 * Returns a builder for creating a SecurityPolicy instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new SecurityPolicy.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for SecurityPolicy.
	 * <p>
	 * Description: Provides a fluent API to construct a SecurityPolicy instance.
	 * <p>
	 * Design Rationale: The builder pattern here allows for step-by-step
	 * configuration of a SecurityPolicy, ensuring immutability. Each setter
	 * validates its argument immediately, so an invalid value is reported at the
	 * call that introduced it rather than at {@link #build()}.
	 *
	 * @since 2.0.0
	 * @version 2.1.0
	 * @author Markus Paulsen
	 */
	public static class Builder {

		/**
		 * The policy-format version declared by the policy under construction.
		 * <p>
		 * Defaults to {@value AresConstants#MAXIMUM_POLICY_VERSION}, because a policy
		 * assembled in code rather than read from a file is by construction expressed
		 * in the newest format this release understands.
		 */
		private int thisPolicyFileCompliesToThePolicyVersion = AresConstants.MAXIMUM_POLICY_VERSION;

		/**
		 * The supervised code for the SecurityPolicy.
		 */
		@Nullable
		private SupervisedCode regardingTheSupervisedCode;

		/**
		 * Sets the policy-format version declared by the policy file.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param thisPolicyFileCompliesToThePolicyVersion the declared policy-format
		 *                                                 version; must be between
		 *                                                 {@value AresConstants#MINIMUM_POLICY_VERSION}
		 *                                                 and
		 *                                                 {@value AresConstants#MAXIMUM_POLICY_VERSION},
		 *                                                 both inclusive.
		 * @return the updated Builder.
		 * @throws IllegalArgumentException if the version lies outside the supported
		 *                                  range.
		 */
		@Nonnull
		public Builder thisPolicyFileCompliesToThePolicyVersion(int thisPolicyFileCompliesToThePolicyVersion) {
			if (thisPolicyFileCompliesToThePolicyVersion < AresConstants.MINIMUM_POLICY_VERSION
					|| thisPolicyFileCompliesToThePolicyVersion > AresConstants.MAXIMUM_POLICY_VERSION) {
				throw new IllegalArgumentException("thisPolicyFileCompliesToThePolicyVersion must be between "
						+ AresConstants.MINIMUM_POLICY_VERSION + " and " + AresConstants.MAXIMUM_POLICY_VERSION
						+ " (inclusive), but was " + thisPolicyFileCompliesToThePolicyVersion);
			}
			this.thisPolicyFileCompliesToThePolicyVersion = thisPolicyFileCompliesToThePolicyVersion;
			return this;
		}

		/**
		 * Sets the supervised code for the SecurityPolicy.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param regardingTheSupervisedCode the supervised code instance; must not be
		 *                                   null.
		 * @return the updated Builder.
		 * @throws NullPointerException if the supervised code is null.
		 */
		@Nonnull
		public Builder regardingTheSupervisedCode(@Nonnull SupervisedCode regardingTheSupervisedCode) {
			this.regardingTheSupervisedCode = Objects.requireNonNull(regardingTheSupervisedCode,
					"regardingTheSupervisedCode must not be null");
			return this;
		}

		/**
		 * Builds a new SecurityPolicy instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new SecurityPolicy instance.
		 * @throws NullPointerException if no supervised code has been set.
		 */
		@Nonnull
		public SecurityPolicy build() {
			return new SecurityPolicy(thisPolicyFileCompliesToThePolicyVersion,
					Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null"));
		}
	}
}
