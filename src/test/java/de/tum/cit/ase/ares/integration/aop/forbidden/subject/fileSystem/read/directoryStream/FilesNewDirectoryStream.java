package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.directoryStream;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesNewDirectoryStream {

	private FilesNewDirectoryStream() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Lists a directory that the policy does not permit reading, using
	 * {@code Files.newDirectoryStream(Path, String)} with the glob {@code "*"}. The
	 * directory (the first argument) must still be checked for read permission, so
	 * this must be denied even though the glob is ignored.
	 */
	public static void accessFileSystemViaFilesNewDirectoryStream() throws IOException {
		Path directory = Paths.get(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/directoryStream");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*")) {
			for (Path entry : stream) {
				entry.getFileName();
			}
		}
	}
}
