package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.characterReader;

import java.io.*;

public class Reader {

	private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt";

	private Reader() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using Reader for character reading. Test Case:
	 * Reader.read() - Read one Unicode code unit; -1 at EOF
	 */
	public static void accessFileSystemViaReader() throws IOException {
		try (FileInputStream fis = new FileInputStream(NOT_TRUSTED_FILE);
				java.io.InputStreamReader isr = new java.io.InputStreamReader(fis);
				java.io.BufferedReader reader = new java.io.BufferedReader(isr)) {
			// Test various Reader operations
			reader.read(); // Single character read - returns -1 at EOF
		}
	}

	/**
	 * Access the file system using a direct {@link java.io.Reader} subclass
	 * constructor to exercise the Reader.<new> pointcut.
	 */
	public static void accessFileSystemViaReaderConstructor() throws IOException {
		new FileBackedReader(new File(NOT_TRUSTED_FILE));
	}

	private static final class FileBackedReader extends java.io.Reader {
		@SuppressWarnings("unused")
		private final File file;

		private FileBackedReader(File file) {
			this.file = file;
		}

		@Override
		public int read(char[] cbuf, int off, int len) {
			return -1;
		}

		@Override
		public void close() {
			// no-op
		}
	}
}
