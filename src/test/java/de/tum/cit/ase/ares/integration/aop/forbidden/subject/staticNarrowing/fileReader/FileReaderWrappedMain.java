package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.fileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Supervised code that reads a file through a buffered reader.
 */
public final class FileReaderWrappedMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private FileReaderWrappedMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Reads the first character of a file through a buffered file reader.
	 *
	 * @return the character
	 * @throws IOException if reading fails
	 */
	public static int read() throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt"))) {
			return reader.read();
		}
	}
}
