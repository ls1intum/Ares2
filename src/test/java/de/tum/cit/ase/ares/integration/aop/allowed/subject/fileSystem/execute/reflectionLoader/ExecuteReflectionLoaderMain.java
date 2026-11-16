package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.reflectionLoader;

import java.io.IOException;

/**
 * Test subject that starts a trusted script through {@link Runtime}, standing
 * in for a reflective call. Ares analyses this class as if a student had
 * written it.
 */
public class ExecuteReflectionLoaderMain {

	/**
	 * Stops anyone from creating an instance, since this class only has static
	 * methods.
	 */
	private ExecuteReflectionLoaderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Starts the script with {@link Runtime#exec(String)}.
	 *
	 * @param filePath the script to start, relative to the project folder
	 * @return the started process, so that the caller can check what it did
	 * @throws IOException if the script cannot be started
	 */
	public static Process accessFileSystemViaReflectionLoader(String filePath) throws IOException {
		return Runtime.getRuntime().exec(filePath);
	}
}
