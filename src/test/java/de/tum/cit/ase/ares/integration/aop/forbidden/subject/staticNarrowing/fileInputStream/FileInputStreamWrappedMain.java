package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.fileInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Supervised code that loads properties from a file through a buffered stream.
 */
public final class FileInputStreamWrappedMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private FileInputStreamWrappedMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Loads properties from a file through a buffered file stream.
	 *
	 * @return the properties read
	 * @throws IOException if reading fails
	 */
	public static Properties read() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = new BufferedInputStream(new FileInputStream(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt"))) {
			properties.load(input);
		}
		return properties;
	}
}
