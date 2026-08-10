package de.tum.cit.ase.ares.api.aop.java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import example.student.AspectJSecurityProbe;

/**
 * Unit tests for the AspectJ backend's three new baseline low-risk exemption
 * helpers (I-baseline-low-risk-jdk-read-exemptions): the entropy-device read
 * exemption, the system-timezone read exemption, and the SecureRandom-seeding
 * stack detector they both build on. Exercised via
 * {@link AspectJSecurityProbe}'s reflective access, mirroring how
 * {@code JavaInstrumentationAdviceFileSystemToolboxTest} exercises the
 * equivalent instrumentation-backend logic directly.
 */
class AspectJBaselineLowRiskExemptionUnitTest {

	@Test
	void isSecureRandomSeedingInProgressReturnsFalseWhenNotSeeding() throws Exception {
		// Called directly from a plain test method: no SecureRandom frame is on the
		// real stack, so this must be false.
		assertFalse(AspectJSecurityProbe.isSecureRandomSeedingInProgress());
	}

	@Test
	void isEntropySourceReadReturnsFalseWithoutSecureRandomFrame() throws Exception {
		// A student opening the entropy device directly (no SecureRandom-seeding
		// frame on the stack) must never be exempted by path name alone.
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/random"));
	}

	@Test
	void isEntropySourceReadReturnsFalseForUnrelatedPathOrAction() throws Exception {
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/not-urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("overwrite", "/dev/urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", null));
	}

	@Test
	void isSystemTimezoneReadMatchesTheSystemTimezoneFileOnReadOnly() throws Exception {
		assertTrue(AspectJSecurityProbe.isSystemTimezoneRead("read", "/etc/localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("overwrite", "/etc/localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("read", "/etc/not-localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("read", null));
	}
}
