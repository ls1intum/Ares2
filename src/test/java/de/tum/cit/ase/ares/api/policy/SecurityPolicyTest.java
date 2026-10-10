package de.tum.cit.ase.ares.api.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityPolicyPreset;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

class SecurityPolicyTest {

	private static SupervisedCode restrictiveSupervisedCode() {
		return SupervisedCode.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
	}

	@Test
	void rejectsAWrongPolicyVersion() {
		SupervisedCode supervisedCode = restrictiveSupervisedCode();
		assertThrows(IllegalArgumentException.class, () -> new SecurityPolicy(2, supervisedCode, null));
	}

	@Test
	void twoArgumentConstructorLeavesThePresetUnconfigured() {
		SupervisedCode supervisedCode = restrictiveSupervisedCode();
		SecurityPolicy policy = new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION, supervisedCode);
		assertThat(policy.basedOnTheFollowingPreset()).isNull();
		assertThat(policy.regardingTheSupervisedCode()).isSameAs(supervisedCode);
	}

	@Test
	void builderSetsTheConfiguredPreset() {
		SupervisedCode supervisedCode = restrictiveSupervisedCode();
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();
		assertThat(policy.basedOnTheFollowingPreset()).isEqualTo(SecurityPolicyPreset.SMOKE_TEST);
	}

	@Test
	void builderDefaultsToNoPreset() {
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(restrictiveSupervisedCode())
				.build();
		assertThat(policy.basedOnTheFollowingPreset()).isNull();
	}
}
