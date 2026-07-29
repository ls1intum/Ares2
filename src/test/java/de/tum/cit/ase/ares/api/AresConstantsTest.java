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
	void cannotBeInstantiatedReflectively() throws NoSuchMethodException {
		Constructor<AresConstants> constructor = AresConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}
}
