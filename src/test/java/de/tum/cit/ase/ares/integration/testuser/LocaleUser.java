package de.tum.cit.ase.ares.integration.testuser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.*;

@Public
@UseLocale("de")
// Scope the default-policy analysis to a benign student-like subtree (no reserved
// packages) instead of scanning all of Ares's own build, which the ReservedPackageGuard
// correctly rejects. Default policy (value left blank) and runtime enforcement are
// unchanged; only the analysed classpath is narrowed.
@Policy(withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
public class LocaleUser {

	private static final String ACTIVE_LOCALIZATION = "active_localization";

	@Public
	@UseLocale("en")
	@Policy(withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
	public static class LocaleEn {
		@Test
		void testLocaleEn() {
			assertThat(Locale.getDefault()).isEqualTo(Locale.ENGLISH);
			assertThat(Messages.localized(ACTIVE_LOCALIZATION)).isEqualTo("en_US");
		}
	}

	@Public
	@UseLocale("fr")
	@Policy(withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
	public static class LocaleUnsupported {
		@Test
		void testLocaleUnsupported() {
			assertThat(Locale.getDefault()).isEqualTo(Locale.FRENCH);
			assertThat(Messages.localized(ACTIVE_LOCALIZATION)).isEqualTo("en_US");
		}
	}

	@Test
	void testLocaleDe() {
		assertThat(Locale.getDefault()).isEqualTo(Locale.GERMAN);
		assertThat(Messages.localized(ACTIVE_LOCALIZATION)).isEqualTo("de_DE");
	}

	@Test
	void testUnknownFormatted() {
		assertThat(Messages.localized("_unknown_%s_", "x")).isEqualTo("!_unknown_%s_!");
	}

	@Test
	void testUnknownNormal() {
		assertThat(Messages.localized("_unknown_")).isEqualTo("!_unknown_!");
	}
}
