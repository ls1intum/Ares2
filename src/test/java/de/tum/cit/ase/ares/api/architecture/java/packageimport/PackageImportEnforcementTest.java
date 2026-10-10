package de.tum.cit.ase.ares.api.architecture.java.packageimport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * Behavioural coverage for the PACKAGE_IMPORT rule.
 * <p>
 * The rule previously had no behavioural tests at all, which is why an
 * essential entry of the bare root {@code java} silently whitelisted the whole
 * JDK. These tests pin the two properties that failure depended on: the JDK
 * baseline is exact-matched, and policy grants stay subpackage-inclusive.
 *
 * @author Markus Paulsen
 */
class PackageImportEnforcementTest {

	private static final String FIXTURES = "de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures";

	/**
	 * The baseline the shipped configuration produces: java.lang, exact-matched.
	 */
	private static final PackagePermission JDK_BASELINE = new PackagePermission("java.lang", true);

	private static final PackagePermission FIXTURE_PACKAGE = new PackagePermission(FIXTURES);

	private static JavaClasses fixtures;

	@BeforeAll
	static void importFixtures() {
		fixtures = new ClassFileImporter().importPackages(FIXTURES);
	}

	private static String violationsFor(Set<PackagePermission> allowedPackages) {
		ArchRule rule = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages,
				Set.<ClassPermission>of());
		try {
			rule.check(fixtures);
			return "";
		} catch (AssertionError violation) {
			return violation.getMessage();
		}
	}

	// <editor-fold desc="Baseline blocks what a policy must be able to forbid">

	@Test
	@DisplayName("The default baseline forbids java.io and java.net")
	void baselineForbidsFileSystemAndNetworkPackages() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE));
		assertTrue(violations.contains("java.io"), "java.io must be reported as a forbidden import");
		assertTrue(violations.contains("java.net"), "java.net must be reported as a forbidden import");
	}

	@Test
	@DisplayName("An exact java.lang baseline does not admit java.lang.reflect")
	void exactBaselineDoesNotLeakIntoSubpackages() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE));
		assertTrue(violations.contains("java.lang.reflect"),
				"java.lang.reflect is a subpackage of the baseline and must stay forbidden");
	}

	@Test
	@DisplayName("Collections, lambdas and streams are not permitted by default")
	void baselineForbidsCollectionsAndLambdas() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE));
		assertTrue(violations.contains("java.util"),
				"java.util must require an explicit grant rather than being allowed by default");
	}

	@Test
	@DisplayName("Code using only java.lang passes under the default baseline")
	void baselinePermitsUnavoidableJavaLangOnlyCode() {
		JavaClasses onlyJavaLang = new ClassFileImporter()
				.importClasses(de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.OnlyJavaLang.class);
		ArchRule rule = JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(Set.of(JDK_BASELINE, FIXTURE_PACKAGE), Set.<ClassPermission>of());
		assertDoesNotThrow(() -> rule.check(onlyJavaLang),
				"java.lang is unavoidable, so plain code must not be flagged");
	}

	// </editor-fold>

	// <editor-fold desc="Defence in depth against a widened essential list">

	@Test
	@DisplayName("Re-adding the bare root 'java' to the essential list cannot re-open the JDK")
	void bareJavaRootGrantedExactlyDoesNotReopenTheJdk() {
		// JavaCreator pins every JDK-namespace essential entry to an exact match, so a
		// bare "java" matches only the literal package "java", which declares no
		// classes.
		String violations = violationsFor(Set.of(new PackagePermission("java", true), FIXTURE_PACKAGE));
		assertTrue(violations.contains("java.io"), "java.io must remain forbidden");
		assertTrue(violations.contains("java.net"), "java.net must remain forbidden");
		assertTrue(violations.contains("java.lang.reflect"), "java.lang.reflect must remain forbidden");
	}

	// </editor-fold>

	// <editor-fold desc="Policy grants keep subpackage-inclusive semantics">

	@Test
	@DisplayName("A policy grant of java.util also covers java.util.concurrent")
	void policyGrantsRemainSubpackageInclusive() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE, new PackagePermission("java.util"),
				new PackagePermission("java.util.stream")));
		assertFalse(violations.contains("java.util.concurrent"),
				"a subpackage-inclusive grant of java.util must cover java.util.concurrent");
	}

	@Test
	@DisplayName("An exact grant of java.util does not cover java.util.concurrent")
	void exactGrantsDoNotCoverSubpackages() {
		String violations = violationsFor(
				Set.of(JDK_BASELINE, FIXTURE_PACKAGE, new PackagePermission("java.util", true)));
		assertTrue(violations.contains("java.util.concurrent"),
				"an exact grant must not extend to java.util.concurrent");
	}

	@Test
	@DisplayName("A grant is boundary-aware and does not match a merely prefixed package")
	void grantsDoNotMatchPrefixedSiblingPackages() {
		// "…packageimport.fixture" (singular) must not cover "…packageimport.fixtures".
		String violations = violationsFor(Set.of(JDK_BASELINE,
				new PackagePermission("de.tum.cit.ase.ares.api.architecture.java.packageimport.fixture")));
		assertTrue(violations.contains(FIXTURES), "a prefixed but distinct package must not be covered");
	}

	// </editor-fold>

	// <editor-fold desc="Engine parity and permission model">

	@Test
	@DisplayName("WALA and ArchUnit reach the same verdict, since WALA delegates this category")
	void walaAndArchunitAgree() {
		Set<PackagePermission> allowed = Set.of(JDK_BASELINE, FIXTURE_PACKAGE);
		ArchRule archunitRule = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowed,
				Set.<ClassPermission>of());
		ArchRule walaRule = JavaWalaTestCaseCollection.noClassMustImportForbiddenPackages(allowed,
				Set.<ClassPermission>of());
		AssertionError fromArchunit = assertThrows(AssertionError.class, () -> archunitRule.check(fixtures));
		AssertionError fromWala = assertThrows(AssertionError.class, () -> walaRule.check(fixtures));
		assertEquals(fromArchunit.getMessage(), fromWala.getMessage(),
				"both engines must report identical package-import violations");
	}

	@Test
	@DisplayName("The single-argument constructor keeps the subpackage-inclusive default")
	void singleArgumentConstructorDefaultsToSubtree() {
		assertFalse(new PackagePermission("java.util").exactMatchOnly(),
				"policy-facing construction must stay subpackage-inclusive");
		assertTrue(new PackagePermission("java.lang", true).exactMatchOnly());
		assertTrue(PackagePermission.builder().importTheFollowingPackage("a.b").exactMatchOnly(true).build()
				.exactMatchOnly(), "the builder must carry the flag through");
	}

	@Test
	@DisplayName("Exactness participates in equality, so both forms can coexist in the allow-list")
	void exactnessParticipatesInEquality() {
		assertNotEquals(new PackagePermission("java.lang", true), new PackagePermission("java.lang", false));
	}

	@Test
	@DisplayName("Policy YAML written before the flag existed still deserialises as subpackage-inclusive")
	void legacyPolicyYamlRemainsSubpackageInclusive() throws Exception {
		String legacyYaml = "regardingPackageImports:\n" + "  - importTheFollowingPackage: \"java.util\"\n";
		LegacyPolicyFragment fragment = new ObjectMapper(new YAMLFactory()).readValue(legacyYaml,
				LegacyPolicyFragment.class);
		assertEquals(1, fragment.regardingPackageImports().size());
		assertFalse(fragment.regardingPackageImports().get(0).exactMatchOnly(),
				"an absent exactMatchOnly key must keep the original subpackage-inclusive meaning");
	}

	/** Minimal stand-in for the package-import section of a security policy. */
	record LegacyPolicyFragment(List<PackagePermission> regardingPackageImports) {
	}

	// </editor-fold>
}
