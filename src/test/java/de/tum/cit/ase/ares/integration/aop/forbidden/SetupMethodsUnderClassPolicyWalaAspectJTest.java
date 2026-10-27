package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;

/**
 * Student code in a field initialiser and {@code @BeforeAll} is denied under
 * WALA and AspectJ.
 */
@Public
@Policy(value = SystemAccessTest.WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = AbstractSetupMethodsUnderClassPolicyTest.WITHIN_PATH)
class SetupMethodsUnderClassPolicyWalaAspectJTest extends AbstractSetupMethodsUnderClassPolicyTest {
	/** The field initialiser was denied. */
	@Test
	void test_studentCodeInFieldInitialiserIsBlockedMavenWalaAspectJ() {
		assertFieldInitialiserWasBlocked();
	}

	/** The {@code @BeforeAll} method was denied. */
	@Test
	void test_studentCodeInBeforeAllIsBlockedMavenWalaAspectJ() {
		assertBeforeAllWasBlocked();
	}
}
