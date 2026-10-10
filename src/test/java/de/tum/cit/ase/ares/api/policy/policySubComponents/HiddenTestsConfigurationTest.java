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

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import de.tum.cit.ase.ares.api.context.TestType;

/**
 * Checks the policy's hidden-test category: its required deadline and
 * visibility switch, the time zone it insists on, which visibility it gives a
 * test, and that it refuses values and entries it could not apply.
 */
class HiddenTestsConfigurationTest {

	/** A valid deadline. */
	private static final String DEADLINE = "2026-12-24 23:59 Europe/Berlin";

	/**
	 * The deadline and the switch alone give the deadline, no extension, no date
	 * and no lists.
	 */
	@Test
	void theRequiredFieldsAloneAreEnough() {
		HiddenTestsConfiguration configuration = base().build();

		assertThat(configuration.effectiveDeadline())
				.isEqualTo(ZonedDateTime.parse("2026-12-24T23:59+01:00[Europe/Berlin]"));
		assertThat(configuration.extension()).isEmpty();
		assertThat(configuration.alwaysRunBefore()).isEmpty();
		assertThat(configuration.theFollowingTestsAreHidden()).isEmpty();
		assertThat(configuration.theFollowingTestsArePublic()).isEmpty();
	}

	/** A missing visibility switch is refused, naming it, in English and German. */
	@Test
	void aMissingUnlistedSwitchIsRefused() {
		assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageStartingWith("Ares Security Error")
				.hasMessageContaining("unlistedTestsAreHidden");
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE).build())
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("unlistedTestsAreHidden");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/** The extension is added to the deadline. */
	@Test
	void theExtensionIsAddedToTheDeadline() {
		HiddenTestsConfiguration configuration = base().theDeadlineIsExtendedBy("1d 12h").build();

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
		assertThatThrownBy(() -> base().hiddenTestsAlwaysRunBefore(value).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hiddenTestsAlwaysRunBefore");
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
		HiddenTestsConfiguration.Builder builder = base();
		switch (field) {
		case "theDeadlineIs" -> builder.theDeadlineIs(value);
		case "theDeadlineIsExtendedBy" -> builder.theDeadlineIsExtendedBy(value);
		default -> builder.hiddenTestsAlwaysRunBefore(value);
		}

		assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(field);
	}

	/**
	 * A malformed entry in either list is refused, naming the list and the entry.
	 *
	 * @param entry the malformed entry.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "org.example.PenguinTest#", "#name", "org example.PenguinTest", "org..PenguinTest",
			"org.example.PenguinTest#a#b" })
	void aMalformedEntryIsRefused(String entry) {
		assertThatThrownBy(() -> base().hiddenTests(entry).build()).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(entry).hasMessageContaining("theFollowingTestsAreHidden");
		assertThatThrownBy(() -> base().publicTests(entry).build()).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(entry).hasMessageContaining("theFollowingTestsArePublic");
	}

	/** An entry in both lists is refused, naming it, in English and German. */
	@Test
	void anEntryInBothListsIsRefused() {
		String entry = Other.class.getCanonicalName();
		assertThatThrownBy(() -> base().hiddenTests(entry).publicTests(entry).build())
				.isInstanceOf(IllegalArgumentException.class).hasMessageStartingWith("Ares Security Error")
				.hasMessageContaining(entry);
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> base().hiddenTests(entry).publicTests(entry).build())
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining(entry);
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * A class entry covers the class and its nested classes; a method entry its
	 * method; an unlisted test is untyped while unlisted tests are not hidden.
	 */
	@Test
	void entriesCoverTheirClassesAndMethods() {
		HiddenTestsConfiguration configuration = base()
				.hiddenTests(Outer.class.getCanonicalName(), Other.class.getCanonicalName() + "#listed").build();

		assertThat(configuration.visibilityOf(Outer.class, Optional.empty())).contains(TestType.HIDDEN);
		assertThat(configuration.visibilityOf(Outer.Inner.class, Optional.of("any"))).contains(TestType.HIDDEN);
		assertThat(configuration.visibilityOf(Other.class, Optional.of("listed"))).contains(TestType.HIDDEN);
		assertThat(configuration.visibilityOf(Other.class, Optional.of("unlisted"))).isEmpty();
		assertThat(configuration.visibilityOf(Other.class, Optional.empty())).isEmpty();
	}

	/** Under {@code unlistedTestsAreHidden: true} an unlisted test is hidden. */
	@Test
	void unlistedTestsAreHiddenWhenTheSwitchSaysSo() {
		HiddenTestsConfiguration configuration = base().unlistedTestsAreHidden(true).build();

		assertThat(configuration.visibilityOf(Other.class, Optional.of("any"))).contains(TestType.HIDDEN);
	}

	/**
	 * The nearer entry wins: a method entry over its class entry, in either
	 * direction.
	 */
	@Test
	void aMethodEntryWinsOverItsClassEntry() {
		String other = Other.class.getCanonicalName();
		HiddenTestsConfiguration publicMethod = base().hiddenTests(other).publicTests(other + "#shown").build();
		HiddenTestsConfiguration hiddenMethod = base().unlistedTestsAreHidden(true).publicTests(other)
				.hiddenTests(other + "#secret").build();

		assertThat(publicMethod.visibilityOf(Other.class, Optional.of("shown"))).contains(TestType.PUBLIC);
		assertThat(publicMethod.visibilityOf(Other.class, Optional.of("rest"))).contains(TestType.HIDDEN);
		assertThat(hiddenMethod.visibilityOf(Other.class, Optional.of("secret"))).contains(TestType.HIDDEN);
		assertThat(hiddenMethod.visibilityOf(Other.class, Optional.of("rest"))).contains(TestType.PUBLIC);
	}

	/** A nested class entry wins over its enclosing class entry. */
	@Test
	void aNestedClassEntryWinsOverItsEnclosingClass() {
		HiddenTestsConfiguration configuration = base().hiddenTests(Outer.class.getCanonicalName())
				.publicTests(Outer.Inner.class.getCanonicalName()).build();

		assertThat(configuration.visibilityOf(Outer.Inner.class, Optional.of("any"))).contains(TestType.PUBLIC);
		assertThat(configuration.visibilityOf(Outer.class, Optional.of("any"))).contains(TestType.HIDDEN);
	}

	/**
	 * A method entry of an enclosing class does not reach a nested class's method.
	 */
	@Test
	void anEnclosingClassMethodEntryDoesNotReachANestedClass() {
		HiddenTestsConfiguration configuration = base().hiddenTests(Outer.class.getCanonicalName() + "#same").build();

		assertThat(configuration.visibilityOf(Outer.Inner.class, Optional.of("same"))).isEmpty();
		assertThat(configuration.visibilityOf(Outer.class, Optional.of("same"))).contains(TestType.HIDDEN);
	}

	/**
	 * A class in the reserved generated package is public even when unlisted tests
	 * are hidden, so the generated sentinels run; a same-named class elsewhere is
	 * not.
	 */
	@Test
	void aGeneratedClassIsAlwaysPublic() {
		HiddenTestsConfiguration configuration = base().unlistedTestsAreHidden(true).build();

		assertThat(configuration.visibilityOf(classNamed("de.tum.cit.ase.ares.generated.SomeSentinelTest"),
				Optional.of("check"))).contains(TestType.PUBLIC);
		assertThat(configuration.visibilityOf(classNamed("org.example.SomeSentinelTest"), Optional.of("check")))
				.contains(TestType.HIDDEN);
	}

	/**
	 * A builder with the required deadline and unlisted tests not hidden.
	 *
	 * @return the builder
	 */
	private static HiddenTestsConfiguration.Builder base() {
		return HiddenTestsConfiguration.builder().theDeadlineIs(DEADLINE).unlistedTestsAreHidden(false);
	}

	/**
	 * An empty class of the given name, defined in its own class loader.
	 *
	 * @param name the fully qualified name.
	 * @return the class
	 */
	private static Class<?> classNamed(String name) {
		return new ByteBuddy().subclass(Object.class).name(name).make()
				.load(HiddenTestsConfigurationTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
				.getLoaded();
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
