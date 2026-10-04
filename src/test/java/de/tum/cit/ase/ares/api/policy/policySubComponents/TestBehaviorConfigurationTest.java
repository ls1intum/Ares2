package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * Checks the behavioural wrapper and the literal constants a precompile run
 * generates from it.
 */
class TestBehaviorConfigurationTest {

	/** Without a category, the wrapper holds none and contributes no constant. */
	@Test
	void anEmptyConfigurationContributesNothing() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertThat(configuration.regardingHiddenTests()).isNull();
		assertThat(configuration.literalFieldAssignments()).isEmpty();
	}

	/** The carrier lives outside the sealed Ares packages, in the reserved one. */
	@Test
	void theCarrierLivesInTheGeneratedPackage() {
		assertThat(TestBehaviorConfiguration.GENERATED_CLASS_NAME)
				.isEqualTo("de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings");
	}

	/** A deadline alone gives the deadline, no always-run date and no list. */
	@Test
	void aDeadlineAloneContributesItsDefaults() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingHiddenTests(HiddenTestsConfiguration.builder().theDeadlineIs("2000-01-01 00:00 UTC").build())
				.build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("public static final long REGARDING_HIDDEN_TESTS_THE_DEADLINE_IS = 946684800000L;")
				.contains(
						"public static final long REGARDING_HIDDEN_TESTS_ALWAYS_RUN_BEFORE = " + Long.MIN_VALUE + "L;")
				.contains("public static final String REGARDING_HIDDEN_TESTS_THE_FOLLOWING_TESTS_ARE_HIDDEN = \"\";");
	}

	/**
	 * The extension is folded into the deadline, and the list is one line per
	 * entry.
	 */
	@Test
	void configuredValuesAreWritten() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingHiddenTests(HiddenTestsConfiguration.builder().theDeadlineIs("2000-01-01 00:00 UTC")
						.theDeadlineIsExtendedBy("1d").hiddenTestsAlwaysRunBefore("1990-01-01 00:00 UTC")
						.hiddenTests("org.example.A", "org.example.B#m").build())
				.build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("REGARDING_HIDDEN_TESTS_THE_DEADLINE_IS = "
						+ ZonedDateTime.parse("2000-01-02T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains("REGARDING_HIDDEN_TESTS_ALWAYS_RUN_BEFORE = "
						+ ZonedDateTime.parse("1990-01-01T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains(
						"REGARDING_HIDDEN_TESTS_THE_FOLLOWING_TESTS_ARE_HIDDEN = \"org.example.A\\norg.example.B#m\";");
	}
}
