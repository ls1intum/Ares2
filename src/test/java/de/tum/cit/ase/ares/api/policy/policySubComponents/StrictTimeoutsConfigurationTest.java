package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks the policy's strict-timeout category: its defaults, and that it
 * refuses every value Ares could not enforce.
 */
class StrictTimeoutsConfigurationTest {

	/** Without units, the timeout counts in seconds and the grace is unset. */
	@Test
	void defaultsMatchTheAnnotation() {
		StrictTimeoutsConfiguration configuration = StrictTimeoutsConfiguration.builder().theTimeoutIs(3).build();

		assertThat(configuration.theTimeUnitIs()).isEqualTo(TimeUnit.SECONDS);
		assertThat(configuration.timeout()).isEqualTo(Duration.ofSeconds(3));
		assertThat(configuration.theTerminationGraceIs()).isNull();
		assertThat(configuration.terminationGrace()).isEmpty();
		assertThat(configuration.theTerminationGraceUnitIs()).isEqualTo(TimeUnit.MILLISECONDS);
	}

	/** A configured grace period is reported in its own unit. */
	@Test
	void aConfiguredGraceIsReported() {
		StrictTimeoutsConfiguration configuration = StrictTimeoutsConfiguration.builder().theTimeoutIs(1)
				.theTerminationGraceIs(500L).theTerminationGraceUnitIs(TimeUnit.MICROSECONDS).build();

		assertThat(configuration.terminationGrace()).contains(Duration.ofNanos(500_000));
	}

	/** Zero grace means "stop at once", which is a configured value. */
	@Test
	void zeroGraceIsConfigured() {
		assertThat(StrictTimeoutsConfiguration.builder().theTimeoutIs(1).theTerminationGraceIs(0L).build()
				.terminationGrace()).contains(Duration.ZERO);
	}

	/** Exactly one day of grace is the largest allowed. */
	@Test
	void oneDayOfGraceIsAllowed() {
		assertThat(StrictTimeoutsConfiguration.builder().theTimeoutIs(1).theTerminationGraceIs(1L)
				.theTerminationGraceUnitIs(TimeUnit.DAYS).build().terminationGrace()).contains(Duration.ofDays(1));
	}

	/**
	 * A timeout that is not positive, or does not fit a duration, is refused and
	 * named.
	 *
	 * @param timeout the timeout amount.
	 */
	@ParameterizedTest
	@ValueSource(longs = { 0, -1, Long.MAX_VALUE })
	void aTimeoutOutOfRangeIsRefused(long timeout) {
		TimeUnit unit = timeout == Long.MAX_VALUE ? TimeUnit.DAYS : TimeUnit.SECONDS;

		assertThatThrownBy(
				() -> StrictTimeoutsConfiguration.builder().theTimeoutIs(timeout).theTimeUnitIs(unit).build())
						.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theTimeoutIs");
	}

	/**
	 * A grace period below zero, above one day or beyond a duration is refused and
	 * named.
	 *
	 * @param grace the grace amount in days.
	 */
	@ParameterizedTest
	@ValueSource(longs = { -1, 2, Long.MAX_VALUE })
	void aGraceOutOfRangeIsRefused(long grace) {
		assertThatThrownBy(() -> StrictTimeoutsConfiguration.builder().theTimeoutIs(1).theTerminationGraceIs(grace)
				.theTerminationGraceUnitIs(TimeUnit.DAYS).build()).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("theTerminationGraceIs");
	}

	/** The refusal is localised: in German it still names the field. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> StrictTimeoutsConfiguration.builder().theTimeoutIs(0).build())
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("theTimeoutIs");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}
}
