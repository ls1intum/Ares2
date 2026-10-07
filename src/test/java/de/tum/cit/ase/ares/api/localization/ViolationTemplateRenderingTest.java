package de.tum.cit.ase.ares.api.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Renders every violation template in English and German and compares whole
 * sentences. The German templates used to receive English action words and put
 * their verb after the denial reason; these sentences pin the corrected word
 * order and that the translated action lands in the right slot.
 */
class ViolationTemplateRenderingTest {

	/** The English prefix of a student violation. */
	private static final String EN = "Ares Security Error (Reason: Student-Code; Stage: Execution): ";

	/** The German prefix of a student violation. */
	private static final String DE = "Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausführung): ";

	/**
	 * One template per row, with its translated action, its resource and the
	 * expected sentence in each language.
	 *
	 * @return the rows
	 */
	static Stream<Arguments> violations() {
		return Stream.of(
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.file.execution", "read", "/p",
						EN + "S tried to illegally read File /p via CALL but was blocked by Ares."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.file.execution", "read", "/p", DE
						+ "S hat versucht, die Datei /p illegal zu lesen, wurde jedoch von Ares blockiert. Aufruf: CALL"),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.command.execution", "execute", "ls",
						EN + "S tried to illegally execute Command ls via CALL but was blocked by Ares."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.command.execution", "execute", "ls",
						DE + "S hat versucht, das Kommando ls illegal auszuführen, wurde jedoch von Ares blockiert. "
								+ "Aufruf: CALL"),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.thread.execution", "create", "T",
						EN + "S tried to illegally create Thread T via CALL but was blocked by Ares."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.thread.execution", "create", "T",
						DE + "S hat versucht, den Thread T illegal zu erstellen, wurde jedoch von Ares blockiert. "
								+ "Aufruf: CALL"),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.network.execution", "connect", "host:80",
						EN + "S tried to illegally connect Network host:80 via CALL but was blocked by Ares."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.network.execution", "connect", "host:80",
						DE + "S hat versucht, illegal eine Verbindung aufzubauen (Netzwerkziel host:80), wurde jedoch "
								+ "von Ares blockiert. Aufruf: CALL"));
	}

	/**
	 * Each advice template renders a complete sentence with the translated action
	 * in the right place.
	 *
	 * @param locale   the language to render in
	 * @param key      the template's key
	 * @param action   the raw action name
	 * @param resource the resource the student reached for
	 * @param expected the complete expected sentence
	 */
	@ParameterizedTest
	@MethodSource("violations")
	void adviceViolationRendersACompleteSentence(Locale locale, String key, String action, String resource,
			String expected) {
		String rendered = AdviceActionWording.inLocale(locale, () -> Messages.localized(key, "S",
				Messages.localized("security.advice.action." + action), resource, "CALL"));
		assertEquals(expected, rendered);
	}

	/**
	 * The architecture template renders a complete sentence in both languages.
	 *
	 * @return the rows
	 */
	static Stream<Arguments> architectureViolations() {
		return Stream.of(
				Arguments.of(Locale.ENGLISH,
						EN + "S tried to illegally access the file system via TARGET (called by P) but was blocked by "
								+ "Ares."),
				Arguments.of(Locale.GERMAN, DE + "S hat versucht, über TARGET (aufgerufen von P) illegal auf das "
						+ "Dateisystem zuzugreifen, wurde jedoch von Ares blockiert."));
	}

	/**
	 * The architecture violation sentence keeps its four arguments in place.
	 *
	 * @param locale   the language to render in
	 * @param expected the complete expected sentence
	 */
	@ParameterizedTest
	@MethodSource("architectureViolations")
	void architectureViolationRendersACompleteSentence(Locale locale, String expected) {
		String rendered = AdviceActionWording.inLocale(locale,
				() -> Messages.localized("security.archunit.violation.error", "S",
						Messages.localized("security.archunit.action.file.system.access"), "TARGET", "P"));
		assertEquals(expected, rendered);
	}
}
