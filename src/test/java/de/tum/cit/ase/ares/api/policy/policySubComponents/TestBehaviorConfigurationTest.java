package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Checks the behavioural wrapper and the literal constants a precompile run
 * generates from it.
 */
class TestBehaviorConfigurationTest {

	/** The released no-argument constructor still creates an empty setting. */
	@Test
	void noArgumentConstructorPreservesCompatibility() {
		TestBehaviorConfiguration configuration = new TestBehaviorConfiguration();
		assertThat(configuration.literalFieldAssignments()).isEmpty();
	}

	/** Without a category, the wrapper holds none and contributes no constant. */
	@Test
	void anEmptyConfigurationContributesNothing() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder().build();

		assertThat(configuration.regardingStrictTimeouts()).isNull();
		assertThat(configuration.literalFieldAssignments()).isEmpty();
	}

	/** The carrier lives outside the sealed Ares packages, in the reserved one. */
	@Test
	void theCarrierLivesInTheGeneratedPackage() {
		assertThat(TestBehaviorConfiguration.GENERATED_CLASS_NAME)
				.isEqualTo("de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings");
	}

	/** A configured timeout contributes its four constants, units by name. */
	@Test
	void aStrictTimeoutContributesItsConstants() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingStrictTimeouts(StrictTimeoutsConfiguration.builder().theTimeoutIs(2)
						.theTimeUnitIs(TimeUnit.MINUTES).theTerminationGraceIs(200L).build())
				.build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("public static final long REGARDING_STRICT_TIMEOUTS_THE_TIMEOUT_IS = 2L;")
				.contains("public static final String REGARDING_STRICT_TIMEOUTS_THE_TIME_UNIT_IS = \"MINUTES\";")
				.contains("public static final long REGARDING_STRICT_TIMEOUTS_THE_TERMINATION_GRACE_IS = 200L;")
				.contains(
						"public static final String REGARDING_STRICT_TIMEOUTS_THE_TERMINATION_GRACE_UNIT_IS = \"MILLISECONDS\";");
	}

	/** An unconfigured grace period is written as -1, meaning "not configured". */
	@Test
	void anUnconfiguredGraceIsWrittenAsMinusOne() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingStrictTimeouts(StrictTimeoutsConfiguration.builder().theTimeoutIs(2).build()).build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("REGARDING_STRICT_TIMEOUTS_THE_TERMINATION_GRACE_IS = -1L;");
	}
}
