package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks the policy's hidden-test category: its required deadline, the time
 * zone it insists on, and that it refuses values and entries it could not
 * apply.
 */
class HiddenTestsConfigurationTest {

	/** A valid deadline. */
	private static final String DEADLINE = "2026-12-24 23:59 Europe/Berlin";

	/** A deadline alone gives the deadline, no extension, no date and no list. */
	@Test
	void aDeadlineAloneIsEnough() {
		HiddenTestsConfiguration configuration = HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE).build();

		assertThat(configuration.effectiveDeadline())
				.isEqualTo(ZonedDateTime.parse("2026-12-24T23:59+01:00[Europe/Berlin]"));
		assertThat(configuration.extension()).isEmpty();
		assertThat(configuration.alwaysRunBefore()).isEmpty();
		assertThat(configuration.theFollowingTestsAreHidden()).isEmpty();
	}

	/** The extension is added to the deadline. */
	@Test
	void theExtensionIsAddedToTheDeadline() {
		HiddenTestsConfiguration configuration = HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE)
				.theDeadlineIsExtendedBy("1d 12h").build();

		assertThat(configuration.extension()).contains(Duration.ofHours(36));
		assertThat(configuration.effectiveDeadline())
				.isEqualTo(ZonedDateTime.parse("2026-12-26T11:59+01:00[Europe/Berlin]"));
	}

	/** A missing or blank deadline is refused. */
	@Test
	void aMissingDeadlineIsRefused() {
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theDeadlineIs");
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(" ").build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theDeadlineIs");
	}

	/**
	 * A date without a time zone is refused, naming the field.
	 *
	 * @param value a date without a zone.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "2026-12-24 23:59", "2026-12-24T23:59", "2026-12-24 23:59 nowhere/at-all" })
	void aDateWithoutAZoneIsRefused(String value) {
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(value).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theDeadlineIs");
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE)
				.hiddenTestsAlwaysRunBefore(value).build()).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("hiddenTestsAlwaysRunBefore");
	}

	/**
	 * The refusal is localised: in German it is the German text, naming the field.
	 */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs("2026-12-24 23:59").build())
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("Zeitzone")
					.hasMessageContaining("theDeadlineIs");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * A malformed value is refused, naming its field.
	 *
	 * @param field the field.
	 * @param value the malformed value.
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = { "theDeadlineIs|2026-13-45 23:59 UTC", "theDeadlineIs|tomorrow UTC",
			"theDeadlineIsExtendedBy|soon", "theDeadlineIsExtendedBy|0d", "hiddenTestsAlwaysRunBefore|yesterday UTC" })
	void aMalformedValueIsRefused(String field, String value) {
		HiddenTestsConfiguration.Builder builder = HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE);
		switch (field) {
		case "theDeadlineIs" -> builder.theDeadlineIs(value);
		case "theDeadlineIsExtendedBy" -> builder.theDeadlineIsExtendedBy(value);
		default -> builder.hiddenTestsAlwaysRunBefore(value);
		}

		assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(field);
	}

	/**
	 * A malformed list entry is refused, naming the entry.
	 *
	 * @param entry the malformed entry.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "org.example.PenguinTest#", "#name", "org example.PenguinTest", "org..PenguinTest",
			"org.example.PenguinTest#a#b" })
	void aMalformedEntryIsRefused(String entry) {
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE).hiddenTests(entry).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining(entry);
	}

	/**
	 * A class entry covers the class and its nested classes; a method entry its
	 * method.
	 */
	@Test
	void entriesCoverTheirClassesAndMethods() {
		HiddenTestsConfiguration configuration = HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE)
				.hiddenTests(Outer.class.getCanonicalName(), Other.class.getCanonicalName() + "#listed").build();

		assertThat(configuration.covers(Outer.class, Optional.empty())).isTrue();
		assertThat(configuration.covers(Outer.Inner.class, Optional.of("any"))).isTrue();
		assertThat(configuration.covers(Other.class, Optional.of("listed"))).isTrue();
		assertThat(configuration.covers(Other.class, Optional.of("unlisted"))).isFalse();
		assertThat(configuration.covers(Other.class, Optional.empty())).isFalse();
	}

	/** A listed fixture class with a nested class. */
	static class Outer {
		/** A nested class of the listed one. */
		static class Inner {
		}
	}

	/** A fixture class one of whose methods is listed. */
	static class Other {
	}
}
