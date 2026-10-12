package de.tum.cit.ase.ares.api.policy;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityPolicyPreset;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Immutable security policy for supervised code execution.
 * <p>
 * Description: This record encapsulates all necessary details regarding
 * supervised code, including its programming language configuration, permitted
 * resource accesses, and additional metadata. By using the immutable record
 * pattern, it guarantees thread-safety and reduces boilerplate. Input
 * validation is performed in factory methods to ensure that every instance
 * meets its invariants.
 * <p>
 * Design Rationale: Leveraging modern Java features such as records and
 * nullability annotations enforces immutability and clarity. The use of a
 * factory method for construction and in‑constructor validation ensures that
 * only valid, consistent instances are created, aligning with best practices
 * for secure and maintainable design. The clear separation into nested records
 * reflects the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param thisPolicyFileCompliesToThePolicyVersion the policy format version;
 *                                                 must be exactly
 *                                                 {@value #CURRENT_POLICY_VERSION}.
 * @param regardingTheSupervisedCode               the details of the supervised
 *                                                 code; must not be null.
 * @param basedOnTheFollowingPreset                the named preset this policy
 *                                                 is merged on top of; null
 *                                                 when no preset is used.
 */
@SuppressWarnings("unused")
public record SecurityPolicy(int thisPolicyFileCompliesToThePolicyVersion,
		@Nonnull SupervisedCode regardingTheSupervisedCode, @Nullable SecurityPolicyPreset basedOnTheFollowingPreset) {

	/** The policy-format version supported by this Ares release. */
	public static final int CURRENT_POLICY_VERSION = 1;

	/**
	 * Constructs a SecurityPolicy instance with validated supervised code.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public SecurityPolicy {
		if (thisPolicyFileCompliesToThePolicyVersion != CURRENT_POLICY_VERSION) {
			throw new IllegalArgumentException(
					"thisPolicyFileCompliesToThePolicyVersion must be exactly " + CURRENT_POLICY_VERSION);
		}
		Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null");
	}

	/**
	 * Constructs a SecurityPolicy instance with no preset, for source and binary
	 * compatibility with code built against the two-argument constructor released
	 * before {@link #basedOnTheFollowingPreset} existed.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param thisPolicyFileCompliesToThePolicyVersion the policy format version;
	 *                                                 must be exactly
	 *                                                 {@value #CURRENT_POLICY_VERSION}.
	 * @param regardingTheSupervisedCode               the details of the supervised
	 *                                                 code; must not be null.
	 */
	public SecurityPolicy(int thisPolicyFileCompliesToThePolicyVersion,
			@Nonnull SupervisedCode regardingTheSupervisedCode) {
		this(thisPolicyFileCompliesToThePolicyVersion, regardingTheSupervisedCode, null);
	}

	/**
	 * Creates a restrictive security policy with all permissions denied by default.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param programmingLanguageConfiguration the programming language
	 *                                         configuration for the restrictive
	 *                                         policy.
	 * @return a new SecurityPolicy instance.
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
	 * configuration of a SecurityPolicy, ensuring immutability.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @version 2.0.0
	 */
	public static class Builder {

		private int thisPolicyFileCompliesToThePolicyVersion = CURRENT_POLICY_VERSION;

		/**
		 * The supervised code for the SecurityPolicy.
		 */
		@Nullable
		private SupervisedCode regardingTheSupervisedCode;

		/**
		 * The named preset this policy is merged on top of, or null when none is used.
		 */
		@Nullable
		private SecurityPolicyPreset basedOnTheFollowingPreset;

		/**
		 * Sets the policy-format version declared by the policy file.
		 *
		 * @param thisPolicyFileCompliesToThePolicyVersion the declared policy-format
		 *                                                 version
		 * @return this builder
		 * @since 2.0.0
		 * @author Markus Paulsen
		 */
		@Nonnull
		public Builder thisPolicyFileCompliesToThePolicyVersion(int thisPolicyFileCompliesToThePolicyVersion) {
			this.thisPolicyFileCompliesToThePolicyVersion = thisPolicyFileCompliesToThePolicyVersion;
			return this;
		}

		/**
		 * Sets the supervised code for the SecurityPolicy.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param regardingTheSupervisedCode the supervised code instance.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingTheSupervisedCode(@Nonnull SupervisedCode regardingTheSupervisedCode) {
			this.regardingTheSupervisedCode = Objects.requireNonNull(regardingTheSupervisedCode,
					"regardingTheSupervisedCode must not be null");
			return this;
		}

		/**
		 * Sets the named preset this policy is merged on top of.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param basedOnTheFollowingPreset the preset to use; null for none.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder basedOnTheFollowingPreset(@Nullable SecurityPolicyPreset basedOnTheFollowingPreset) {
			this.basedOnTheFollowingPreset = basedOnTheFollowingPreset;
			return this;
		}

		/**
		 * Builds a new SecurityPolicy instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new SecurityPolicy instance.
		 */
		@Nonnull
		public SecurityPolicy build() {
			return new SecurityPolicy(thisPolicyFileCompliesToThePolicyVersion,
					Objects.requireNonNull(regardingTheSupervisedCode, "regardingTheSupervisedCode must not be null"),
					basedOnTheFollowingPreset);
		}
	}
}
