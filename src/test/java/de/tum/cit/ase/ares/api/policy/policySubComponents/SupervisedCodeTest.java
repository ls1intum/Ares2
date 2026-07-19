package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class SupervisedCodeTest {

	@Test
	void createRestrictiveShouldAllowNullPackageAndMainClass() {
		SupervisedCode supervisedCode = SupervisedCode
				.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION);

		assertNotNull(supervisedCode);
		assertNull(supervisedCode.theSupervisedCodeUsesTheFollowingPackage());
		assertNull(supervisedCode.theMainClassInsideThisPackageIs());
		assertEquals(List.of(), supervisedCode.theFollowingClassesAreTestClasses());
		assertNotNull(supervisedCode.theFollowingResourceAccessesArePermitted());
	}

	@Test
	void builderShouldAcceptNullPackageAndMainClass() {
		SupervisedCode supervisedCode = SupervisedCode.builder()
				.theFollowingProgrammingLanguageConfigurationIsUsed(
						ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION)
				.theSupervisedCodeUsesTheFollowingPackage(null).theMainClassInsideThisPackageIs(null)
				.theFollowingClassesAreTestClasses(List.of())
				.theFollowingResourceAccessesArePermitted(ResourceAccesses.createRestrictive()).build();

		assertNotNull(supervisedCode);
		assertNull(supervisedCode.theSupervisedCodeUsesTheFollowingPackage());
		assertNull(supervisedCode.theMainClassInsideThisPackageIs());
	}
}
