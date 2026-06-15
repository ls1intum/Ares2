package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileSystemProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public class ReadFileSystemProviderMain {

	private ReadFileSystemProviderMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using the {@link FileSystemProvider} class for
	 * reading.
	 * 
	 * @return The content of the trusted file
	 */
	public static String accessFileSystemViaFileSystemProvider() throws IOException {
		Path trustedFile = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");
		FileSystemProvider provider = trustedFile.getFileSystem().provider();
		try (InputStream is = provider.newInputStream(trustedFile)) {
			byte[] data = is.readAllBytes();
			return new String(data);
		}
	}
}
