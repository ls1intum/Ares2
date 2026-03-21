package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.file;

import java.io.File;

public final class FileCreateDirectoryMain {

	private static final String NOT_TRUSTED_DIR = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/nottrusteddir";
	private static final String NOT_TRUSTED_SUBDIR = NOT_TRUSTED_DIR + "/createdDir";
	private static final String NOT_TRUSTED_NESTED_DIR = NOT_TRUSTED_DIR + "/nested/dir";

	private FileCreateDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FileCreateDirectoryMain is a utility class and should not be instantiated.");
	}

	/** {@link java.io.File#mkdir()} */
	public static void accessFileSystemViaFileMkdir() {
		new File(NOT_TRUSTED_SUBDIR).mkdir();
	}

	/** {@link java.io.File#mkdirs()} */
	public static void accessFileSystemViaFileMkdirs() {
		new File(NOT_TRUSTED_NESTED_DIR).mkdirs();
	}
}
