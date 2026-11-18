package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.charsetStatic.formatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;

/**
 * Student code that writes a file with {@code new Formatter(String, Charset,
 * Locale)}, which a policy without any file rule refuses before the test runs.
 */
public final class FormatterCharsetMain {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private FormatterCharsetMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Writes the file.
	 *
	 * @param path the file to write
	 * @throws IOException if the file cannot be written
	 */
	public static void write(String path) throws IOException {
		try (Formatter formatter = new Formatter(path, StandardCharsets.UTF_8, Locale.ROOT)) {
			formatter.format("%s", "written");
		}
	}
}
