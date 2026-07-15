package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FilesDeleteOnClose {

	private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";

	private FilesDeleteOnClose() {
		throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): "
				+ "FilesDeleteOnClose is a utility class and should not be instantiated.");
	}

	/**
	 * Open a channel with {@link StandardOpenOption#DELETE_ON_CLOSE}. Any file open
	 * in channel will be deleted when the channel is closed.
	 */
	public static void closeChannelToDeleteFileInChannel(SeekableByteChannel ch) throws IOException {
		ch.close();
	}

	/**
	 * Opens and closes the forbidden path with DELETE_ON_CLOSE in supervised code.
	 */
	public static void deleteOnClose() throws IOException {
		try (SeekableByteChannel ignored = Files.newByteChannel(Path.of(NOT_TRUSTED_FILE), StandardOpenOption.WRITE,
				StandardOpenOption.DELETE_ON_CLOSE)) {
			// Closing the resource performs the requested deletion.
		}
	}
}
