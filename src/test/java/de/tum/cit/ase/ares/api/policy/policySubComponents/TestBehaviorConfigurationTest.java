package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(configuration.regardingOutputMirroring()).isNull();
		assertThat(configuration.literalFieldAssignments()).isEmpty();
	}

	/** The carrier lives outside the sealed Ares packages, in the reserved one. */
	@Test
	void theCarrierLivesInTheGeneratedPackage() {
		assertThat(TestBehaviorConfiguration.GENERATED_CLASS_NAME)
				.isEqualTo("de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings");
	}

	/**
	 * An empty category contributes its two constants with the defaults applied.
	 */
	@Test
	void anEmptyCategoryContributesItsDefaults() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingOutputMirroring(OutputMirroringConfiguration.builder().build()).build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("public static final boolean REGARDING_OUTPUT_MIRRORING_THE_OUTPUT_IS_MIRRORED = false;")
				.contains(
						"public static final long REGARDING_OUTPUT_MIRRORING_THE_MAXIMUM_CHARACTER_COUNT_IS = 100000000L;");
	}

	/** Configured values are written as they are. */
	@Test
	void configuredValuesAreWritten() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingOutputMirroring(OutputMirroringConfiguration.builder().theOutputIsMirrored(true)
						.theMaximumCharacterCountIs(10L).build())
				.build();

		assertThat(String.join("", configuration.literalFieldAssignments()))
				.contains("REGARDING_OUTPUT_MIRRORING_THE_OUTPUT_IS_MIRRORED = true;")
				.contains("REGARDING_OUTPUT_MIRRORING_THE_MAXIMUM_CHARACTER_COUNT_IS = 10L;");
	}
}
