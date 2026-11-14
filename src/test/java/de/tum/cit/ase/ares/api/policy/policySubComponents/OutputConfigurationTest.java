package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.MirrorOutput;

/**
 * Checks the policy's output-mirroring category: its defaults, and that it
 * refuses a limit Ares could not enforce.
 */
class OutputConfigurationTest {

	/**
	 * Without values, output is not mirrored and the annotation's limit applies.
	 */
	@Test
	void defaultsMatchNoAnnotation() {
		OutputConfiguration configuration = OutputConfiguration.builder().build();

		assertThat(configuration.mirrored()).isFalse();
		assertThat(configuration.maximumCharacterCount()).isEqualTo(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	/** Configured values are reported. */
	@Test
	void configuredValuesAreReported() {
		OutputConfiguration configuration = OutputConfiguration.builder().theOutputIsMirrored(true)
				.theMaximumNumberOfOutputCharactersIs(10L).build();

		assertThat(configuration.mirrored()).isTrue();
		assertThat(configuration.maximumCharacterCount()).isEqualTo(10);
	}

	/**
	 * A limit that is not positive is refused and named.
	 *
	 * @param limit the limit.
	 */
	@ParameterizedTest
	@ValueSource(longs = { 0, -1 })
	void aLimitThatIsNotPositiveIsRefused(long limit) {
		assertThatThrownBy(() -> OutputConfiguration.builder().theMaximumNumberOfOutputCharactersIs(limit).build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("theMaximumNumberOfOutputCharactersIs");
	}

	/** The refusal is localised: in German it still names the field. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> OutputConfiguration.builder().theMaximumNumberOfOutputCharactersIs(0L).build())
					.hasMessageStartingWith("Ares Sicherheitsfehler")
					.hasMessageContaining("theMaximumNumberOfOutputCharactersIs");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}
}
