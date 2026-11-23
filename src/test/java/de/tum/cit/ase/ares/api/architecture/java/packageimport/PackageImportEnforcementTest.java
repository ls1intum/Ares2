package de.tum.cit.ase.ares.api.architecture.java.packageimport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;

import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.OnlyJavaLang;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.PrintsAndUsesLambdas;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesCollections;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesConcurrent;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesForbidden;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.UsesJavaxAndThirdParty;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own.OwnSibling;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own.OwnSubUser;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own.OwnUser;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own.sub.SubHelper;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode.InstructorCheck;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode.UsesInstructorCheck;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode.UsesInstructorCheckEvil;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode.UsesInstructorCheckExtra;
import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode.UsesInstructorCheckHelper;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ExemptedClassNests;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

/**
 * Behavioural coverage for the PACKAGE_IMPORT rule.
 * <p>
 * The rule previously had no behavioural tests at all, which is why an
 * essential entry of the bare root {@code java} silently whitelisted the whole
 * JDK. These tests pin what the baseline permits, and that every grant covers
 * the packages below it.
 *
 * @author Markus Paulsen
 */
class PackageImportEnforcementTest {

	/** The package of the fixtures whose imports the rule checks. */
	private static final String FIXTURES = "de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures";

	/**
	 * java.lang, as the shipped configuration grants it.
	 */
	private static final PackagePermission JDK_BASELINE = new PackagePermission("java.lang");

	/**
	 * java.io, which the shipped configuration also grants because printing depends
	 * on java.io.PrintStream.
	 */
	private static final PackagePermission JAVA_IO_BASELINE = new PackagePermission("java.io");

	/**
	 * The infrastructure packages the shipped configuration always grants. The test
	 * fixtures are woven by AspectJ, so they depend on them.
	 */
	private static final Set<PackagePermission> INFRASTRUCTURE = Set.of(new PackagePermission("org.aspectj"),
			new PackagePermission("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut"));

	/** The package of the test-code fixtures. */
	private static final String TEST_CODE = FIXTURES + ".testcode";

	/** The fixture standing for a declared test class. */
	private static final String INSTRUCTOR_CHECK = TEST_CODE + ".InstructorCheck";

	/**
	 * The fixtures' own package, granted with its subpackages, so that the
	 * fixtures' uses of one another are not reported.
	 */
	private static final PackagePermission FIXTURE_PACKAGE = new PackagePermission(FIXTURES);

	/** The shared fixtures, imported once before all tests. */
	private static JavaClasses fixtures;

	/** Imports the shared fixtures. */
	@BeforeAll
	static void importFixtures() {
		fixtures = new ClassFileImporter().importClasses(UsesForbidden.class, UsesCollections.class,
				UsesConcurrent.class, OnlyJavaLang.class);
	}

	/**
	 * Returns the violations of the rule over the imported fixtures with only the
	 * given packages permitted, or an empty string.
	 */
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

	/** The shipped baseline forbids java.net but permits java.io. */
	@Test
	@DisplayName("The shipped baseline forbids java.net but permits java.io")
	void baselineForbidsNetworkButPermitsJavaIo() {
		String violations = violationsFor(Set.of(JDK_BASELINE, JAVA_IO_BASELINE, FIXTURE_PACKAGE));
		assertTrue(violations.contains("java.net"), "java.net must be reported as a forbidden import");
		assertFalse(violations.contains("java.io."), "java.io belongs to the shipped baseline");
	}

	/** The java.lang baseline covers java.lang.reflect for the import rule. */
	@Test
	@DisplayName("The java.lang baseline covers java.lang.reflect for the import rule")
	void baselineCoversSubpackagesOfJavaLang() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE));
		assertFalse(violations.contains("java.lang.reflect"),
				"java.lang.reflect lies below the baseline java.lang and must be covered");
	}

	/**
	 * The reflection rule still refuses a reflective call the import rule allows.
	 */
	@Test
	@DisplayName("The reflection rule still refuses a reflective call the import rule allows")
	void reflectionRuleStillRefusesReflection() {
		JavaClasses usesReflection = new ClassFileImporter().importClasses(UsesForbidden.class);
		assertThrows(AssertionError.class,
				() -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION.check(usesReflection),
				"covering java.lang.reflect for imports must not permit reflection");
	}

	/**
	 * Collections need a grant: the default baseline reports the fixture's use of
	 * java.util.ArrayList.
	 */
	@Test
	@DisplayName("Collections, lambdas and streams are not permitted by default")
	void baselineForbidsCollectionsAndLambdas() {
		String violations = violationsOf(Set.of(JDK_BASELINE, JAVA_IO_BASELINE), Set.of(), UsesCollections.class);
		assertTrue(violations.contains("java.util.ArrayList"),
				"java.util must require an explicit grant rather than being allowed by default");
	}

	/** Code using only java.lang passes under the default baseline. */
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

	/**
	 * A grant of the bare root {@code java} covers every {@code java.*} package, so
	 * the essential list must never contain it.
	 */
	@Test
	@DisplayName("A grant of the bare root 'java' covers every java.* package")
	void bareJavaRootCoversTheWholeJdk() {
		String violations = violationsFor(Set.of(new PackagePermission("java"), FIXTURE_PACKAGE));
		assertFalse(violations.contains("java.io"), "java.io lies below java");
		assertFalse(violations.contains("java.net"), "java.net lies below java");
		assertFalse(violations.contains("java.lang.reflect"), "java.lang.reflect lies below java");
	}

	// </editor-fold>

	// <editor-fold desc="Policy grants keep subpackage-inclusive semantics">

	/** A policy grant of java.util also covers java.util.concurrent. */
	@Test
	@DisplayName("A policy grant of java.util also covers java.util.concurrent")
	void policyGrantsRemainSubpackageInclusive() {
		String violations = violationsFor(Set.of(JDK_BASELINE, FIXTURE_PACKAGE, new PackagePermission("java.util"),
				new PackagePermission("java.util.stream")));
		assertFalse(violations.contains("java.util.concurrent"),
				"a subpackage-inclusive grant of java.util must cover java.util.concurrent");
	}

	/**
	 * A grant is boundary-aware: {@code ...packageimport.fixture}, in the singular,
	 * does not cover {@code ...packageimport.fixtures}.
	 */
	@Test
	@DisplayName("A grant is boundary-aware and does not match a merely prefixed package")
	void grantsDoNotMatchPrefixedSiblingPackages() {
		String violations = violationsFor(Set.of(JDK_BASELINE,
				new PackagePermission("de.tum.cit.ase.ares.api.architecture.java.packageimport.fixture")));
		assertTrue(violations.contains(FIXTURES), "a prefixed but distinct package must not be covered");
	}

	// </editor-fold>

	// <editor-fold desc="Engine parity and permission model">

	/**
	 * WALA and ArchUnit reach the same verdict, since WALA delegates this category.
	 */
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

	/** A package entry binds from policy YAML to the permission it names. */
	@Test
	@DisplayName("A package entry binds from policy YAML to the permission it names")
	void policyYamlBindsThePackage() throws Exception {
		String yaml = "regardingPackageImports:\n" + "  - importTheFollowingPackage: \"java.util\"\n";
		LegacyPolicyFragment fragment = new ObjectMapper(new YAMLFactory()).readValue(yaml, LegacyPolicyFragment.class);
		assertEquals(List.of(new PackagePermission("java.util")), fragment.regardingPackageImports());
	}

	/** A permission written out names only its package, and reads back equal. */
	@Test
	@DisplayName("A permission written out names only its package, and reads back equal")
	void permissionRoundTripsWithOnlyItsPackage() throws Exception {
		ObjectMapper json = new ObjectMapper();
		String written = json.writeValueAsString(new PackagePermission("java.util"));
		assertEquals("{\"importTheFollowingPackage\":\"java.util\"}", written);
		assertEquals(new PackagePermission("java.util"), json.readValue(written, PackagePermission.class));
		ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
		String writtenYaml = yaml.writeValueAsString(new PackagePermission("java.util"));
		assertEquals("---\nimportTheFollowingPackage: \"java.util\"\n", writtenYaml);
		assertEquals(new PackagePermission("java.util"), yaml.readValue(writtenYaml, PackagePermission.class));
	}

	/** Minimal stand-in for the package-import section of a security policy. */
	record LegacyPolicyFragment(List<PackagePermission> regardingPackageImports) {
	}

	// </editor-fold>
	// <editor-fold desc="Shipped baseline and the default without a policy">

	/**
	 * Returns the violations of the rule over the given classes, with the
	 * infrastructure and their own packages permitted, or an empty string.
	 */
	private static String violationsOf(Set<PackagePermission> allowedPackages, Set<String> testClassNames,
			Class<?>... classes) {
		JavaClasses checked = new ClassFileImporter().importClasses(classes);
		Set<PackagePermission> withInfrastructure = new HashSet<>(allowedPackages);
		withInfrastructure.addAll(INFRASTRUCTURE);
		ArchRule rule = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(
				JavaArchunitTestCaseCollection.withOwnPackages(withInfrastructure, checked, Set.of()), Set.of(),
				testClassNames);
		try {
			rule.check(checked);
			return "";
		} catch (AssertionError violation) {
			return violation.getMessage();
		}
	}

	/**
	 * Printing, lambdas, records, enums and string switches pass under the shipped
	 * baseline.
	 */
	@Test
	@DisplayName("Printing, lambdas, records, enums and string switches pass under the shipped baseline")
	void shippedBaselinePermitsOrdinaryCode() {
		assertEquals("", violationsOf(Set.of(JDK_BASELINE, JAVA_IO_BASELINE), Set.of(),
				PrintsAndUsesLambdas.class.getNestMembers()), "ordinary code must not need a policy grant");
	}

	/** Without a policy every java.* package may be imported. */
	@Test
	@DisplayName("Without a policy every java.* package may be imported")
	void noPolicyDefaultPermitsEveryJavaPackage() {
		Set<PackagePermission> allowed = new HashSet<>(
				ResourceAccesses.createRestrictiveWithoutPolicy().regardingPackageImports());
		allowed.add(JDK_BASELINE);
		allowed.add(JAVA_IO_BASELINE);
		assertEquals("",
				violationsOf(allowed, Set.of(), UsesForbidden.class, UsesCollections.class, UsesConcurrent.class),
				"java.io, java.net, java.util and java.lang.reflect must all be permitted");
	}

	/** Without a policy javax and third-party packages stay forbidden. */
	@Test
	@DisplayName("Without a policy javax and third-party packages stay forbidden")
	void noPolicyDefaultForbidsJavaxAndThirdParty() {
		Set<PackagePermission> allowed = new HashSet<>(
				ResourceAccesses.createRestrictiveWithoutPolicy().regardingPackageImports());
		allowed.add(JDK_BASELINE);
		String violations = violationsOf(allowed, Set.of(), UsesJavaxAndThirdParty.class);
		assertTrue(violations.contains("javax.swing"), "javax is not part of java.*");
		assertTrue(violations.contains("com.fasterxml.jackson"), "a third-party library is not part of java.*");
	}

	/** Without a policy everything but the package imports stays restrictive. */
	@Test
	@DisplayName("Without a policy everything but the package imports stays restrictive")
	void noPolicyDefaultOnlyWidensPackageImports() {
		ResourceAccesses withoutPolicy = ResourceAccesses.createRestrictiveWithoutPolicy();
		ResourceAccesses restrictive = ResourceAccesses.createRestrictive();
		assertEquals(List.of(new PackagePermission("java")), withoutPolicy.regardingPackageImports());
		assertEquals(restrictive.regardingTimeouts(), withoutPolicy.regardingTimeouts());
		assertTrue(withoutPolicy.regardingFileSystemInteractions().isEmpty());
		assertTrue(withoutPolicy.regardingNetworkConnections().isEmpty());
		assertTrue(withoutPolicy.regardingCommandExecutions().isEmpty());
		assertTrue(withoutPolicy.regardingThreadCreations().isEmpty());
	}

	// </editor-fold>

	// <editor-fold desc="The supervised code's own packages">

	/** The supervised code may use a class of its own package. */
	@Test
	@DisplayName("The supervised code may use a class of its own package")
	void ownPackageCoversSiblings() {
		assertEquals("", violationsOf(Set.of(JDK_BASELINE), Set.of(), OwnUser.class, OwnSibling.class));
	}

	/** An own package covers the packages below it. */
	@Test
	@DisplayName("An own package covers the packages below it")
	void ownPackageCoversItsSubpackages() {
		assertFalse(violationsOf(Set.of(JDK_BASELINE), Set.of(), OwnSubUser.class).contains(FIXTURES + ".own.sub"),
				"a class below the own package must be importable");
	}

	/** A named test class below an own package stays forbidden. */
	@Test
	@DisplayName("A named test class below an own package stays forbidden")
	void testClassBelowAnOwnPackageIsForbidden() {
		assertTrue(violationsOf(Set.of(JDK_BASELINE), Set.of(SubHelper.class.getName()), OwnSubUser.class)
				.contains(SubHelper.class.getName()), "the own package must not open a test class below it");
	}

	/** Classes in the JDK namespace never add an own package. */
	@Test
	@DisplayName("Classes in the JDK namespace never add an own package")
	void ownPackagesSkipTheJdkNamespace() {
		JavaClasses jdkClasses = new ClassFileImporter().importClasses(javax.swing.JLabel.class,
				java.util.ArrayList.class);
		assertTrue(JavaArchunitTestCaseCollection.withOwnPackages(Set.of(), jdkClasses, Set.of()).isEmpty());
	}

	/** An exempt class adds no own package. */
	@Test
	@DisplayName("An exempt class adds no own package")
	void exemptClassesAddNoOwnPackage() {
		JavaClasses checked = new ClassFileImporter().importClasses(OwnUser.class);
		assertTrue(JavaArchunitTestCaseCollection
				.withOwnPackages(Set.of(), checked, Set.of(new ClassPermission(OwnUser.class.getName()))).isEmpty());
	}

	/**
	 * A compiled class whose package is not a valid Java package, which bytecode
	 * allows and source does not, is refused instead of becoming an own package; a
	 * valid one still does.
	 */
	@Test
	@DisplayName("Refuses an own package that is not a valid Java package")
	void ownPackageMustBeAValidJavaPackage(@TempDir Path classFolder) throws Exception {
		byte[] bytecode = new ByteBuddy().with(TypeValidation.DISABLED).subclass(Object.class).name("com.class.Evil")
				.make().getBytes();
		Path classFile = Files.createDirectories(classFolder.resolve("com").resolve("class")).resolve("Evil.class");
		Files.write(classFile, bytecode);
		JavaClasses invalid = new ClassFileImporter().importPath(classFolder);
		assertEquals(Set.of("com.class"),
				invalid.stream().map(javaClass -> javaClass.getPackageName()).collect(Collectors.toSet()));
		assertThrows(SecurityException.class,
				() -> JavaArchunitTestCaseCollection.withOwnPackages(Set.of(), invalid, Set.of()));
		JavaClasses valid = new ClassFileImporter().importClasses(OwnUser.class);
		assertEquals(Set.of(new PackagePermission(FIXTURES + ".own")),
				JavaArchunitTestCaseCollection.withOwnPackages(Set.of(), valid, Set.of()));
	}

	// </editor-fold>

	// <editor-fold desc="Test code is never importable">

	/**
	 * Returns the violations for a class depending on test code, with the test code
	 * package granted by policy.
	 */
	private static String testCodeViolationsOf(Class<?> origin) {
		return violationsOf(Set.of(JDK_BASELINE, new PackagePermission(TEST_CODE)), Set.of(INSTRUCTOR_CHECK), origin);
	}

	/** A test class is forbidden even when the policy grants its package. */
	@Test
	@DisplayName("A test class is forbidden even when the policy grants its package")
	void testClassIsForbiddenDespiteAPackageGrant() {
		assertTrue(testCodeViolationsOf(UsesInstructorCheck.class).contains(INSTRUCTOR_CHECK));
	}

	/** A class nested in a test class is forbidden. */
	@Test
	@DisplayName("A class nested in a test class is forbidden")
	void nestedTestClassIsForbidden() {
		assertTrue(testCodeViolationsOf(UsesInstructorCheckHelper.class).contains(INSTRUCTOR_CHECK + "$Helper"));
	}

	/** A class merely sharing the test class's prefix is not test code. */
	@Test
	@DisplayName("A class merely sharing the test class's prefix is not test code")
	void prefixedSiblingIsNotTestCode() {
		assertEquals("", testCodeViolationsOf(UsesInstructorCheckExtra.class));
	}

	/** A top-level class named like a nested test class is treated as test code. */
	@Test
	@DisplayName("A top-level class named like a nested test class is treated as test code")
	void topLevelClassNamedLikeANestedTestClassIsForbidden() {
		assertTrue(testCodeViolationsOf(UsesInstructorCheckEvil.class).contains(INSTRUCTOR_CHECK + "$Evil"));
	}

	/** The exemption still requires real nesting, unlike the dependency rule. */
	@Test
	@DisplayName("The exemption still requires real nesting, unlike the dependency rule")
	void exemptionStillRequiresRealNesting() throws Exception {
		Path testOutputRoot = Path
				.of(InstructorCheck.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		List<String> exempted = ExemptedClassNests.expandFromTestOutput(List.of(INSTRUCTOR_CHECK), testOutputRoot);
		assertTrue(exempted.contains(INSTRUCTOR_CHECK + "$Helper"));
		assertFalse(exempted.contains(INSTRUCTOR_CHECK + "$Evil"));
	}

	/** WALA reaches the same verdict on test code. */
	@Test
	@DisplayName("WALA reaches the same verdict on test code")
	void walaAgreesOnTestCode() {
		JavaClasses checked = new ClassFileImporter().importClasses(UsesInstructorCheck.class);
		Set<PackagePermission> allowed = Set.of(JDK_BASELINE, new PackagePermission(TEST_CODE));
		AssertionError fromArchunit = assertThrows(AssertionError.class, () -> JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(allowed, Set.of(), Set.of(INSTRUCTOR_CHECK)).check(checked));
		AssertionError fromWala = assertThrows(AssertionError.class, () -> JavaWalaTestCaseCollection
				.noClassMustImportForbiddenPackages(allowed, Set.of(), Set.of(INSTRUCTOR_CHECK)).check(checked));
		assertEquals(fromArchunit.getMessage(), fromWala.getMessage());
	}

	// </editor-fold>

	// <editor-fold desc="Cached verdicts and generated rules">

	/**
	 * Builds an ArchUnit package-import test case over the given classes and test
	 * class names.
	 */
	private static JavaArchunitTestCase archunitCase(JavaClasses checked, Set<String> testClassNames) {
		return JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Set.of(JDK_BASELINE, new PackagePermission(TEST_CODE))).javaClasses(checked)
				.testClassNames(testClassNames).build();
	}

	/** The ArchUnit cache never serves a verdict across different test classes. */
	@Test
	@DisplayName("The ArchUnit cache never serves a verdict across different test classes")
	void archunitCacheSeparatesTestClassNames() {
		JavaClasses passFirst = new ClassFileImporter().importClasses(UsesInstructorCheck.class);
		assertDoesNotThrow(() -> archunitCase(passFirst, Set.of()).executeArchitectureTestCase("ARCHUNIT", ""));
		assertThrows(SecurityException.class,
				() -> archunitCase(passFirst, Set.of(INSTRUCTOR_CHECK)).executeArchitectureTestCase("ARCHUNIT", ""));
		JavaClasses failFirst = new ClassFileImporter().importClasses(UsesInstructorCheck.class);
		assertThrows(SecurityException.class,
				() -> archunitCase(failFirst, Set.of(INSTRUCTOR_CHECK)).executeArchitectureTestCase("ARCHUNIT", ""));
		assertDoesNotThrow(() -> archunitCase(failFirst, Set.of()).executeArchitectureTestCase("ARCHUNIT", ""));
	}

	/**
	 * Builds a WALA package-import test case that must never build a call graph.
	 */
	private static JavaWalaTestCase walaCase(JavaClasses checked, Set<String> testClassNames) {
		return new JavaWalaTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				Set.of(JDK_BASELINE, new PackagePermission(TEST_CODE)), checked, () -> {
					throw new AssertionError("PACKAGE_IMPORT must not build a call graph");
				}, Set.of(), testClassNames, null, false);
	}

	/** The WALA cache never serves a verdict across different test classes. */
	@Test
	@DisplayName("The WALA cache never serves a verdict across different test classes")
	void walaCacheSeparatesTestClassNames() {
		JavaClasses checked = new ClassFileImporter().importClasses(UsesInstructorCheck.class);
		assertDoesNotThrow(() -> walaCase(checked, Set.of()).executeArchitectureTestCase("WALA", ""));
		assertThrows(SecurityException.class,
				() -> walaCase(checked, Set.of(INSTRUCTOR_CHECK)).executeArchitectureTestCase("WALA", ""));
		assertDoesNotThrow(() -> walaCase(checked, Set.of()).executeArchitectureTestCase("WALA", ""));
	}

	/**
	 * Generated rules of both engines name the test classes and read the own
	 * packages at runtime.
	 */
	@Test
	@DisplayName("Generated rules of both engines name the test classes and read the own packages at runtime")
	void generatedRulesCarryTheTestClassesThroughTheConversion() {
		JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Set.of(JDK_BASELINE, new PackagePermission("java.util")))
				.javaClasses(new ClassFileImporter().importPackages()).testClassNames(Set.of(INSTRUCTOR_CHECK))
				.supervisedPackage("de.tum.cit.aet").callGraphSupplier(() -> {
					throw new AssertionError("writing must not build a call graph");
				}).build();
		for (ArchitectureMode mode : ArchitectureMode.values()) {
			String written = mode.threePartedFileBody(List.of(testCase));
			assertTrue(written.contains("Set.of(\"" + INSTRUCTOR_CHECK + "\")"), mode + " must name the test class");
			assertTrue(written.contains("new PackagePermission(\"java.util\")"), mode + " must write the grant");
			assertTrue(written.contains("JavaArchunitTestCaseCollection.withOwnPackages(allowedPackages, javaClasses"),
					mode + " must read the own packages from the checked classes");
		}
	}

	// </editor-fold>
}
