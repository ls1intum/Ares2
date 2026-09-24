package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Checks the behaviour configuration and the constants it contributes to the
 * generated settings class.
 */
class TestBehaviorConfigurationTest {

	/** A configuration built empty has no privileged-exceptions category. */
	@Test
	void builderDefaultsToNullCategory() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertNull(configuration.regardingPrivilegedExceptions());
	}

	/** The builder keeps the privileged-exceptions category it was given. */
	@Test
	void builderRoundTripsPrivilegedExceptionsCategory() {
		PrivilegedExceptionsConfiguration privilegedExceptions = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).theFailureMessageIs("Test failed.").build();

		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingPrivilegedExceptions(privilegedExceptions).build();

		assertNotNull(configuration.regardingPrivilegedExceptions());
		assertSame(privilegedExceptions, configuration.regardingPrivilegedExceptions());
	}

	@Test
	void literalFieldAssignmentsIsEmptyWhenNoCategoryIsConfigured() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertTrue(configuration.literalFieldAssignments().isEmpty());
	}
}
