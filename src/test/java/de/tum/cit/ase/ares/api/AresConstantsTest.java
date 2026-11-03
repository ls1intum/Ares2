package de.tum.cit.ase.ares.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * Checks the shared constants of Ares: the range of policy-format versions it
 * reads, the old constant kept for compatibility, and that the class cannot be
 * instantiated.
 */
class AresConstantsTest {

	/**
	 * Checks that version 1 is both the lowest and the highest supported policy
	 * version, and that the lowest does not exceed the highest.
	 */
	@Test
	void exposesAConsistentSupportedPolicyVersionRange() {
		assertEquals(1, AresConstants.MINIMUM_POLICY_VERSION);
		assertEquals(1, AresConstants.MAXIMUM_POLICY_VERSION);
		assertTrue(AresConstants.MINIMUM_POLICY_VERSION <= AresConstants.MAXIMUM_POLICY_VERSION);
	}

	/**
	 * Checks that the deprecated {@code SecurityPolicy.CURRENT_POLICY_VERSION},
	 * shipped in 2.1.0 and kept so code naming it still compiles, equals the
	 * maximum version. It aliases the maximum rather than the minimum because it
	 * meant "the version this release writes", which the builder still defaults to.
	 */
	@Test
	@SuppressWarnings("removal")
	void keepsTheReleasedVersionConstantAsAnAliasOfTheMaximum() {
		assertEquals(AresConstants.MAXIMUM_POLICY_VERSION,
				de.tum.cit.ase.ares.api.policy.SecurityPolicy.CURRENT_POLICY_VERSION);
	}

	/**
	 * Checks that calling the private constructor through reflection fails with a
	 * {@link SecurityException}.
	 */
	@Test
	void cannotBeInstantiatedReflectively() throws NoSuchMethodException {
		Constructor<AresConstants> constructor = AresConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}
}
