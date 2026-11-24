package de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code that reads a file when it is closed, handed to a parameterised
 * test as an argument JUnit closes after the invocation.
 */
public final class ClosingReader implements AutoCloseable {

	/**
	 * The file read on closing.
	 */
	private final String file;

	/**
	 * What the read on closing gave: {@code not closed}, {@code read},
	 * {@code refused} or {@code failed}.
	 */
	private volatile String outcome = "not closed";

	/**
	 * Builds a reader for the given file, reading nothing yet.
	 *
	 * @param file the file to read on closing
	 */
	public ClosingReader(String file) {
		this.file = file;
	}

	/**
	 * Reads the file once.
	 */
	@Override
	public void close() {
		String result;
		try {
			Files.readAllBytes(Path.of(file));
			result = "read";
		} catch (SecurityException refusal) {
			result = "refused";
		} catch (Exception failure) {
			result = "failed";
		}
		outcome = result;
	}

	/**
	 * Says what the read on closing gave.
	 *
	 * @return {@code not closed}, {@code read}, {@code refused} or {@code failed}
	 */
	public String outcome() {
		return outcome;
	}

	/** Names the file, for the display name of the test. */
	@Override
	public String toString() {
		return file;
	}
}
