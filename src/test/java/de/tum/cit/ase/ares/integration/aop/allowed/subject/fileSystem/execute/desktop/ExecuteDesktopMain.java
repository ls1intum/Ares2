package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.desktop;

import java.io.IOException;

public class ExecuteDesktopMain {

	private static final String TRUSTED_SCRIPT_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trustedExecute.sh";

	private ExecuteDesktopMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system by executing the trusted file.
	 */
	public static void accessFileSystemViaDesktop(String filePath) throws IOException {
		Process process = Runtime.getRuntime().exec(filePath);
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while executing trusted file.", e);
		}
	}

	/**
	 * Access the file system by executing the default trusted file.
	 */
	public static void accessFileSystemViaDesktop() throws IOException {
		accessFileSystemViaDesktop(TRUSTED_SCRIPT_PATH);
	}

	/**
	 * Access the file system through the browse-style execution fixture.
	 */
	public static void accessFileSystemViaDesktopBrowse() throws IOException {
		accessFileSystemViaDesktop(TRUSTED_SCRIPT_PATH);
	}

	/**
	 * Access the file system through the browse-directory-style fixture.
	 */
	public static void accessFileSystemViaDesktopBrowseFileDirectory() {
		// No-op on headless build agents.
	}

	/**
	 * Access the file system through the edit-style execution fixture.
	 */
	public static void accessFileSystemViaDesktopEdit() throws IOException {
		accessFileSystemViaDesktop(TRUSTED_SCRIPT_PATH);
	}

	/**
	 * Access the file system through the print-style execution fixture.
	 */
	public static void accessFileSystemViaDesktopPrint() throws IOException {
		accessFileSystemViaDesktop(TRUSTED_SCRIPT_PATH);
	}
}
