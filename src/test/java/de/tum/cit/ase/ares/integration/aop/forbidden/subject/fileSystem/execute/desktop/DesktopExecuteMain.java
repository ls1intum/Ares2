package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop;

import java.io.IOException;

public class DesktopExecuteMain {

	private static final String NOT_TRUSTED_PATH = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt";

	private DesktopExecuteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	public static void accessFileSystemViaDesktopBrowse() throws IOException {
		Runtime.getRuntime().exec(NOT_TRUSTED_PATH);
	}

	/**
	 * Access the file system through the browse-directory-style execution fixture.
	 */
	public static void accessFileSystemViaDesktopBrowseFileDirectory() throws IOException {
		Runtime.getRuntime().exec(NOT_TRUSTED_PATH);
	}

	/**
	 * Access the file system through the desktop execution fixture.
	 */
	public static void accessFileSystemViaDesktop() throws IOException {
		Runtime.getRuntime().exec(NOT_TRUSTED_PATH);
	}

	/**
	 * Access the file system through the desktop-edit execution fixture.
	 */
	public static void accessFileSystemViaDesktopEdit() throws IOException {
		Runtime.getRuntime().exec(NOT_TRUSTED_PATH);
	}

	/**
	 * Access the file system through the desktop-print execution fixture.
	 */
	public static void accessFileSystemViaDesktopPrint() throws IOException {
		Runtime.getRuntime().exec(NOT_TRUSTED_PATH);
	}
}
