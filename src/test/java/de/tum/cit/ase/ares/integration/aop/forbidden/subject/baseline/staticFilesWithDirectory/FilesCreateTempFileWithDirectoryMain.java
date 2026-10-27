package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticFilesWithDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code calling
 * {@link Files#createTempFile(Path, String, String, java.nio.file.attribute.FileAttribute...)},
 * which the static analysis still denies when the policy grants no file access.
 */
public final class FilesCreateTempFileWithDirectoryMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private FilesCreateTempFileWithDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FilesCreateTempFileWithDirectoryMain is a utility class and should not be instantiated.");
	}

	/**
	 * Makes the call.
	 *
	 * @return what the call created
	 * @throws IOException if it cannot be created
	 */
	public static Path createTempFile() throws IOException {
		return Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")), "ares-baseline-", ".tmp");
	}
}
