package de.tum.cit.ase.ares.integration.aop.allowed.subject.baselineArchive;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/** Student call sites for entries in already-open JDK archives. */
public final class AllowedArchiveEntryCalls {

	/** Prevents instantiation of this call-site holder. */
	private AllowedArchiveEntryCalls() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reads both archive types through JDK-typed receivers.
	 *
	 * @param zip an already-open zip
	 * @param jar an already-open jar
	 * @return the number of nonempty enumerations and entry streams
	 * @throws IOException if an entry cannot be read
	 */
	public static int read(ZipFile zip, JarFile jar) throws IOException {
		int count = zip.entries().hasMoreElements() ? 1 : 0;
		count += jar.entries().hasMoreElements() ? 1 : 0;
		try (InputStream zipStream = zip.getInputStream(zip.getEntry("entry.txt"));
				InputStream jarStream = jar.getInputStream(jar.getEntry("entry.txt"))) {
			return count + (zipStream == null ? 0 : 1) + (jarStream == null ? 0 : 1);
		}
	}
}
