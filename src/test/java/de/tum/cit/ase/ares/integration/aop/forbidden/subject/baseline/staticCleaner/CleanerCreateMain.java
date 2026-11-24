package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticCleaner;

import java.lang.ref.Cleaner;

/**
 * Student code creating a {@link Cleaner}, which starts a thread inside the
 * JDK, under a policy that permits no thread.
 */
public final class CleanerCreateMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private CleanerCreateMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): CleanerCreateMain is a utility class and should not be instantiated.");
	}

	/**
	 * Makes the call.
	 *
	 * @return the cleaner
	 */
	public static Cleaner create() {
		return Cleaner.create();
	}
}
