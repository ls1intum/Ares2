package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.overwrite.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ArchitectureWriteFilesWriteMain {

	private static final String NOT_TRUSTED_DIR = "src/test/java/de/tum/cit/ase/ares/integration/architecture/forbidden/subject/fileSystem/overwrite/nottrusteddir";
	private static final Path NOT_TRUSTED_FILE = Path.of(NOT_TRUSTED_DIR, "nottrusted.txt");

	private ArchitectureWriteFilesWriteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using the
	 * {@link Files#write(Path, byte[], java.nio.file.OpenOption...)} method.
	 */
	public static void accessFileSystemViaFilesWrite() throws IOException {
		byte[] content = "Hello, world!".getBytes();
		Files.write(NOT_TRUSTED_FILE, content, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
