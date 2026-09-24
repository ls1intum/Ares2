package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks the privileged-exceptions category and its default message. */
class PrivilegedExceptionsConfigurationTest {

	/** A blank message falls back to the default. */
	@Test
	void constructorDefaultsBlankFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, "  ");

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}

	/** A missing message falls back to the default. */
	@Test
	void constructorDefaultsNullFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, null);

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}

	/** Given values are kept as they are. */
	@Test
	void constructorAcceptsValidValues() {
		PrivilegedExceptionsConfiguration configuration = new PrivilegedExceptionsConfiguration(true, "Test failed.");

		assertTrue(configuration.onlyPrivilegedExceptionsAreReported());
		assertEquals("Test failed.", configuration.theFailureMessageIs());
	}

	/** The builder keeps both fields. */
	@Test
	void builderRoundTripsBothFields() {
		PrivilegedExceptionsConfiguration configuration = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(false).theFailureMessageIs("Custom message").build();

		assertFalse(configuration.onlyPrivilegedExceptionsAreReported());
		assertEquals("Custom message", configuration.theFailureMessageIs());
	}

	/** The builder falls back to the default message when none is set. */
	@Test
	void builderDefaultsOmittedFailureMessage() {
		PrivilegedExceptionsConfiguration configuration = PrivilegedExceptionsConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).build();

		assertEquals(PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE, configuration.theFailureMessageIs());
	}
}
