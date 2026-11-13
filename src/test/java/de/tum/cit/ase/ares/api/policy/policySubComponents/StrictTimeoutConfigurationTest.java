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

	/** The default locale before a test changed it. */
	private final Locale previous = Locale.getDefault();

	/** Restores the default locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

	/** A timeout and a grace period read as durations. */
	@Test
	void durationsAreRead() {
		StrictTimeoutConfiguration configuration = StrictTimeoutConfiguration.builder().theTimeoutIs("1m 30s")
				.theTerminationGraceIs("250ms").build();
		assertThat(configuration.timeout()).isEqualTo(Duration.ofSeconds(90));
		assertThat(configuration.terminationGrace()).contains(Duration.ofMillis(250));
	}

	/** Without a grace period the record reports none, not zero. */
	@Test
	void aMissingGraceIsEmpty() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("2s").build().terminationGrace()).isEmpty();
	}

	/** A grace period of zero is configured, not absent. */
	@Test
	void aZeroGraceIsKept() {
		assertThat(StrictTimeoutConfiguration.builder().theTimeoutIs("2s").theTerminationGraceIs("0ms").build()
				.terminationGrace()).contains(Duration.ZERO);
	}

	/**
	 * A missing, malformed or zero timeout is refused.
	 *
	 * @param timeout the timeout to refuse.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "soon", "0s", "-1s", "1s 1m", "1s 2s", "99999999999999999999d", "106752d" })
	void anInvalidTimeoutIsRefused(String timeout) {
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs(timeout).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theTimeoutIs");
	}

	/** A grace period above one day is refused, naming the field. */
	@Test
	void aGraceAboveOneDayIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(
				() -> StrictTimeoutConfiguration.builder().theTimeoutIs("2s").theTerminationGraceIs("1d 1ms").build())
						.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theTerminationGraceIs")
						.hasMessageContaining("at most one day");
	}

	/** The refusal is localised in German too. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThatThrownBy(() -> StrictTimeoutConfiguration.builder().theTimeoutIs("0s").build())
				.hasMessageContaining("Ares Sicherheitsfehler");
	}
}
