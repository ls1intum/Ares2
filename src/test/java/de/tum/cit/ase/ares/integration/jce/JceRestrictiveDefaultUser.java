package de.tum.cit.ase.ares.integration.jce;

import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import example.jce.JceCryptoSubject;

/**
 * Attempts first crypto use through actual supervision without an explicit
 * policy.
 */
public class JceRestrictiveDefaultUser {

	/**
	 * Leaves discovery to Ares and exposes any unexpected entry into student
	 * crypto.
	 */
	@PublicTest
	void firstCryptoUseWithoutPolicy() throws Exception {
		System.out.println("JCE_DEFAULT_CRYPTO_ENTRY");
		JceCryptoSubject.initialiseCrypto(JceForkProbe.testRandom());
		throw new AssertionError("The restrictive default unexpectedly entered student crypto");
	}
}
