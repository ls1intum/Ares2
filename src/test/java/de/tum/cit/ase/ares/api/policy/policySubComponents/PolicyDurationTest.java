package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.internal.TestGuardUtils;
import de.tum.cit.ase.ares.api.localization.Messages;

/** Checks the one duration grammar the policy uses. */
class PolicyDurationTest {

	/**
	 * Every {@code TimeUnit} name is read with a whole number before it.
	 *
	 * @param text  the text to read.
	 * @param nanos the expected duration in nanoseconds.
	 */
	@ParameterizedTest
	@CsvSource({ "2 NANOSECONDS, 2", "2 MICROSECONDS, 2000", "2 MILLISECONDS, 2000000", "2 SECONDS, 2000000000",
			"2 MINUTES, 120000000000", "2 HOURS, 7200000000000", "2 DAYS, 172800000000000", "0 SECONDS, 0" })
	void everyTimeUnitIsRead(String text, long nanos) {
		assertThat(PolicyDuration.tryParse(text)).contains(Duration.ofNanos(nanos));
	}

	/**
	 * Text outside the grammar is not a duration.
	 *
	 * @param text the text to refuse.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", " ", "2s", "2 seconds", "2 SECOND", "2", "SECONDS", "-1 SECONDS", "2  SECONDS",
			" 2 SECONDS", "2 SECONDS ", "1 DAYS 12 HOURS", "1.5 SECONDS", "2 WEEKS", "9223372036854775807 DAYS",
			"99999999999999999999 SECONDS" })
	void invalidTextIsNotADuration(String text) {
		assertThat(PolicyDuration.tryParse(text)).isEmpty();
	}

	/** A null text is not a duration. */
	@Test
	void nullIsNotADuration() {
		assertThat(PolicyDuration.tryParse(null)).isEmpty();
	}

	/** The refusal names the field, the value and the seven units. */
	@Test
	void theRefusalNamesTheFieldAndTheUnits() {
		assertThatThrownBy(() -> PolicyDuration.parse("theTimeoutIs", "2s"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(Messages.localized("policy.behavior.duration.invalid", "theTimeoutIs", "2s"))
				.hasMessageContaining("theTimeoutIs").hasMessageContaining("NANOSECONDS").hasMessageContaining("DAYS");
	}

	/**
	 * Every form {@code @ExtendedDeadline} accepted before still parses the same,
	 * since the annotation keeps its own format.
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
