package de.tum.cit.ase.ares.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

class AresConstantsTest {

	@Test
	void exposesAConsistentSupportedPolicyVersionRange() {
		assertEquals(1, AresConstants.MINIMUM_POLICY_VERSION);
		assertEquals(1, AresConstants.MAXIMUM_POLICY_VERSION);
		assertTrue(AresConstants.MINIMUM_POLICY_VERSION <= AresConstants.MAXIMUM_POLICY_VERSION);
	}

	@Test
	@SuppressWarnings("removal")
	void keepsTheReleasedVersionConstantAsAnAliasOfTheMaximum() {
		// SecurityPolicy.CURRENT_POLICY_VERSION shipped in v2.1.0 and is retained as a
		// deprecated alias, so a downstream source that names it still compiles. It
		// aliases the maximum rather than the minimum, since it meant "the version this
		// release writes", which is what the builder still defaults to.
		assertEquals(AresConstants.MAXIMUM_POLICY_VERSION,
				de.tum.cit.ase.ares.api.policy.SecurityPolicy.CURRENT_POLICY_VERSION);
	}

	@Test
	void cannotBeInstantiatedReflectively() throws NoSuchMethodException {
		Constructor<AresConstants> constructor = AresConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}
}
