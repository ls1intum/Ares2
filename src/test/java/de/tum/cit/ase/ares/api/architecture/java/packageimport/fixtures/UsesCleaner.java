package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.lang.ref.Cleaner;

/**
 * Fixture creating a {@link Cleaner}, which starts a thread inside the JDK.
 * {@code java.lang.ref} lies below the baseline {@code java.lang}, so the
 * import rule allows it and the thread rule has to refuse it.
 */
public class UsesCleaner {

	/**
	 * Creates a cleaner and with it a new thread.
	 *
	 * @return the cleaner
	 */
	public Cleaner cleaner() {
		return Cleaner.create();
	}
}
