package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticManagementClassPath;

import java.lang.management.ManagementFactory;

/**
 * Student code reading the class path through {@code java.lang.management},
 * which the static analysis denies like {@code System.getProperty}.
 */
public final class ManagementClassPathMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private ManagementClassPathMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): ManagementClassPathMain is a utility class and should not be instantiated.");
	}

	/**
	 * Makes the call.
	 *
	 * @return the class path of the running JVM
	 */
	public static String classPath() {
		return ManagementFactory.getRuntimeMXBean().getClassPath();
	}
}
