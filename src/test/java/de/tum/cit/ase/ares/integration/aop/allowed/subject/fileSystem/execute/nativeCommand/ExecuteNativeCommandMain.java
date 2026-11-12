package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.nativeCommand;

import java.io.IOException;

/**
 * Test subject that starts a trusted script through a native command. Ares
 * analyses this class as if a student had written it.
 */
public class ExecuteNativeCommandMain {

	/**
	 * Stops anyone from creating an instance, since this class only has static
	 * methods.
	 */
	private ExecuteNativeCommandMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Starts the script as a native command.
	 *
	 * @param filePath the script to start, relative to the project folder
	 * @return the started process, so that the caller can check what it did
	 * @throws IOException if the script cannot be started
	 */
	public static Process accessFileSystemViaNativeCommand(String filePath) throws IOException {
		return Runtime.getRuntime().exec(filePath);
	}
}
