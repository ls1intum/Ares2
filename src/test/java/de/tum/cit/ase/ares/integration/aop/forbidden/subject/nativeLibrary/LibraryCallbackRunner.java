package de.tum.cit.ase.ares.integration.aop.forbidden.subject.nativeLibrary;

/**
 * Student code that runs code handed in by the test, so that student code is on
 * the stack while that code asks for a native library.
 */
public final class LibraryCallbackRunner {

	/** Prevents instances of the student class. */
	private LibraryCallbackRunner() {
	}

	/**
	 * Runs the given code.
	 *
	 * @param callback the code to run
	 */
	public static void run(Runnable callback) {
		callback.run();
	}
}
