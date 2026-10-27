package de.tum.cit.ase.ares.integration.aop.allowed.subject.baselineCertificates;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.TrustManagerFactory;

/**
 * Student code loading the JDK's trusted certificates, as setting up an
 * encrypted connection does. That needs a network entry in the policy, but no
 * file entry.
 */
public final class TrustedCertificatesMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private TrustedCertificatesMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): TrustedCertificatesMain is a utility class and should not be instantiated.");
	}

	/**
	 * Loads the JDK's trusted certificates.
	 *
	 * @return the number of trust managers created
	 * @throws GeneralSecurityException if the certificates cannot be loaded
	 */
	public static int loadTrustedCertificates() throws GeneralSecurityException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init((KeyStore) null);
		return factory.getTrustManagers().length;
	}
}
