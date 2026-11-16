package de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Student call sites that use the JDK archive type. */
public final class ArchiveEntryCalls {

	/** Prevents instantiation of this call-site holder. */
	private ArchiveEntryCalls() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Enumerates entries through a JDK-typed receiver.
	 *
	 * @param archive an already-open archive
	 */
	public static void entries(ZipFile archive) {
		archive.entries();
	}

	/**
	 * Reads an entry through a JDK-typed receiver.
	 *
	 * @param archive an already-open archive
	 * @param entry   an entry in the archive
	 * @throws IOException if the entry cannot be read
	 */
	public static void stream(ZipFile archive, ZipEntry entry) throws IOException {
		archive.getInputStream(entry);
	}

	/**
	 * Enumerates entries through a JDK-typed jar receiver.
	 *
	 * @param archive an already-open jar
	 */
	public static void entries(JarFile archive) {
		archive.entries();
	}

	/**
	 * Reads an entry through a JDK-typed jar receiver.
	 *
	 * @param archive an already-open jar
	 * @param entry   an entry in the jar
	 * @throws IOException if the entry cannot be read
	 */
	public static void stream(JarFile archive, ZipEntry entry) throws IOException {
		archive.getInputStream(entry);
	}
}
