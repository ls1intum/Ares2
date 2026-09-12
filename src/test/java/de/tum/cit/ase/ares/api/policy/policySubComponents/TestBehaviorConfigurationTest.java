package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TestBehaviorConfigurationTest {

	@Test
	void builderBuildsAnEmptyConfiguration() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNotNull(configuration);
	}
}
