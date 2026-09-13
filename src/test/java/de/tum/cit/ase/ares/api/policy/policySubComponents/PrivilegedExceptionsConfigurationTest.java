package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PrivilegedExceptionsConfigurationTest {

	@Test
	void constructorDefaultsBlankFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, "  ");

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}

	@Test
	void constructorDefaultsNullFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, null);

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}

	@Test
	void constructorAcceptsValidValues() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, "Test failed.");

		assertTrue(configuration.onlyPrivilegedExceptionsAreReported());
		assertEquals("Test failed.", configuration.theFailureMessageIs());
	}

	@Test
	void builderRoundTripsBothFields() {
		PrivilegedExceptionsConfiguration configuration = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(false).theFailureMessageIs("Custom message").build();

		assertFalse(configuration.onlyPrivilegedExceptionsAreReported());
		assertEquals("Custom message", configuration.theFailureMessageIs());
	}

	@Test
	void builderDefaultsOmittedFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).build();

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}
}
