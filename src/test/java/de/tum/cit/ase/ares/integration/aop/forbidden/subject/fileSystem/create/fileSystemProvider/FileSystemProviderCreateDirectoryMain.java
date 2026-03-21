package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.fileSystemProvider;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public final class FileSystemProviderCreateDirectoryMain {

	private static final Path NOT_TRUSTED_DIR = Path.of(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/nottrusteddir/providerDir");

	private FileSystemProviderCreateDirectoryMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FileSystemProviderCreateDirectoryMain is a utility class and should not be instantiated.");
	}

	/**
	 * {@link java.nio.file.spi.FileSystemProvider#createDirectory(Path, java.nio.file.attribute.FileAttribute...)}
	 */
	public static void accessFileSystemViaFileSystemProviderCreateDirectory() throws IOException {
		FileSystemProvider provider = FileSystems.getDefault().provider();
		provider.createDirectory(NOT_TRUSTED_DIR);
	}
}
