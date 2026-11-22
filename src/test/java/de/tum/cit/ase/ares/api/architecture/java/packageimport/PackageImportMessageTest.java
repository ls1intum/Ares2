package de.tum.cit.ase.ares.api.architecture.java.packageimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesForbidden;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCaseCollection;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * Checks the refusal a forbidden import produces: it names the importing class
 * and every forbidden package it imports, agrees in number, falls back to "the
 * supervised code" when no class is named, and reads the same for ArchUnit and
 * WALA, in English and German.
 */
class PackageImportMessageTest {

	/** The fixture class that imports forbidden packages. */
	private static final String IMPORTING_CLASS = UsesForbidden.class.getName();

	/** The fixtures' own package, granted so their uses of one another pass. */
	private static final PackagePermission FIXTURE_PACKAGE = new PackagePermission(
			"de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures");

	/**
	 * The packages the fixture may import without being refused for them. The
	 * fixture is woven by AspectJ, so it also depends on the AspectJ packages the
	 * shipped configuration always grants.
	 */
	private static final Set<PackagePermission> BASELINE = Set.of(new PackagePermission("java.lang", true),
			new PackagePermission("java.io", true), FIXTURE_PACKAGE, new PackagePermission("org.aspectj"),
			new PackagePermission("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut"));

	/** The two languages a refusal is reported in. */
	private static final List<Locale> LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);

	/** The fixture, imported once. */
	private static JavaClasses fixture;

	/** Imports the fixture. */
	@BeforeAll
	static void importFixture() {
		fixture = new ClassFileImporter().importClasses(UsesForbidden.class);
	}

	/**
	 * Every engine in every language.
	 *
	 * @return rows of engine name and language
	 */
	static Stream<Arguments> enginesAndLocales() {
		return Stream.of("archunit", "wala")
				.flatMap(engine -> LOCALES.stream().map(locale -> Arguments.of(engine, locale)));
	}

	/**
	 * Every language.
	 *
	 * @return one row per language
	 */
	static Stream<Arguments> locales() {
		return LOCALES.stream().map(Arguments::of);
	}

	/**
	 * The import rule each engine runs, over the given permitted packages.
	 *
	 * @param engine  {@code archunit} or {@code wala}
	 * @param allowed the permitted packages
	 * @return the rule
	 */
	private static ArchRule ruleOf(String engine, Set<PackagePermission> allowed) {
		return "wala".equals(engine)
				? JavaWalaTestCaseCollection.noClassMustImportForbiddenPackages(allowed, Set.<ClassPermission>of())
				: JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowed, Set.<ClassPermission>of());
	}

	/**
	 * Runs a rule over the fixture and returns the refusal Ares reports for it.
	 *
	 * @param rule   the rule
	 * @param locale the language to report in
	 * @return the refusal's message
	 */
	private static String refusalOf(ArchRule rule, Locale locale) {
		AssertionError violation = assertThrows(AssertionError.class, () -> rule.check(fixture));
		return refusalOf(violation, locale);
	}

	/**
	 * Returns the refusal Ares reports for a rule violation.
	 *
	 * @param violation the rule violation
	 * @param locale    the language to report in
	 * @return the refusal's message
	 */
	private static String refusalOf(AssertionError violation, Locale locale) {
		return AdviceActionWording.inLocale(locale,
				() -> assertThrows(SecurityException.class, () -> JavaArchitectureTestCase.parseErrorMessage(violation))
						.getMessage());
	}

	/**
	 * Reads a catalogue text in a language.
	 *
	 * @param locale the language
	 * @param key    the key
	 * @param args   the arguments
	 * @return the text
	 */
	private static String localized(Locale locale, String key, Object... args) {
		return AdviceActionWording.inLocale(locale, () -> Messages.localized(key, args));
	}

	/**
	 * A class importing one forbidden package is named with that package, in the
	 * singular.
	 *
	 * @param engine the engine
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("enginesAndLocales")
	void oneForbiddenPackageIsNamedWithItsClass(String engine, Locale locale) {
		Set<PackagePermission> allowed = new java.util.HashSet<>(BASELINE);
		allowed.add(new PackagePermission("java.lang.reflect"));
		assertEquals(localized(locale, "security.archunit.package.import.violation", IMPORTING_CLASS, "java.net"),
				refusalOf(ruleOf(engine, allowed), locale));
	}

	/**
	 * A class importing two forbidden packages is named with both, in the plural.
	 *
	 * @param engine the engine
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("enginesAndLocales")
	void twoForbiddenPackagesAreNamedTogether(String engine, Locale locale) {
		assertEquals(localized(locale, "security.archunit.package.import.violation.plural", IMPORTING_CLASS,
				"java.lang.reflect, java.net"), refusalOf(ruleOf(engine, BASELINE), locale));
	}

	/**
	 * Both engines give the same text for the same violation.
	 *
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("locales")
	void archunitAndWalaGiveTheSameText(Locale locale) {
		assertEquals(refusalOf(ruleOf("archunit", BASELINE), locale), refusalOf(ruleOf("wala", BASELINE), locale));
	}

	/**
	 * A report whose line names no class gives the text about the supervised code.
	 *
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("locales")
	void reportWithoutAClassFallsBackToTheSupervisedCode(Locale locale) {
		AssertionError violation = new AssertionError(
				reportOf("Method <run()> calls method <java.net.Socket.close()>"));
		assertEquals(localized(locale, "security.archunit.package.import.violation.without.class", "java.net"),
				refusalOf(violation, locale));
	}

	/**
	 * Without a class, two forbidden packages are still named together, in the
	 * plural.
	 *
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("locales")
	void reportWithoutAClassNamesTwoPackagesTogether(Locale locale) {
		AssertionError violation = new AssertionError(reportOf("Method <run()> calls method <java.net.Socket.close()>",
				"Method <run()> gets field <java.nio.file.StandardOpenOption.READ>"));
		assertEquals(localized(locale, "security.archunit.package.import.violation.without.class.plural",
				"java.net, java.nio.file"), refusalOf(violation, locale));
	}

	/**
	 * A constructor line names its declaring class, and only packages the first
	 * reported class imports are named.
	 *
	 * @param locale the language
	 */
	@ParameterizedTest
	@MethodSource("locales")
	void onlyThePackagesOfTheFirstReportedClassAreNamed(Locale locale) {
		AssertionError violation = new AssertionError(reportOf(
				"Constructor <com.example.A.<init>()> calls constructor <java.net.Socket.<init>()> in (A.java:3)",
				"Class <com.example.B> depends on <java.util.ArrayList> in (B.java:0)"));
		assertEquals(localized(locale, "security.archunit.package.import.violation", "com.example.A", "java.net"),
				refusalOf(violation, locale));
	}

	/**
	 * Builds an import-rule report in the shape ArchUnit writes one.
	 *
	 * @param lines the violation lines
	 * @return the report
	 */
	private static String reportOf(String... lines) {
		return "Architecture Violation [Priority: MEDIUM] - Rule '"
				+ Messages.localized("security.architecture.package.import") + "' was violated (" + lines.length
				+ " times):\n" + String.join("\n", lines);
	}
}
