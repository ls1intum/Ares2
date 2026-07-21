package de.tum.cit.ase.ares.api.policy;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
 *                                                 {@value #MINIMUM_POLICY_VERSION}
 *                                                 and
 *                                                 {@value #MAXIMUM_POLICY_VERSION},
 *                                                 both inclusive.
 * @param regardingTheSupervisedCode               the details of the supervised
 *                                                 code; must not be null.
 */
@SuppressWarnings("unused")
public record SecurityPolicy(int thisPolicyFileCompliesToThePolicyVersion,
		@Nonnull SupervisedCode regardingTheSupervisedCode) {

	/**
	 * The earliest policy-format version supported by this Ares release.
	 *
	 * @since 2.1.0
	 */
	public static final int MINIMUM_POLICY_VERSION = 1;

	/**
	 * The latest policy-format version supported by this Ares release.
	 * <p>
	 * This is the version assumed for policies that are built programmatically
	 * without declaring one, as such policies are by construction expressed in the
	 * newest format this release understands.
	 *
	 * @since 2.1.0
	 */
	public static final int MAXIMUM_POLICY_VERSION = 1;

	/*
	 * Guards against an edit that leaves the two ends of the supported range
	 * inconsistent. Such a range would reject every policy version, so the cause is
	 * stated here rather than inferred from a suite that fails everywhere at once.
	 */
	static {
		if (MINIMUM_POLICY_VERSION > MAXIMUM_POLICY_VERSION) {
			throw new ExceptionInInitializerError("MINIMUM_POLICY_VERSION must not exceed MAXIMUM_POLICY_VERSION");
		}
	}

	/**
	 * Constructs a SecurityPolicy instance with a validated policy-format version
	 * and validated supervised code.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws IllegalArgumentException if the declared policy-format version lies
	 *                                  outside [{@value #MINIMUM_POLICY_VERSION},
	 *                                  {@value #MAXIMUM_POLICY_VERSION}].
	 * @throws NullPointerException     if the supervised code is null.
	 */
	public SecurityPolicy {
		requireSupportedPolicyVersion(thisPolicyFileCompliesToThePolicyVersion);
		Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null");
	}

	/**
	 * Checks that a policy-format version is supported by this Ares release.
	 * <p>
	 * Description: Single source of truth for the version invariant, shared by the
	 * canonical constructor and the builder so that both reject the same values
	 * with the same message.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param policyVersion the declared policy-format version.
	 * @return the unchanged policy version, so that the check can be used inline.
	 * @throws IllegalArgumentException if the version lies outside
	 *                                  [{@value #MINIMUM_POLICY_VERSION},
	 *                                  {@value #MAXIMUM_POLICY_VERSION}].
	 */
	private static int requireSupportedPolicyVersion(int policyVersion) {
		if (policyVersion < MINIMUM_POLICY_VERSION || policyVersion > MAXIMUM_POLICY_VERSION) {
			throw new IllegalArgumentException(
					"thisPolicyFileCompliesToThePolicyVersion must be between " + MINIMUM_POLICY_VERSION + " and "
							+ MAXIMUM_POLICY_VERSION + " (inclusive), but was " + policyVersion);
		}
		return policyVersion;
	}

	/**
	 * Creates a restrictive security policy with all permissions denied by default.
	 * <p>
	 * The returned policy declares {@value #MAXIMUM_POLICY_VERSION} as its
	 * policy-format version.
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
		 * Defaults to {@value SecurityPolicy#MAXIMUM_POLICY_VERSION}, because a policy
		 * assembled in code rather than read from a file is by construction expressed
		 * in the newest format this release understands.
		 */
		private int thisPolicyFileCompliesToThePolicyVersion = MAXIMUM_POLICY_VERSION;

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
		 *                                                 {@value SecurityPolicy#MINIMUM_POLICY_VERSION}
		 *                                                 and
		 *                                                 {@value SecurityPolicy#MAXIMUM_POLICY_VERSION},
		 *                                                 both inclusive.
		 * @return the updated Builder.
		 * @throws IllegalArgumentException if the version lies outside the supported
		 *                                  range.
		 */
		@Nonnull
		public Builder thisPolicyFileCompliesToThePolicyVersion(int thisPolicyFileCompliesToThePolicyVersion) {
			this.thisPolicyFileCompliesToThePolicyVersion = requireSupportedPolicyVersion(
					thisPolicyFileCompliesToThePolicyVersion);
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
