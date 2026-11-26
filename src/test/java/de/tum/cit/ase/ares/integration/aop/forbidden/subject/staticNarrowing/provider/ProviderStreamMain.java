package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.provider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Supervised code that opens a file through the file system provider directly.
 */
public final class ProviderStreamMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private ProviderStreamMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Reads the first byte of a file through the default provider's stream.
	 *
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int read() throws IOException {
		try (InputStream input = FileSystems.getDefault().provider().newInputStream(Path.of(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt"))) {
			return input.read();
		}
	}
}
