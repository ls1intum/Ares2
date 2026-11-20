package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.untrustedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Student code that opens a permitted file through its own {@link File}
 * subclass, a type Ares refuses to inspect because reading it would run student
 * code.
 */
public final class UntrustedFileReadMain {

	/** The file the policies permit reading. */
	private static final String PERMITTED = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";

	/** Not to be created. */
	private UntrustedFileReadMain() {
	}

	/**
	 * Opens the permitted file through a student {@link File} subclass.
	 *
	 * @throws IOException if the file cannot be opened
	 */
	public static void readThroughAStudentFile() throws IOException {
		try (FileInputStream stream = new FileInputStream(new StudentFile(PERMITTED))) {
			stream.read();
		}
	}

	/** A {@link File} subclass written by the student. */
	private static final class StudentFile extends File {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates one for a path.
		 *
		 * @param path the path.
		 */
		StudentFile(String path) {
			super(path);
		}
	}
}
