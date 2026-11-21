package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.inspect;

import java.io.File;

/**
 * A student subclass of {@link File}. Ares must not trust it to say which path
 * it stands for.
 */
final class StudentFile extends File {

	/**
	 * Keeps this class serialisable in the same way as {@link File}.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the file for the given path.
	 *
	 * @param path the path
	 */
	StudentFile(String path) {
		super(path);
	}
}
