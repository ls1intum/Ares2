package de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** An archive with observable overrides for intercepted entry calls. */
public final class StudentZipFile extends ZipFile {

	/** Whether an entry override ran. */
	private boolean entryOverrideCalled;

	/** Whether the size override ran. */
	private boolean sizeOverrideCalled;

	/**
	 * Opens the test archive before a policy is armed.
	 *
	 * @param path the archive path
	 * @throws IOException if the archive cannot be opened
	 */
	public StudentZipFile(Path path) throws IOException {
		super(path.toFile());
	}

	/**
	 * Records an attempted entry enumeration.
	 *
	 * @return the archive entries
	 */
	@Override
	public Enumeration<? extends ZipEntry> entries() {
		entryOverrideCalled = true;
		return super.entries();
	}

	/**
	 * Records an attempted entry stream read.
	 *
	 * @param entry the archive entry
	 * @return the entry stream
	 * @throws IOException if the entry cannot be read
	 */
	@Override
	public InputStream getInputStream(ZipEntry entry) throws IOException {
		entryOverrideCalled = true;
		return super.getInputStream(entry);
	}

	/**
	 * Records inspection of the archive while advice is active.
	 *
	 * @return the number of entries
	 */
	@Override
	public int size() {
		sizeOverrideCalled = true;
		return super.size();
	}

	/**
	 * Reports whether an entry override ran.
	 *
	 * @return whether an entry override ran
	 */
	public boolean entryOverrideCalled() {
		return entryOverrideCalled;
	}

	/**
	 * Reports whether the size override ran.
	 *
	 * @return whether the size override ran
	 */
	public boolean sizeOverrideCalled() {
		return sizeOverrideCalled;
	}

	/** Clears observations before the next mode runs. */
	public void clearObservations() {
		entryOverrideCalled = false;
		sizeOverrideCalled = false;
	}
}
