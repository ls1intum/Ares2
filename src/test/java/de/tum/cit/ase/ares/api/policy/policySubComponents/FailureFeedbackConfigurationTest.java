package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Checks the failure-feedback category, whose every field is optional. */
class FailureFeedbackConfigurationTest {

	/** An empty category means today's behaviour. */
	@Test
	void anEmptyCategoryMeansTodaysBehaviour() {
		FailureFeedbackConfiguration configuration = FailureFeedbackConfiguration.builder().build();

		assertFalse(configuration.privilegedOnly());
		assertEquals(FailureFeedbackConfiguration.DEFAULT_FAILURE_MESSAGE,
				configuration.theFailureMessageForOtherExceptionsIs());
		assertEquals(SecurityViolationFeedbackLevel.FULL, configuration.feedbackLevel());
	}

	/** A blank message falls back to the default. */
	@Test
	void aBlankMessageDefaults() {
		assertEquals(FailureFeedbackConfiguration.DEFAULT_FAILURE_MESSAGE,
				new FailureFeedbackConfiguration(true, "  ", null).theFailureMessageForOtherExceptionsIs());
	}

	/** Given values are kept as they are. */
	@Test
	void givenValuesAreKept() {
		FailureFeedbackConfiguration configuration = FailureFeedbackConfiguration.builder()
				.onlyPrivilegedExceptionsAreReported(true).theFailureMessageForOtherExceptionsIs("Custom message")
				.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.MINIMIZED).build();

		assertTrue(configuration.privilegedOnly());
		assertEquals("Custom message", configuration.theFailureMessageForOtherExceptionsIs());
		assertEquals(SecurityViolationFeedbackLevel.MINIMIZED, configuration.feedbackLevel());
	}

	/** Only the level set, privileged-only reporting stays off. */
	@Test
	void theLevelAloneLeavesPrivilegedOnlyOff() {
		FailureFeedbackConfiguration configuration = FailureFeedbackConfiguration.builder()
				.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.DEBUG).build();

		assertFalse(configuration.privilegedOnly());
		assertEquals(SecurityViolationFeedbackLevel.DEBUG, configuration.feedbackLevel());
	}
}
