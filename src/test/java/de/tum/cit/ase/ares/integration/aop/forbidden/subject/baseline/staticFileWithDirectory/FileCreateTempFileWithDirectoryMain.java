package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticFileWithDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Student code calling {@link File#createTempFile(String, String, File)}, which
 * the static analysis still denies when the policy grants no file access.
 */
public final class FileCreateTempFileWithDirectoryMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private FileCreateTempFileWithDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FileCreateTempFileWithDirectoryMain is a utility class and should not be instantiated.");
	}

	/**
	 * Makes the call.
	 *
	 * @return what the call created
	 * @throws IOException if it cannot be created
	 */
	public static File createTempFile() throws IOException {
		return File.createTempFile("ares-baseline-", ".tmp", new File(System.getProperty("java.io.tmpdir")));
	}
}
