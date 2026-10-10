package example.jce;

import java.security.Provider;

/**
 * A student-like random-number provider, put first so the JDK's own
 * {@code new SecureRandom()} constructs its generator.
 */
public final class JceRandomProvider extends Provider {

	/** Keeps the serial form {@link Provider} requires. */
	private static final long serialVersionUID = 1L;

	/**
	 * Registers {@link JceRandomSpi} as this provider's random-number generator.
	 */
	public JceRandomProvider() {
		super("AresRandomFixture", "1.0", "Student-like SecureRandom provider");
		put("SecureRandom.AresFixtureRandom", JceRandomSpi.class.getName());
	}
}
