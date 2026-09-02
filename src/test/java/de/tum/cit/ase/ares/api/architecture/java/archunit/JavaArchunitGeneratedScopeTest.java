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
	@DisplayName("Reads the self-import permissions from the output when the scope was derived")
	void readsSelfImportPermissionsFromTheOutputWhenDerived() {
		String written = generated(true).writeArchitectureTestCase("ARCHUNIT", "");

		// A permission is matched as a prefix, so writing the derived scope itself into
		// the generated file left it holding a grant over that whole namespace, which
		// outlives the coverage check that ran beside it.
		assertTrue(written.contains("JavaArchunitSupervisedClasses.allowedPackages(\"de.tum.cit.aet\""),
				"a derived scope must ask for the packages the output declares: " + written);
	}

	@Test
	@DisplayName("Leaves a pinned scope stated as the instructor wrote it")
	void leavesAPinnedScopeAlone() {
		String written = generated(false).writeArchitectureTestCase("ARCHUNIT", "");

		// A pinned policy may deliberately supervise part of the output and declare
		// exactly what may be imported, so neither is second-guessed at runtime.
		assertTrue(written.contains("JavaArchunitSupervisedClasses.pinned(\"de.tum.cit.aet\")"), written);
		assertFalse(written.contains("JavaArchunitSupervisedClasses.allowedPackages("),
				"a pinned scope's permissions are a declaration, not a reading: " + written);
	}
}
