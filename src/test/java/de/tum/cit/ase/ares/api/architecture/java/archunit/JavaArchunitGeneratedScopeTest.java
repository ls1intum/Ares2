package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * What a generated architecture test is told to do at its own runtime.
 * <p>
 * The generated file is the enforcement in the precompile flow, so what these
 * assert is not formatting but whether that file enforces anything. Two
 * regressions are pinned here, both of which produced a green suite that
 * checked nothing.
 */
class JavaArchunitGeneratedScopeTest {

	private JavaArchunitTestCase generated(boolean derived) {
		return JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Set.of(new PackagePermission("de.tum.cit.aet")))
				// Empty, exactly as it is during precompile: nothing has been compiled yet.
				.javaClasses(new ClassFileImporter().importPackages()).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(derived).build();
	}

	@Test
	@DisplayName("Asks for the classes at runtime rather than importing nothing")
	void asksForTheClassesAtRuntime() {
		String written = generated(true).writeArchitectureTestCase("ARCHUNIT", "");

		// The regression: with no classes compiled the emission fell back to
		// importPackages() with no arguments, so every rule checked an empty set and
		// passed. archunit.properties sets archRule.failOnEmptyShould=false, so
		// nothing objected.
		assertFalse(written.contains("importPackages()"),
				"a generated rule that imports no packages checks nothing and passes: " + written);
		assertTrue(written.contains("JavaArchunitSupervisedClasses.validated(\"de.tum.cit.aet\")"),
				"a derived scope must be checked against the compiled output at runtime: " + written);
	}

	@Test
	@DisplayName("Reads the own-package permissions from the checked classes when the scope was derived")
	void readsOwnPackagesFromTheCheckedClassesWhenDerived() {
		String written = generated(true).writeArchitectureTestCase("ARCHUNIT", "");

		assertTrue(written.contains("JavaArchunitTestCaseCollection.withOwnPackages(allowedPackages, javaClasses"),
				"a derived scope must read the packages the checked classes declare: " + written);
		assertFalse(written.contains("JavaArchunitSupervisedClasses.allowedPackages("),
				"what was compiled while generating must not become a permission: " + written);
	}

	@Test
	@DisplayName("Writes a pinned scope's permissions as declared and reads its own packages at runtime")
	void writesAPinnedScopeAsDeclared() {
		String written = generated(false).writeArchitectureTestCase("ARCHUNIT", "");

		assertTrue(written.contains("JavaArchunitSupervisedClasses.pinned(\"de.tum.cit.aet\")"), written);
		assertTrue(written.contains("new PackagePermission(\"de.tum.cit.aet\", false)"),
				"a pinned scope's permissions are a declaration and are written as such: " + written);
		assertTrue(written.contains("JavaArchunitTestCaseCollection.withOwnPackages(allowedPackages, javaClasses"),
				"the supervised code's own packages are read from the checked classes: " + written);
	}
}
