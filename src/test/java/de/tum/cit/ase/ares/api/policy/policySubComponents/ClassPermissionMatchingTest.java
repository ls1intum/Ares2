package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

/** Guards the class-name boundary shared by packaged and generated checks. */
class ClassPermissionMatchingTest {

	/**
	 * An exemption covers exactly the listed name; a name that merely looks nested
	 * in it, or neighbours it, is not covered.
	 */
	@ParameterizedTest
	@CsvSource({ "fixture.Trusted, true", "fixture.Trusted$Nested, false", "fixture.Trusted$1, false",
			"fixture.TrustedOther, false", "fixture.Trusted.Nested, false", "other.Trusted, false" })
	void exemptionsRespectClassNameBoundaries(String className, boolean allowed) {
		Set<ClassPermission> exemptions = Set.of(new ClassPermission("fixture.Trusted"));
		assertEquals(allowed, ClassPermission.isAllowedClass(className, exemptions));
		assertEquals(allowed, JavaArchitectureTestCase.isAllowedClass(className, exemptions));
	}

	/** A nested class is covered once its name comes from the nest listing. */
	@Test
	void listedNestMembersAreCoveredByName() {
		Set<ClassPermission> exemptions = Set.of(new ClassPermission("fixture.Trusted"),
				new ClassPermission("fixture.Trusted$Nested"));
		assertTrue(ClassPermission.isAllowedClass("fixture.Trusted$Nested", exemptions));
		assertFalse(ClassPermission.isAllowedClass("fixture.Trusted$Nested$Deeper", exemptions));
	}

	/** Missing names or exemptions cannot authorise access. */
	@Test
	void missingInputsDoNotGrantExemptions() {
		assertFalse(ClassPermission.isAllowedClass(null, Set.of(new ClassPermission("fixture.Trusted"))));
		assertFalse(ClassPermission.isAllowedClass("fixture.Trusted", null));
		assertFalse(ClassPermission.isAllowedClass("fixture.Trusted", Set.of()));
	}
}
