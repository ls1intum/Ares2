package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Checks the strict-timeout record's durations and refusals. */
class StrictTimeoutConfigurationTest {

	/** The display locale, which Ares's messages read, before a test changed it. */
	private final Locale previous = Locale.getDefault(Locale.Category.DISPLAY);

	/**
	 * Restores the display locale only, leaving the other locale categories alone.
	 */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(Locale.Category.DISPLAY, previous);
	}

	/** A timeout and a grace period read as durations. */
	@Test
	void durationsAreRead() {
		StrictTimeoutConfiguration configuration = StrictTimeoutConfiguration.builder().theTimeoutIs("90 SECONDS")
				.theTerminationGraceIs("250 MILLISECONDS").build();
		assertThat(configuration.timeout()).isEqualTo(Duration.ofSeconds(90));
		assertThat(configuration.terminationGrace()).contains(Duration.ofMillis(250));
	}

	/** Without a grace period the record reports none, not zero. */
	@Test
	void aMissingGraceIsEmpty() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS").build().terminationGrace()).isEmpty();
	}

	/** A grace period of zero is configured, not absent. */
	@Test
	void aZeroGraceIsKept() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS")
				.theTerminationGraceIs("0 MILLISECONDS").build().terminationGrace()).contains(Duration.ZERO);
	}

	/**
	 * A missing, malformed or zero timeout is refused.
	 *
	 * @param timeout the timeout to refuse.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "soon", "0 SECONDS", "-1 SECONDS", "2s", "2 seconds", "1 DAYS 12 HOURS",
			"99999999999999999999 DAYS", "106752 DAYS" })
	void anInvalidTimeoutIsRefused(String timeout) {
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs(timeout).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theTimeoutIs");
	}

	/** The shortest timeout, one nanosecond, is accepted. */
	@Test
	void aOneNanosecondTimeoutIsAccepted() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("1 NANOSECONDS").build().timeout())
				.isEqualTo(Duration.ofNanos(1));
	}

	/** A grace period of exactly one day is accepted. */
	@Test
	void aGraceOfOneDayIsAccepted() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS").theTerminationGraceIs("1 DAYS")
				.build().terminationGrace()).contains(Duration.ofDays(1));
	}

	/** A grace period of 25 hours is refused. */
	@Test
	void aGraceOf25HoursIsRefused() {
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS")
				.theTerminationGraceIs("25 HOURS").build()).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("theTerminationGraceIs");
	}

	/** A grace period above one day is refused, naming the field. */
	@Test
	void aGraceAboveOneDayIsRefused() {
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS")
				.theTerminationGraceIs("86400001 MILLISECONDS").build()).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("theTerminationGraceIs").hasMessageContaining("at most one day");
	}

	/** The refusal is localised in German too. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs("0 SECONDS").build())
				.hasMessageContaining("Ares Sicherheitsfehler");
	}
}
