package de.tum.cit.ase.ares.testutilities;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.util.List;
import java.util.Map;

/**
 * Plugs a student-style random generator in beneath the public
 * {@link SecureRandom} class and runs a probe while that class asks it for a
 * seed. The probe then runs below a real {@code SecureRandom} frame without any
 * JDK seeding taking place, which is exactly what the random-seed exemption
 * must not trust.
 */
public final class FakeSecureRandomSeedingFixture {

	/**
	 * Prevents instantiation of this fixture.
	 */
	private FakeSecureRandomSeedingFixture() {
	}

	/**
	 * Code to run while the fake generator is asked for a seed.
	 */
	@FunctionalInterface
	public interface SeedingProbe {

		/**
		 * Runs the probe.
		 *
		 * @throws Exception whatever the probe throws
		 */
		void run() throws Exception;
	}

	/**
	 * Registers the fake generator under a throwaway provider and asks it for one
	 * seed byte, which runs the probe. Each call gets its own generator, so tests
	 * running in parallel cannot mix their probes.
	 *
	 * @param probe the code to run
	 * @throws Exception whatever the probe throws
	 */
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

	/**
	 * The fake generator: asked for a seed, it runs the probe.
	 */
	public static final class ProbingSecureRandomSpi extends SecureRandomSpi {

		/**
		 * Serialisation version of this generator.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The probe to run when a seed is requested.
		 */
		private final transient SeedingProbe probe;

		/**
		 * Creates the generator.
		 *
		 * @param probe the probe to run when a seed is requested
		 */
		ProbingSecureRandomSpi(SeedingProbe probe) {
			this.probe = probe;
		}

		/**
		 * Runs the probe and returns zero bytes. An unchecked exception, such as the
		 * denial the tests expect, passes through unchanged; a checked one is wrapped,
		 * since this method cannot declare it.
		 *
		 * @param numBytes the number of seed bytes requested
		 * @return that many zero bytes
		 */
		@Override
		protected byte[] engineGenerateSeed(int numBytes) {
			try {
				probe.run();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return new byte[numBytes];
		}

		/**
		 * Refuses, since the tests never set a seed and must notice if one is set.
		 *
		 * @param seed the seed
		 */
		@Override
		protected void engineSetSeed(byte[] seed) {
			throw new UnsupportedOperationException("the fake generator takes no seed");
		}

		/**
		 * Refuses, since the tests never draw random bytes and must notice if they do.
		 *
		 * @param bytes the bytes to fill
		 */
		@Override
		protected void engineNextBytes(byte[] bytes) {
			throw new UnsupportedOperationException("the fake generator draws no bytes");
		}
	}
}
