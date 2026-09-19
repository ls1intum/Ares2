package de.tum.cit.ase.ares.testutilities;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.util.List;
import java.util.Map;

/**
 * Registers a synthetic, student-authored-style {@link SecureRandomSpi} whose
 * {@code engineGenerateSeed} runs a caller-supplied probe, then calls
 * {@link SecureRandom#generateSeed(int)} on it. This makes
 * {@code java.security.SecureRandom.generateSeed(...)} a genuine caller frame
 * on the real stack while the probe runs — the exact spoof both AOP backends'
 * narrowed {@code sun.security.provider.*}-only SecureRandom-seeding stack
 * detector is designed to reject, since no genuine JDK seeding is actually
 * taking place.
 * <p>
 * Shared between the instrumentation and AspectJ baseline-low-risk-exemption
 * unit tests. The probe is threaded through {@link ProbingSecureRandomSpi} as a
 * constructor-injected instance field (via a custom
 * {@link Provider.Service#newInstance}) rather than a static field, so
 * concurrent/parallel test execution cannot cross-contaminate between callers.
 */
public final class FakeSecureRandomSeedingFixture {

	private FakeSecureRandomSeedingFixture() {
	}

	@FunctionalInterface
	public interface SeedingProbe {
		void run() throws Exception;
	}

	public static void triggerFakeSecureRandomSeeding(SeedingProbe probe) throws Exception {
		Provider provider = new Provider("ares-hotfix-test-secure-random-provider", "1.0",
				"Ares test fixture provider for exercising the SecureRandom-seeding stack detector") {
			private static final long serialVersionUID = 1L;

			{
				putService(new Service(this, "SecureRandom", "AresProbe", ProbingSecureRandomSpi.class.getName(),
						List.of(), Map.of()) {
					@Override
					public Object newInstance(Object constructorParameter) {
						return new ProbingSecureRandomSpi(probe);
					}
				});
			}
		};
		SecureRandom.getInstance("AresProbe", provider).generateSeed(1);
	}

	public static final class ProbingSecureRandomSpi extends SecureRandomSpi {

		private static final long serialVersionUID = 1L;

		private final transient SeedingProbe probe;

		ProbingSecureRandomSpi(SeedingProbe probe) {
			this.probe = probe;
		}

		@Override
		protected byte[] engineGenerateSeed(int numBytes) {
			try {
				probe.run();
			} catch (RuntimeException e) {
				// Let unchecked exceptions (in particular the SecurityException this fixture
				// exists to provoke) propagate as-is; only a genuine checked exception from
				// the functional interface needs wrapping to cross the override boundary.
				throw e;
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
