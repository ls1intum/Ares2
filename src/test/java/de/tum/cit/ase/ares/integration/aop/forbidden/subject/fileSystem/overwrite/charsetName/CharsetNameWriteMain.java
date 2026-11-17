package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.charsetName;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Locale;

/**
 * Student code that names a character set by its name next to a file the policy
 * does not permit. Ares must still check the file and refuse the call.
 */
public final class CharsetNameWriteMain {

	/**
	 * A file the overwrite policy does not permit.
	 */
	public static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt";

	/**
	 * The character set name passed as a String.
	 */
	private static final String CHARSET_NAME = "UTF-8";

	/**
	 * Prevents instantiation of this utility class.
	 */
	private CharsetNameWriteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Writes with {@code new Formatter(File, String, Locale)}.
	 *
	 * @throws IOException if the file cannot be written
	 */
	public static void formatterWithFileAndCharsetName() throws IOException {
		try (Formatter formatter = new Formatter(new File(NOT_TRUSTED_FILE), CHARSET_NAME, Locale.ROOT)) {
			formatter.format("%s", "overwritten");
		}
	}

	/**
	 * Writes with {@code new Formatter(String, String)}.
	 *
	 * @throws IOException if the file cannot be written
	 */
	public static void formatterWithFileNameAndCharsetName() throws IOException {
		try (Formatter formatter = new Formatter(NOT_TRUSTED_FILE, CHARSET_NAME)) {
			formatter.format("%s", "overwritten");
		}
	}

	/**
	 * Writes with {@code new PrintWriter(File, String)}.
	 *
	 * @throws IOException if the file cannot be written
	 */
	public static void printWriterWithFileAndCharsetName() throws IOException {
		try (PrintWriter writer = new PrintWriter(new File(NOT_TRUSTED_FILE), CHARSET_NAME)) {
			writer.print("overwritten");
		}
	}
}
