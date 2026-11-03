package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

public class JavaArchunitTestCaseTest {

	private JavaArchunitTestCase.Builder builder;

	@BeforeEach
	void setUp() {
		builder = JavaArchunitTestCase.archunitBuilder();
	}

	@Test
	void builder_missingParameters_throwsNullPointerException() {
		// Missing all parameters
		assertThrows(NullPointerException.class, () -> builder.build());

		// Only supported set
		builder.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION);
		assertThrows(NullPointerException.class, () -> builder.build());

		// Supported and allowedPackages
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		builder.allowedPackages(allowed);
		assertThrows(NullPointerException.class, () -> builder.build());
	}

	@Test
	void builder_allParameters_buildsSuccessfully() {
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");

		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		assertNotNull(testCase);
	}

	@Test
	void allowedPackagesAsCode_emptySet_returnsSetOf() throws Exception {
		Set<PackagePermission> allowed = Collections.emptySet();
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
		method.setAccessible(true);
		String result = (String) method.invoke(testCase);
		assertEquals("Set.of()", result);
	}

	@Test
	void allowedPackagesAsCode_nonEmptySet_returnsCorrectLiteral() throws Exception {
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.test.pkg"));
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
		method.setAccessible(true);
		String result = (String) method.invoke(testCase);
		assertTrue(result.startsWith("Set.of("));
		assertTrue(result.contains("PackagePermission"));
	}

	/**
	 * The emitted code used to carry the packages seen when the file was written.
	 * That is exactly what made a precompiled suite vacuous: during generation
	 * nothing is compiled, so the set was empty and the emission was
	 * importPackages() with no arguments, which every rule then checked and passed.
	 * What is emitted now is a question asked at the generated test's own runtime.
	 */
	@Test
	void javaClassesAsCode_derivedScope_asksForTheValidatedInventoryAtRuntime() throws Exception {
		JavaClasses emptyClasses = new ClassFileImporter().importPackages("non.existent.package");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(emptyClasses).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(true).build();

		String result = javaClassesAsCode(testCase);

		assertEquals("JavaArchunitSupervisedClasses.validated(\"de.tum.cit.aet\")", result,
				"a derived scope has to be checked against the whole compiled output before it is used");
		assertFalse(result.contains("importPackages"),
				"nothing may be baked in at generation time, since that is what an empty precompile emitted");
	}

	/**
	 * A pinned scope is the instructor's statement rather than a reading of the
	 * project, so it imports its own subtree and is not held to whole-project
	 * coverage: a policy may deliberately supervise part of an output.
	 */
	@Test
	void javaClassesAsCode_pinnedScope_asksOnlyForItsOwnSubtree() throws Exception {
		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(false).build();

		String result = javaClassesAsCode(testCase);

		assertEquals("JavaArchunitSupervisedClasses.pinned(\"de.tum.cit.aet\")", result);
	}

	/**
	 * Generating a file with no scope would generate one that analyses nothing, so
	 * it is refused where it is noticed rather than written out and discovered
	 * green.
	 */
	@Test
	void javaClassesAsCode_withoutAScope_refusesToGenerate() throws Exception {
		JavaClasses javaClasses = new ClassFileImporter().importPackages("non.existent.package");
		Set<PackagePermission> allowed = new HashSet<>();
		allowed.add(new PackagePermission("com.example"));
		JavaArchunitTestCase testCase = builder
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
				.allowedPackages(allowed).javaClasses(javaClasses).build();

		Method method = JavaArchunitTestCase.class.getDeclaredMethod("javaClassesAsCode");
		method.setAccessible(true);
		InvocationTargetException failure = assertThrows(InvocationTargetException.class,
				() -> method.invoke(testCase));

		assertInstanceOf(SecurityException.class, failure.getCause());
	}

	private static String javaClassesAsCode(JavaArchunitTestCase testCase) throws Exception {
		Method method = JavaArchunitTestCase.class.getDeclaredMethod("javaClassesAsCode");
		method.setAccessible(true);
		return (String) method.invoke(testCase);
	}

	@Test
	void archunitBuilder_returnsBuilderInstance() {
		assertNotNull(JavaArchunitTestCase.archunitBuilder());
	}

	/** A class with no dependencies beyond its own nest and java.lang. */
	static final class Tiny {
	}

	/** The package of {@link Tiny}, which it depends on through its nest host. */
	private static final String OWN_PACKAGE = "de.tum.cit.ase.ares.api.architecture.java.archunit";

	/**
	 * Runs the package-import rule over the given classes with the given
	 * permissions.
	 *
	 * @param javaClasses the analysed classes, shared between runs
	 * @param packages    the permitted package names
	 * @param classes     the exempted class names
	 */
	private static void runPackageImport(JavaClasses javaClasses, Set<String> packages, Set<String> classes) {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		packages.forEach(name -> allowedPackages.add(new PackagePermission(name)));
		Set<ClassPermission> allowedClasses = new HashSet<>();
		classes.forEach(name -> allowedClasses.add(new ClassPermission(name)));
		JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(allowedPackages).allowedClasses(allowedClasses).javaClasses(javaClasses).build()
				.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ");
	}

	/**
	 * A single permitted package named "a,b" is not served the cached pass of the
	 * two packages a and b.
	 */
	@Test
	void packageImport_commaJoinedPackageAfterTheSplitOnes_isNotServedTheirCachedPass() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		runPackageImport(javaClasses, Set.of("java", OWN_PACKAGE), Set.of());
		assertThrows(SecurityException.class,
				() -> runPackageImport(javaClasses, Set.of(OWN_PACKAGE + ",java"), Set.of()));
	}

	/**
	 * The two packages a and b are not served the cached failure of the single
	 * package named "a,b".
	 */
	@Test
	void packageImport_splitPackagesAfterTheCommaJoinedOne_areNotServedItsCachedFailure() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		assertThrows(SecurityException.class,
				() -> runPackageImport(javaClasses, Set.of(OWN_PACKAGE + ",java"), Set.of()));
		assertDoesNotThrow(() -> runPackageImport(javaClasses, Set.of("java", OWN_PACKAGE), Set.of()));
	}

	/**
	 * A single exempted class named "a,b" is not served the cached pass of the two
	 * exempted classes a and b.
	 */
	@Test
	void packageImport_commaJoinedExemptionAfterTheSplitOnes_isNotServedTheirCachedPass() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		String tiny = Tiny.class.getName();
		runPackageImport(javaClasses, Set.of(), Set.of(tiny, "x.Other"));
		assertThrows(SecurityException.class, () -> runPackageImport(javaClasses, Set.of(), Set.of(tiny + ",x.Other")));
	}

	/**
	 * The two exempted classes a and b are not served the cached failure of the
	 * single exempted class named "a,b".
	 */
	@Test
	void packageImport_splitExemptionsAfterTheCommaJoinedOne_areNotServedItsCachedFailure() {
		JavaClasses javaClasses = new ClassFileImporter().importClasses(Tiny.class);
		String tiny = Tiny.class.getName();
		assertThrows(SecurityException.class, () -> runPackageImport(javaClasses, Set.of(), Set.of(tiny + ",x.Other")));
		assertDoesNotThrow(() -> runPackageImport(javaClasses, Set.of(), Set.of(tiny, "x.Other")));
	}
}
