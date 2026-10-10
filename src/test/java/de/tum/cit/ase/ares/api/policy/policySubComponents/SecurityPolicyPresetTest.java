package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityPolicyPresetTest {

	@Test
	void resourceFileNameReturnsTheConfiguredFileName() {
		assertThat(SecurityPolicyPreset.SMOKE_TEST.resourceFileName()).isEqualTo("smoke-test.yaml");
	}
}
