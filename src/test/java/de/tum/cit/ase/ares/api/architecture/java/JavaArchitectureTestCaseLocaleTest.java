package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks that an architecture violation is reported correctly whatever language
 * its rule was named in. A rule keeps the name it got when its class was
 * loaded, so a German name can reach a report written in English and the other
 * way round. Before, only English names were recognised: a German name put a
 * German verb into the action slot and a German import rule was not recognised
 * as one.
 */
class JavaArchitectureTestCaseLocaleTest {

	/** The rules whose violations name a caller and a target. */
	private static final List<String> CALL_RULES = List.of("file.system.access", "network.access", "terminate.jvm",
			"reflection.uses", "execute.command", "manipulate.threads", "serialize", "class.loading",
			"native.code.access", "agent.attach", "environment.access", "module.system.access", "jndi.injection");

	/** The two languages a rule can be named in and reported in. */
	private static final List<Locale> LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);

	/**
	 * Every call rule, named in each language and reported in each language.
	 *
	 * @return rows of rule, naming language and reporting language
	 */
	static Stream<Arguments> callRules() {
		return CALL_RULES.stream().flatMap(rule -> LOCALES.stream()
				.flatMap(naming -> LOCALES.stream().map(reporting -> Arguments.of(rule, naming, reporting))));
	}

	/**
	 * The forbidden-import rule, named in each language and reported in each.
	 *
	 * @return rows of naming language and reporting language
	 */
	static Stream<Arguments> importRule() {
		return LOCALES.stream().flatMap(naming -> LOCALES.stream().map(reporting -> Arguments.of(naming, reporting)));
	}

	/**
	 * Reads a rule's name as it reads in a given language.
	 *
	 * @param rule   the rule's key part after {@code security.architecture.}
	 * @param locale the language
	 * @return the rule's name in that language
	 */
	private static String ruleName(String rule, Locale locale) {
		return AdviceActionWording.inLocale(locale, () -> Messages.localized("security.architecture." + rule));
	}

	/**
	 * A call rule maps to its action worded in the reporting language, whatever
	 * language its name came in.
	 *
	 * @param rule      the rule's key part after {@code security.architecture.}
	 * @param naming    the language the rule was named in
	 * @param reporting the language the violation is reported in
	 */
	@ParameterizedTest
	@MethodSource("callRules")
	void callRuleIsReportedWithTheActionInTheReportingLanguage(String rule, Locale naming, Locale reporting) {
		AssertionError violation = new AssertionError("Architecture Violation [Priority: MEDIUM] - Rule '"
				+ ruleName(rule, naming) + "' was violated (1 times):\n"
				+ "Method <com.example.A.m()> calls method <java.io.File.delete()> in (A.java:1)");
		String reported = AdviceActionWording.inLocale(reporting,
				() -> assertThrows(SecurityException.class, () -> JavaArchitectureTestCase.parseErrorMessage(violation))
						.getMessage());
		String expected = AdviceActionWording.inLocale(reporting,
				() -> Messages.localized("security.archunit.violation.error", "com.example.A.m()",
						Messages.localized("security.archunit.action." + rule), "java.io.File.delete()",
						"com.example.A"));
		assertEquals(expected, reported);
	}

	/**
	 * The forbidden-import rule is recognised in either language and reports the
	 * forbidden package.
	 *
	 * @param naming    the language the rule was named in
	 * @param reporting the language the violation is reported in
	 */
	@ParameterizedTest
	@MethodSource("importRule")
	void importRuleIsRecognisedInEitherLanguage(Locale naming, Locale reporting) {
		AssertionError violation = new AssertionError("Architecture Violation [Priority: MEDIUM] - Rule '"
				+ ruleName("package.import", naming) + "' was violated (1 times):\n"
				+ "Class <com.example.A> depends on <java.io.File> in (A.java:0)");
		String reported = AdviceActionWording.inLocale(reporting,
				() -> assertThrows(SecurityException.class, () -> JavaArchitectureTestCase.parseErrorMessage(violation))
						.getMessage());
		String expected = AdviceActionWording.inLocale(reporting,
				() -> Messages.localized("security.archunit.package.import.violation", "java.io"));
		assertEquals(expected, reported);
	}
}
