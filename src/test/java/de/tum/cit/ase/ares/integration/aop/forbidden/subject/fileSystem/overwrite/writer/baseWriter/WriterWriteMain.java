package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.baseWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public final class WriterWriteMain {

	private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt";

	private WriterWriteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): WriterWriteMain is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using a direct {@link java.io.Writer} subclass
	 * constructor to exercise the Writer.<new> pointcut.
	 */
	public static void accessFileSystemViaWriterConstructor() throws IOException {
		new FileBackedWriter(new File(NOT_TRUSTED_FILE));
	}

	private static final class FileBackedWriter extends Writer {
		@SuppressWarnings("unused")
		private final File file;

		private FileBackedWriter(File file) {
			this.file = file;
		}

		@Override
		public void write(char[] cbuf, int off, int len) {
			// no-op
		}

		@Override
		public void flush() {
			// no-op
		}

		@Override
		public void close() {
			// no-op
		}
	}
}
