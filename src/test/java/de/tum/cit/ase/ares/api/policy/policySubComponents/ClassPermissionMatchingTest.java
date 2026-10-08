package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

/** Guards the class-name boundary shared by packaged and generated checks. */
class ClassPermissionMatchingTest {

	/** An exemption covers nested classes but never a neighbouring class name. */
	@ParameterizedTest
	@CsvSource({ "fixture.Trusted, true", "fixture.Trusted$Nested, true", "fixture.Trusted$1, true",
			"fixture.TrustedOther, false", "fixture.Trusted.Nested, false", "other.Trusted, false" })
	void exemptionsRespectClassNameBoundaries(String className, boolean allowed) {
		Set<ClassPermission> exemptions = Set.of(new ClassPermission("fixture.Trusted"));
		assertEquals(allowed, ClassPermission.isAllowedClass(className, exemptions));
		assertEquals(allowed, JavaArchitectureTestCase.isAllowedClass(className, exemptions));
	}

	/** Missing names or exemptions cannot authorise access. */
	@Test
	void missingInputsDoNotGrantExemptions() {
		assertFalse(ClassPermission.isAllowedClass(null, Set.of(new ClassPermission("fixture.Trusted"))));
		assertFalse(ClassPermission.isAllowedClass("fixture.Trusted", null));
		assertFalse(ClassPermission.isAllowedClass("fixture.Trusted", Set.of()));
	}
}
