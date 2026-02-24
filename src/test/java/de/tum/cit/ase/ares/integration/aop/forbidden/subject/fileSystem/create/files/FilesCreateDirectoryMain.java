package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FilesCreateDirectoryMain {

	private static final Path NOT_TRUSTED_DIR = Path.of(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/nottrusteddir");
	private static final Path NOT_TRUSTED_CREATE_DIR = NOT_TRUSTED_DIR.resolve("filesCreateDir");
	private static final Path NOT_TRUSTED_CREATE_DIRS = NOT_TRUSTED_DIR.resolve("filesCreateDirs/nested");

	private FilesCreateDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FilesCreateDirectoryMain is a utility class and should not be instantiated.");
	}

	/** {@link java.nio.file.Files#createDirectory(Path, java.nio.file.attribute.FileAttribute...)} */
	public static void accessFileSystemViaFilesCreateDirectory() throws IOException {
		Files.createDirectory(NOT_TRUSTED_CREATE_DIR);
	}

	/** {@link java.nio.file.Files#createDirectories(Path, java.nio.file.attribute.FileAttribute...)} */
	public static void accessFileSystemViaFilesCreateDirectories() throws IOException {
		Files.createDirectories(NOT_TRUSTED_CREATE_DIRS);
	}

	/** {@link java.nio.file.Files#createTempDirectory(Path, String, java.nio.file.attribute.FileAttribute...)} */
	public static void accessFileSystemViaFilesCreateTempDirectory() throws IOException {
		Files.createTempDirectory(NOT_TRUSTED_DIR, "ares-create-");
	}
}
