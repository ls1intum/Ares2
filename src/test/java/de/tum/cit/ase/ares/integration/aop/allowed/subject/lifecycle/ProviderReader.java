package de.tum.cit.ase.ares.integration.aop.allowed.subject.lifecycle;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code whose constructor reads a file, built where JUnit builds the
 * arguments of a parameterised test, before the test method runs.
 */
public final class ProviderReader {

	/**
	 * What the read in the constructor gave: {@code read}, {@code refused} or
	 * {@code failed}.
	 */
	private final String outcome;

	/**
	 * Reads the given file once.
	 *
	 * @param file the file to read
	 */
	public ProviderReader(String file) {
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
	 * Says what the read in the constructor gave.
	 *
	 * @return {@code read}, {@code refused} or {@code failed}
	 */
	public String outcome() {
		return outcome;
	}

	/** Names the outcome, for the display name of the test. */
	@Override
	public String toString() {
		return outcome;
	}
}
