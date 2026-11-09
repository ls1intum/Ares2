package de.tum.cit.ase.ares.integration.aop.forbidden.subject.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/** A jar with observable overrides for intercepted entry calls. */
public final class StudentJarFile extends JarFile {

	/** Whether an entry override ran. */
	private boolean entryOverrideCalled;

	/** Whether the size override ran. */
	private boolean sizeOverrideCalled;

	/**
	 * Opens the test jar before a policy is armed.
	 *
	 * @param path the jar path
	 * @throws IOException if the jar cannot be opened
	 */
	public StudentJarFile(Path path) throws IOException {
		super(path.toFile());
	}

	/**
	 * Records an attempted entry enumeration.
	 *
	 * @return the jar entries
	 */
	@Override
	public Enumeration<JarEntry> entries() {
		entryOverrideCalled = true;
		return super.entries();
	}

	/**
	 * Records an attempted entry stream read.
	 *
	 * @param entry the jar entry
	 * @return the entry stream
	 * @throws IOException if the entry cannot be read
	 */
	@Override
	public InputStream getInputStream(ZipEntry entry) throws IOException {
		entryOverrideCalled = true;
		return super.getInputStream(entry);
	}

	/**
	 * Records inspection of the jar while advice is active.
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
