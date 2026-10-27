package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;

/**
 * Student code in a field initialiser and {@code @BeforeAll} is denied under
 * WALA and instrumentation.
 */
@Public
@Policy(value = SystemAccessTest.WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = AbstractSetupMethodsUnderClassPolicyTest.WITHIN_PATH)
class SetupMethodsUnderClassPolicyWalaInstrumentationTest extends AbstractSetupMethodsUnderClassPolicyTest {
	/** The field initialiser was denied. */
	@Test
	void test_studentCodeInFieldInitialiserIsBlockedMavenWalaInstrumentation() {
		assertFieldInitialiserWasBlocked();
	}

	/** The {@code @BeforeAll} method was denied. */
	@Test
	void test_studentCodeInBeforeAllIsBlockedMavenWalaInstrumentation() {
		assertBeforeAllWasBlocked();
	}
}
