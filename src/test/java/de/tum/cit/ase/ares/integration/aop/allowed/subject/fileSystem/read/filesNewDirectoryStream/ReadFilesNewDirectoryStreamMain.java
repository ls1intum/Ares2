package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.filesNewDirectoryStream;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReadFilesNewDirectoryStreamMain {

	private ReadFilesNewDirectoryStreamMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Lists a permitted directory with
	 * {@link Files#newDirectoryStream(Path, String)} and the glob {@code "*"}. The
	 * glob is the second argument, so it must not be treated as a path; only the
	 * directory (the first argument) is checked for read permission.
	 */
	public static void accessFileSystemViaFilesNewDirectoryStream() throws IOException {
		Path directory = Path.of(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/filesNewDirectoryStream");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*")) {
			for (Path entry : stream) {
				entry.getFileName();
			}
		}
	}
}
