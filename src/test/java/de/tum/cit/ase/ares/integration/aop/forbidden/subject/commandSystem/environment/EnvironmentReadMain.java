package de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.environment;

/** Student code that reads an environment variable through a ProcessBuilder. */
public final class EnvironmentReadMain {

	/** Prevents creating instances of this utility class. */
	private EnvironmentReadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Reads the home folder from the environment a ProcessBuilder carries.
	 *
	 * @return the value, or {@code null}
	 */
	public static String readHome() {
		return new ProcessBuilder().environment().get("HOME");
	}
}
