package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * The public constructors Ares 2.1.2 published, kept callable.
 * <p>
 * The supervised scope became a constructor parameter, which is what stops a
 * test case existing without one. Doing that by replacing the released
 * signatures would have removed method descriptors a client may already be
 * compiled against, so each is kept as a delegating bridge. These assertions
 * exist because nothing else in the repository calls them: the production path
 * builds through the builders, so a future removal would pass every other test.
 * <p>
 * What each bridge produces is asserted too, not merely that it links. A test
 * case built without a scope must refuse to write a generated file rather than
 * write one that analyses nothing, which is the failure this whole change is
 * about.
 */
@SuppressWarnings({ "deprecation", "removal" })
class ReleasedConstructorCompatibilityTest {

	private static final Set<PackagePermission> PACKAGES = Set.of(new PackagePermission("com.example"));

	private static final Set<ClassPermission> CLASSES = Set.of();

	private static JavaClasses noClasses() {
		return new ClassFileImporter().importPackages("non.existent.package");
	}

	@Test
	@DisplayName("The two released JavaArchitectureTestCase constructors still link")
	void javaArchitectureTestCaseKeepsItsReleasedConstructors() {
		CallGraph callGraph = Mockito.mock(CallGraph.class);
		Supplier<CallGraph> supplier = () -> callGraph;

		JavaArchitectureTestCase eager = assertDoesNotThrow(() -> new JavaArchitectureTestCase(
				JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES, noClasses(), callGraph));
		JavaArchitectureTestCase full = assertDoesNotThrow(() -> new JavaArchitectureTestCase(
				JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES, noClasses(), null, supplier, CLASSES));

		for (JavaArchitectureTestCase testCase : Set.of(eager, full)) {
			assertNull(testCase.getSupervisedPackage(), "a bridge cannot invent a scope the caller never gave");
			assertFalse(testCase.isSupervisedScopeWasDerived(),
					"nothing was derived here, so the generated file must not claim a derived scope");
		}
	}

	@Test
	@DisplayName("The two released JavaArchunitTestCase constructors still link")
	void javaArchunitTestCaseKeepsItsReleasedConstructors() {
		JavaArchunitTestCase bare = assertDoesNotThrow(() -> new JavaArchunitTestCase(
				JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES, noClasses()));
		JavaArchunitTestCase exempting = assertDoesNotThrow(() -> new JavaArchunitTestCase(
				JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES, noClasses(), CLASSES));

		assertNull(bare.getSupervisedPackage());
		assertNull(exempting.getSupervisedPackage());
	}

	@Test
	@DisplayName("The four released JavaWalaTestCase constructors still link")
	void javaWalaTestCaseKeepsItsReleasedConstructors() {
		CallGraph callGraph = Mockito.mock(CallGraph.class);
		Supplier<CallGraph> supplier = () -> callGraph;

		assertDoesNotThrow(() -> new JavaWalaTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES,
				noClasses(), callGraph));
		assertDoesNotThrow(() -> new JavaWalaTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES,
				noClasses(), callGraph, CLASSES));
		assertDoesNotThrow(() -> new JavaWalaTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES,
				noClasses(), supplier));
		assertDoesNotThrow(() -> new JavaWalaTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, PACKAGES,
				noClasses(), supplier, CLASSES));
	}

	@Test
	@DisplayName("A test case built without a scope refuses to write a generated file")
	void aScopelessTestCaseRefusesToGenerate() {
		JavaArchunitTestCase testCase = new JavaArchunitTestCase(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				PACKAGES, noClasses());

		// Refusing is the point. Writing the file would emit rules over an empty set
		// of classes, which passes and enforces nothing.
		assertThrows(SecurityException.class, () -> testCase.writeArchitectureTestCase("ARCHUNIT", ""));
	}
}
