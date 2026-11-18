package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.inspect;

/**
 * A student class that nothing uses before {@link InspectMain} does, so the JVM
 * loads it while student code is running.
 */
final class InspectHelper {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private InspectHelper() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Helper is a utility class and should not be instantiated.");
	}

	/**
	 * Gives back a fixed value.
	 *
	 * @return the value
	 */
	static int answer() {
		return 42;
	}
}
