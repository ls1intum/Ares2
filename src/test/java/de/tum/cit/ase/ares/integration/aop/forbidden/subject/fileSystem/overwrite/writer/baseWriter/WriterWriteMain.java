package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.baseWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class WriterWriteMain {

	private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt";

	private WriterWriteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): WriterWriteMain is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using a concrete {@link java.io.Writer} constructor.
	 */
	public static void accessFileSystemViaWriterConstructor() throws IOException {
		try (Writer writer = new FileWriter(NOT_TRUSTED_FILE)) {
			writer.write("Hello, world!");
		}
	}
}
