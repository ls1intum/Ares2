package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;

/**
 * Student code in a field initialiser and {@code @BeforeAll} is denied under
 * ArchUnit and instrumentation.
 */
@Public
@Policy(value = SystemAccessTest.ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = AbstractSetupMethodsUnderClassPolicyTest.WITHIN_PATH)
class SetupMethodsUnderClassPolicyArchunitInstrumentationTest extends AbstractSetupMethodsUnderClassPolicyTest {
	/** The field initialiser was denied. */
	@Test
	void test_studentCodeInFieldInitialiserIsBlockedMavenArchunitInstrumentation() {
		assertFieldInitialiserWasBlocked();
	}

	/** The {@code @BeforeAll} method was denied. */
	@Test
	void test_studentCodeInBeforeAllIsBlockedMavenArchunitInstrumentation() {
		assertBeforeAllWasBlocked();
	}
}
