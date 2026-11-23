package de.tum.cit.ase.ares.api.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Renders every violation template in English and German and compares whole
 * sentences. They pin the one denial sentence, which ends with the reason after
 * "but was blocked by Ares", and that the translated action lands in the right
 * slot.
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
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.file.execution", "read", "/p", EN
						+ "S tried to illegally read the file /p via CALL, but was blocked by Ares, because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.file.execution", "read", "/p", DE
						+ "S hat versucht, die Datei /p über CALL illegal zu lesen, wurde jedoch von Ares blockiert, "
						+ "weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.command.execution", "execute", "\"ls\"",
						EN + "S tried to illegally execute the command \"ls\" via CALL, but was blocked by Ares, "
								+ "because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.command.execution", "execute", "\"ls\"",
						DE + "S hat versucht, den Befehl \"ls\" über CALL illegal auszuführen, wurde jedoch von Ares "
								+ "blockiert, weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.thread.execution", "create", "T", EN
						+ "S tried to illegally create the thread T via CALL, but was blocked by Ares, because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.thread.execution", "create", "T",
						DE + "S hat versucht, den Thread T über CALL illegal zu erstellen, wurde jedoch von Ares "
								+ "blockiert, weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.method.execution", "manipulate", "T",
						EN + "S tried to illegally manipulate the thread T via CALL, but was blocked by Ares, "
								+ "because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.method.execution", "manipulate", "T",
						DE + "S hat versucht, den Thread T über CALL illegal zu manipulieren, wurde jedoch von Ares "
								+ "blockiert, weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.network.execution", "connect", "host:80",
						EN + "S tried to illegally connect to the network address host:80 via CALL, but was blocked by "
								+ "Ares, because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.network.execution", "connect", "host:80",
						DE + "S hat versucht, über CALL illegal eine Verbindung aufzubauen (Netzwerkadresse: host:80), "
								+ "wurde jedoch von Ares blockiert, weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.network.execution", "send", "host:80",
						EN + "S tried to illegally send data to the network address host:80 via CALL, but was blocked "
								+ "by Ares, because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.network.execution", "send", "host:80",
						DE + "S hat versucht, über CALL illegal Daten zu senden (Netzwerkadresse: host:80), wurde "
								+ "jedoch von Ares blockiert, weil REASON."),
				Arguments.of(Locale.ENGLISH, "security.advice.illegal.network.execution", "receive", "host:80",
						EN + "S tried to illegally receive data from the network address host:80 via CALL, but was "
								+ "blocked by Ares, because REASON."),
				Arguments.of(Locale.GERMAN, "security.advice.illegal.network.execution", "receive", "host:80",
						DE + "S hat versucht, über CALL illegal Daten zu empfangen (Netzwerkadresse: host:80), wurde "
								+ "jedoch von Ares blockiert, weil REASON."));
	}

	/**
	 * The thread template with a lambda task, in both languages.
	 *
	 * @return rows of language and expected sentence
	 */
	static Stream<Arguments> lambdaThreadViolations() {
		return Stream.of(
				Arguments.of(Locale.ENGLISH,
						EN + "S tried to illegally create the thread for a lambda requested by M via CALL, but was "
								+ "blocked by Ares, because REASON."),
				Arguments.of(Locale.GERMAN, DE + "S hat versucht, den Thread für ein Lambda, angefordert von M, über "
						+ "CALL illegal zu erstellen, wurde jedoch von Ares blockiert, weil REASON."));
	}

	/**
	 * A thread that runs a lambda is named after the method that asked for it, and
	 * the sentence stays grammatical in both languages.
	 *
	 * @param locale   the language to render in
	 * @param expected the complete expected sentence
	 */
	@ParameterizedTest
	@MethodSource("lambdaThreadViolations")
	void lambdaThreadViolationRendersACompleteSentence(Locale locale, String expected) {
		String rendered = AdviceActionWording.inLocale(locale,
				() -> Messages.localized("security.advice.illegal.thread.execution", "S",
						Messages.localized("security.advice.action.create"),
						Messages.localized("security.advice.thread.lambda", "M"), "CALL", "REASON"));
		assertEquals(expected, rendered);
	}

	/**
	 * The reasons for an action without any rule, in both languages.
	 *
	 * @return rows of language, reason key, raw action and expected reason
	 */
	static Stream<Arguments> missingActionRuleReasons() {
		return Stream.of(
				Arguments.of(Locale.ENGLISH, "security.advice.denial.reason.no.action.rule.file", "overwrite",
						"the policy has no rule that allows the code to overwrite any file"),
				Arguments.of(Locale.GERMAN, "security.advice.denial.reason.no.action.rule.file", "overwrite",
						"die Richtlinie keine Regel enthält, die es erlaubt, Dateien zu überschreiben"),
				Arguments.of(Locale.ENGLISH, "security.advice.denial.reason.no.action.rule.network", "receive",
						"the policy has no rule that allows the code to receive data from any network address"),
				Arguments.of(Locale.GERMAN, "security.advice.denial.reason.no.action.rule.network", "receive",
						"die Richtlinie keine Regel enthält, die es erlaubt, über das Netzwerk Daten zu empfangen"));
	}

	/**
	 * The reason for a missing action rule reads as a grammatical clause with the
	 * translated action in it.
	 *
	 * @param locale   the language to render in
	 * @param key      the reason's key
	 * @param action   the raw action name
	 * @param expected the expected reason clause
	 */
	@ParameterizedTest
	@MethodSource("missingActionRuleReasons")
	void missingActionRuleReasonReadsAsAClause(Locale locale, String key, String action, String expected) {
		String rendered = AdviceActionWording.inLocale(locale,
				() -> Messages.localized(key, Messages.localized("security.advice.action." + action)));
		assertEquals(expected, rendered);
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
				Messages.localized("security.advice.action." + action), resource, "CALL", "REASON"));
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
						EN + "S tried to illegally access the file system via TARGET (called by P), but was blocked by "
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
