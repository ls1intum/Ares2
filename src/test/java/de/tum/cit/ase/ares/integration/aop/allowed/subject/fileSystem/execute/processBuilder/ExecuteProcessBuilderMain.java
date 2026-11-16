package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.processBuilder;

import java.io.IOException;

/**
 * Test subject that starts a trusted script through {@link ProcessBuilder}.
 * Ares analyses this class as if a student had written it.
 */
public class ExecuteProcessBuilderMain {

	/**
	 * Stops anyone from creating an instance, since this class only has static
	 * methods.
	 */
	private ExecuteProcessBuilderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Starts the script with {@link ProcessBuilder}.
	 *
	 * @param filePath the script to start, relative to the project folder
	 * @return the started process, so that the caller can check what it did
	 * @throws IOException if the script cannot be started
	 */
	public static Process accessFileSystemViaProcessBuilder(String filePath) throws IOException {
		return new ProcessBuilder(filePath).start();
	}
}
