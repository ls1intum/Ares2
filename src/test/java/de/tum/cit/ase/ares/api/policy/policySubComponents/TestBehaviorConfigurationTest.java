package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class TestBehaviorConfigurationTest {

	@Test
	void builderDefaultsToNullCategory() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNull(configuration.regardingPrivilegedExceptions());
	}

	@Test
	void builderRoundTripsPrivilegedExceptionsCategory() {
		PrivilegedExceptionsConfiguration privilegedExceptions = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).theFailureMessageIs("Test failed.").build();

		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingPrivilegedExceptions(privilegedExceptions).build();

		assertNotNull(configuration.regardingPrivilegedExceptions());
		assertSame(privilegedExceptions, configuration.regardingPrivilegedExceptions());
	}
}
