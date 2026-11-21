package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.inspectStatic;

import java.io.File;

/**
 * Student code that asks whether a file exists, which a policy without any file
 * rule refuses before the test runs.
 */
public final class FileExistsMain {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private FileExistsMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Asks {@code File.exists}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}
}
