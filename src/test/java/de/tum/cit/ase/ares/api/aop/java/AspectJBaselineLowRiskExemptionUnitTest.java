package de.tum.cit.ase.ares.api.aop.java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.util.concurrent.atomic.AtomicBoolean;

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
	void isSecureRandomSeedingInProgressReturnsTrueDuringRealSecureRandomSeeding() throws Exception {
		AtomicBoolean seedingDetected = new AtomicBoolean();
		triggerRealSecureRandomSeeding(
				() -> seedingDetected.set(AspectJSecurityProbe.isSecureRandomSeedingInProgress()));

		// java.security.SecureRandom.generateSeed(...) genuinely carries a
		// java.security.SecureRandom frame on the real call stack, so this must be
		// true without needing an actual OS entropy device read.
		assertTrue(seedingDetected.get());
	}

	@Test
	void isEntropySourceReadReturnsFalseWithoutSecureRandomFrame() throws Exception {
		// A student opening the entropy device directly (no SecureRandom-seeding
		// frame on the stack) must never be exempted by path name alone.
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/random"));
	}

	@Test
	void isEntropySourceReadReturnsTrueDuringRealSecureRandomSeeding() throws Exception {
		AtomicBoolean entropyReadExempt = new AtomicBoolean();
		triggerRealSecureRandomSeeding(
				() -> entropyReadExempt.set(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/urandom")));

		assertTrue(entropyReadExempt.get(),
				"a real SecureRandom entropy read should be exempt even under an active policy");
	}

	@Test
	void isEntropySourceReadReturnsFalseForUnrelatedPathOrAction() throws Exception {
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/not-urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("overwrite", "/dev/urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", null));
	}

	@Test
	void isSystemTimezoneReadDirectlyByStudentCodeIsDenied() throws Exception {
		// No trusted java.time timezone-resolution frame is on this stack, so a
		// student opening the symlink directly must never be exempted by path name
		// alone.
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("read", "/etc/localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("overwrite", "/etc/localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("read", "/etc/not-localtime"));
		assertFalse(AspectJSecurityProbe.isSystemTimezoneRead("read", null));
	}

	/**
	 * Registers a synthetic {@link SecureRandomSpi} whose
	 * {@code engineGenerateSeed} runs the given probe, then calls
	 * {@link SecureRandom#generateSeed(int)} on it. This makes
	 * {@code java.security.SecureRandom.generateSeed(...)} a genuine caller frame
	 * on the real stack while the probe runs, without needing an actual OS entropy
	 * device to exist or be read.
	 */
	private static void triggerRealSecureRandomSeeding(SeedingProbe probe) throws Exception {
		ProbingSecureRandomSpi.PROBE = probe;
		try {
			Provider provider = new Provider("ares-hotfix-test-secure-random-provider", "1.0",
					"Ares test fixture provider for exercising the SecureRandom-seeding stack detector") {
				private static final long serialVersionUID = 1L;
			};
			provider.put("SecureRandom.AresProbe", ProbingSecureRandomSpi.class.getName());
			SecureRandom.getInstance("AresProbe", provider).generateSeed(1);
		} finally {
			ProbingSecureRandomSpi.PROBE = null;
		}
	}

	@FunctionalInterface
	private interface SeedingProbe {
		void run() throws Exception;
	}

	public static final class ProbingSecureRandomSpi extends SecureRandomSpi {

		private static volatile SeedingProbe PROBE;

		@Override
		protected byte[] engineGenerateSeed(int numBytes) {
			try {
				PROBE.run();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return new byte[numBytes];
		}

		@Override
		protected void engineSetSeed(byte[] seed) {
			// Not exercised by these tests.
		}

		@Override
		protected void engineNextBytes(byte[] bytes) {
			// Not exercised by these tests.
		}
	}
}
