package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticTempDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code calling
 * {@link Files#createTempDirectory(String, java.nio.file.attribute.FileAttribute...)},
 * which the static analysis still denies when the policy grants no file access.
 */
public final class FilesCreateTempDirectoryMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private FilesCreateTempDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FilesCreateTempDirectoryMain is a utility class and should not be instantiated.");
	}

	/**
	 * Makes the call.
	 *
	 * @return the created directory
	 * @throws IOException if it cannot be created
	 */
	public static Path createTempDirectory() throws IOException {
		return Files.createTempDirectory("ares-baseline-");
	}
}
