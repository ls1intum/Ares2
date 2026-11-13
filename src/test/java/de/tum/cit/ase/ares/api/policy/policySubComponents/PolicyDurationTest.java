package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.internal.TestGuardUtils;

/** Checks the one duration grammar the policy uses. */
class PolicyDurationTest {

	/**
	 * Every unit, combined in order, with and without spaces and in any case.
	 *
	 * @param text   the text to read.
	 * @param millis the expected duration in milliseconds.
	 */
	@ParameterizedTest
	@CsvSource({ "2s, 2000", "500ms, 500", "1d 12h, 129600000", "1d12h30m, 131400000", "1m 30s 5ms, 90005",
			"5M, 300000", "5MS, 5", "5Ms, 5", "1D 12H 30M, 131400000", "0s, 0" })
	void validDurationsAreRead(String text, long millis) {
		assertThat(PolicyDuration.tryParse(text)).contains(Duration.ofMillis(millis));
	}

	/**
	 * Text outside the grammar is not a duration.
	 *
	 * @param text the text to refuse.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", " ", "2", "soon", "1s 1m", "1s 2s", "-1s", "1.5s", "1 s x", "1ns",
			"9999999999999999999d" })
	void invalidTextIsNotADuration(String text) {
		assertThat(PolicyDuration.tryParse(text)).isEmpty();
	}

	/** A null text is not a duration. */
	@Test
	void nullIsNotADuration() {
		assertThat(PolicyDuration.tryParse(null)).isEmpty();
	}

	/** The refusal names the field. */
	@Test
	void theRefusalNamesTheField() {
		assertThatThrownBy(() -> PolicyDuration.parse("theTimeoutIs", "soon"))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theTimeoutIs")
				.hasMessageContaining("soon");
	}

	/**
	 * Every form {@code @ExtendedDeadline} accepted before still parses the same.
	 *
	 * @param text    the extension.
	 * @param minutes the expected duration in minutes.
	 */
	@ParameterizedTest
	@CsvSource({ "1d, 1440", "12h, 720", "30m, 30", "1d 2h 3m, 1563", "2D, 2880" })
	void extendedDeadlineFormsStillParse(String text, long minutes) {
		assertThat(TestGuardUtils.parseDuration(text)).isEqualTo(Duration.ofMinutes(minutes));
	}
}
