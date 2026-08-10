package de.tum.cit.ase.ares.api.aop.java;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.time.LocalDate;
import java.time.temporal.TemporalQuery;
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
	void isSecureRandomSeedingInProgressIsNotForgedByACustomSecureRandomSpi() throws Exception {
		// Adversarial test (Trusted Boundary Preservation): a student-authored
		// SecureRandomSpi's engineGenerateSeed genuinely runs beneath a real
		// java.security.SecureRandom.generateSeed(...) frame, but that public
		// dispatch frame is not itself trusted - only genuine
		// sun.security.provider.* internal implementation frames are - so this must
		// stay false.
		AtomicBoolean seedingDetected = new AtomicBoolean();
		triggerFakeSecureRandomSeeding(
				() -> seedingDetected.set(AspectJSecurityProbe.isSecureRandomSeedingInProgress()));

		assertFalse(seedingDetected.get());
	}

	@Test
	void isEntropySourceReadReturnsFalseWithoutSecureRandomFrame() throws Exception {
		// A student opening the entropy device directly (no SecureRandom-seeding
		// frame on the stack) must never be exempted by path name alone.
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/urandom"));
		assertFalse(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/random"));
	}

	@Test
	void isEntropySourceReadIsNotForgedByACustomSecureRandomSpi() throws Exception {
		AtomicBoolean entropyReadExempt = new AtomicBoolean();
		triggerFakeSecureRandomSeeding(
				() -> entropyReadExempt.set(AspectJSecurityProbe.isEntropySourceRead("read", "/dev/urandom")));

		assertFalse(entropyReadExempt.get(),
				"a custom SecureRandomSpi must not be able to forge the entropy-device read exemption");
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

	@Test
	void isSystemTimezoneReadIsNotForgedByATemporalQueryCallback() throws Exception {
		// Adversarial test (Trusted Boundary Preservation): java.time.temporal's
		// default query(...) method dispatches synchronously to a caller-supplied
		// TemporalQuery from within a genuinely JDK-declared java.time.temporal.*
		// frame. Broadly trusting the "java.time." prefix (as a prior version of
		// this exemption did) would let a student forge the system-timezone
		// exemption this way; only sun.util.calendar.* internals are trusted now,
		// so this must stay false.
		AtomicBoolean timezoneReadExempt = new AtomicBoolean();
		TemporalQuery<Void> maliciousQuery = temporal -> {
			try {
				timezoneReadExempt.set(AspectJSecurityProbe.isSystemTimezoneRead("read", "/etc/localtime"));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return null;
		};

		LocalDate.now().query(maliciousQuery);

		assertFalse(timezoneReadExempt.get(),
				"a java.time.temporal.Temporal callback must not be able to forge the system-timezone read exemption");
	}

	/**
	 * Registers a synthetic, student-authored-style {@link SecureRandomSpi} whose
	 * {@code engineGenerateSeed} runs the given probe, then calls
	 * {@link SecureRandom#generateSeed(int)} on it. This makes
	 * {@code java.security.SecureRandom.generateSeed(...)} a genuine caller frame
	 * on the real stack while the probe runs — the exact spoof the narrowed
	 * {@code sun.security.provider.*}-only trust set is designed to reject, since
	 * no genuine JDK seeding is actually taking place.
	 */
	private static void triggerFakeSecureRandomSeeding(SeedingProbe probe) throws Exception {
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
