package de.tum.cit.ase.ares.integration.aop.allowed.subject.nativeLibrary;

import com.sun.security.auth.module.UnixSystem;

/**
 * Student code that reads the user id through the JDK, which loads its own
 * {@code jaas} library for every new reader.
 */
public final class JdkLibraryUser {

	/** Prevents instances of the student class. */
	private JdkLibraryUser() {
	}

	/**
	 * Reads the numeric id of the current user.
	 *
	 * @return that id
	 */
	public static long readUserId() {
		return new UnixSystem().getUid();
	}

	/**
	 * Runs code handed in by the test, so that student code is on the stack while
	 * it runs.
	 *
	 * @param callback the code to run
	 */
	public static void run(Runnable callback) {
		callback.run();
	}
}
