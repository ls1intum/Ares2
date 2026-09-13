package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBehaviorConfigurationTest {

	@Test
	void builderBuildsAnEmptyConfiguration() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNotNull(configuration);
	}

	@Test
	void literalFieldAssignmentsIsEmptyWhenNoCategoryIsConfigured() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertTrue(configuration.literalFieldAssignments().isEmpty());
	}
}
