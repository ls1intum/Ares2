package de.tum.cit.ase.ares.integration.jce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import example.jce.JceCryptoSubject;

/**
 * Exercises actual Jupiter activation and cleanup inside an isolated exercise
 * JVM.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JcePolicyLifecycleUser {

	/**
	 * An ordinary Jupiter test reads successfully before any Ares test executes.
	 */
	@Test
	@Order(1)
	void unsupervisedReadSucceeds() throws Exception {
		assertEquals("protected fixture", JceCryptoSubject.read(file()));
	}

	/**
	 * An explicit policy permits crypto and its listed read while denying the
	 * unlisted file.
	 */
	@PublicTest
	@Policy("policy.yaml")
	@Order(2)
	void activePolicyAllowsCryptoAndRejectsRead() throws Exception {
		System.out.println("JCE_LIFECYCLE_FIRST_CRYPTO");
		assertTrue(JceCryptoSubject.initialiseCrypto(JceForkProbe.testRandom()) >= 128);
		assertEquals("permitted fixture", JceCryptoSubject.read(Path.of("allowed.txt")));
		assertTrue(assertThrows(SecurityException.class, () -> JceCryptoSubject.read(file())).getMessage()
				.contains(file().toString()));
	}

	/**
	 * A failing supervised test still leaves teardown responsible for clearing the
	 * policy.
	 */
	@PublicTest
	@Policy("policy.yaml")
	@Order(3)
	void failingActiveTestCleansUp() {
		assertThrows(SecurityException.class, () -> JceCryptoSubject.read(file()));
		throw new AssertionError("Expected lifecycle failure");
	}

	/** Explicit deactivation permits the same read after a test failure. */
	@PublicTest
	@Policy(activated = false)
	@Order(4)
	void deactivatedPolicyReadsAfterFailure() throws Exception {
		assertEquals("protected fixture", JceCryptoSubject.read(file()));
	}

	/** Reactivation restores denials while repeated crypto remains available. */
	@PublicTest
	@Policy("policy.yaml")
	@Order(5)
	void reactivatedPolicyRejectsRead() throws Exception {
		assertTrue(JceCryptoSubject.initialiseCrypto(JceForkProbe.testRandom()) >= 128);
		assertThrows(SecurityException.class, () -> JceCryptoSubject.read(file()));
	}

	/**
	 * A blank policy invokes real project discovery and the restrictive default.
	 */
	@PublicTest
	@Order(6)
	void absentPolicyUsesRestrictiveDiscovery() throws Exception {
		JceCryptoSubject.read(file());
		throw new AssertionError("The discovered restrictive policy permitted a read");
	}

	/**
	 * A missing explicit policy fails during preparation rather than silently
	 * disabling Ares.
	 */
	@PublicTest
	@Policy("missing-policy.yaml")
	@Order(7)
	void failedPreparationCleansUp() {
		throw new AssertionError("A missing policy reached the test body");
	}

	/** A deactivated test remains usable after policy preparation failed. */
	@PublicTest
	@Policy(activated = false)
	@Order(8)
	void deactivatedPolicyReadsAfterPreparationFailure() throws Exception {
		assertEquals("protected fixture", JceCryptoSubject.read(file()));
	}

	/**
	 * Resolves the parent-created file without performing a supervised environment
	 * access.
	 */
	private static Path file() {
		return Path.of("default_local.policy").toAbsolutePath();
	}
}
