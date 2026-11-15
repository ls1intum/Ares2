package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.localization.Messages;

class TestBehaviorConfigurationTest {

	/** The locale before each test, restored afterwards. */
	private final Locale previous = Locale.getDefault();

	/** Puts the default locale back. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

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

	/** Postcompile reads the category directly, so configuring it never throws. */
	@Test
	void failureFeedbackStaysReadableOutsidePrecompile() {
		TestBehaviorConfiguration configuration = withFailureFeedback();

		assertThat(configuration.regardingTheFailureFeedbackForTheTestCases().feedbackLevel())
				.isEqualTo(SecurityViolationFeedbackLevel.MINIMIZED);
	}

	/** Precompile cannot carry failure feedback yet, so it refuses it by name. */
	@Test
	void failureFeedbackIsRefusedWhenPrecompileWritesTheSettings() {
		Locale.setDefault(Locale.ENGLISH);

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> withFailureFeedback().literalFieldAssignments());

		assertThat(refusal.getMessage()).isEqualTo(Messages.localized("security.creation.test.behavior.unsupported",
				TestBehaviorFieldNames.FAILURE_FEEDBACK)).contains(TestBehaviorFieldNames.FAILURE_FEEDBACK);
	}

	/** The refusal is localised in German. */
	@Test
	void failureFeedbackRefusalIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> withFailureFeedback().literalFieldAssignments());

		assertThat(refusal.getMessage())
				.isEqualTo(Messages.localized("security.creation.test.behavior.unsupported",
						TestBehaviorFieldNames.FAILURE_FEEDBACK))
				.startsWith("Ares Sicherheitsfehler").contains(TestBehaviorFieldNames.FAILURE_FEEDBACK);
	}

	/** A configuration with only the feedback level set to MINIMIZED. */
	private static TestBehaviorConfiguration withFailureFeedback() {
		return TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
						.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.MINIMIZED).build())
				.build();
	}
}
