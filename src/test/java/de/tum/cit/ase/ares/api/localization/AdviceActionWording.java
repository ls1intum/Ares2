package de.tum.cit.ase.ares.api.localization;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

/**
 * The expected English and German wording of every action and caller note the
 * advice code reports, shared by the tests of both enforcement back-ends so the
 * two cannot drift apart. It also offers a way to run a step in a given
 * language.
 */
public final class AdviceActionWording {

	/** Not meant to be instantiated. */
	private AdviceActionWording() {
	}

	/**
	 * Every action the advice code translates, with its English and German wording,
	 * plus one unknown action that must come back unchanged.
	 *
	 * @return rows of raw action, English wording and German wording
	 */
	public static Stream<Arguments> actions() {
		return Stream.of(Arguments.of("read", "read", "zu lesen"),
				Arguments.of("overwrite", "overwrite", "zu überschreiben"),
				Arguments.of("create", "create", "zu erstellen"), Arguments.of("delete", "delete", "zu löschen"),
				Arguments.of("execute", "execute", "auszuführen"),
				Arguments.of("connect", "connect", "eine Verbindung aufzubauen"),
				Arguments.of("send", "send", "Daten zu senden"),
				Arguments.of("receive", "receive", "Daten zu empfangen"),
				Arguments.of("manipulate", "manipulate", "zu manipulieren"),
				Arguments.of("teleport", "teleport", "teleport"));
	}

	/**
	 * The caller note for a known caller in both languages, and the empty note for
	 * an unknown one.
	 *
	 * @return rows of caller, English note and German note
	 */
	public static Stream<Arguments> callers() {
		return Stream.of(Arguments.of("a.B.m", " (called by a.B.m)", " (aufgerufen von a.B.m)"),
				Arguments.of(null, "", ""));
	}

	/**
	 * Runs a step with both the default and the display locale set, then restores
	 * them.
	 *
	 * @param locale the locale to run in
	 * @param step   the step to run
	 * @return what the step returned
	 */
	public static String inLocale(Locale locale, Supplier<String> step) {
		Locale originalDefault = Locale.getDefault();
		Locale originalDisplay = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(locale);
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			return step.get();
		} finally {
			Locale.setDefault(originalDefault);
			Locale.setDefault(Locale.Category.DISPLAY, originalDisplay);
		}
	}
}
