package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * What a generated architecture test is told to do at its own runtime.
 * <p>
 * The generated file is the enforcement in the precompile flow, so what these
 * assert is not formatting but whether that file enforces anything: a pinned
 * scope is asked for at runtime, and a derived one is not written at all.
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
		String written = generated(false).writeArchitectureTestCase("ARCHUNIT", "");

		// The regression: with no classes compiled the emission fell back to
		// importPackages() with no arguments, so every rule checked an empty set and
		// passed. archunit.properties sets archRule.failOnEmptyShould=false, so
		// nothing objected.
		assertFalse(written.contains("importPackages()"),
				"a generated rule that imports no packages checks nothing and passes: " + written);
	}

	/**
	 * A generated file is only written in precompile, where nothing verifies a
	 * derived scope before the generated runtime checks use it. Both writers refuse
	 * it, so no lower-level path can emit one.
	 */
	@Test
	@DisplayName("Refuses to write a derived scope, in both analysis modes")
	void refusesToWriteADerivedScope() {
		SecurityException archunit = assertThrows(SecurityException.class,
				() -> generated(true).writeArchitectureTestCase("ARCHUNIT", ""));
		assertTrue(archunit.getMessage().contains("de.tum.cit.aet"), archunit.getMessage());
		JavaWalaTestCase wala = JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Set.of(new PackagePermission("de.tum.cit.aet")))
				.javaClasses(new ClassFileImporter().importPackages()).callGraph(Mockito.mock(CallGraph.class))
				.supervisedPackage("de.tum.cit.aet").supervisedScopeWasDerived(true).build();
		SecurityException walaRefusal = assertThrows(SecurityException.class,
				() -> wala.writeArchitectureTestCase("WALA", ""));
		assertTrue(walaRefusal.getMessage().contains("de.tum.cit.aet"), walaRefusal.getMessage());
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
