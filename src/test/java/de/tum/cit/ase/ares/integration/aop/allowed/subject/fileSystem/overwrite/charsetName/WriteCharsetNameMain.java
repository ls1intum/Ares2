package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.charsetName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;

/**
 * Student code that names a character set by its name, such as "UTF-8", next to
 * a permitted file or an in-memory stream. Ares must check only the file or
 * stream, never the character set name as if it were a path.
 */
public final class WriteCharsetNameMain {

	/**
	 * The file the overwrite policy permits.
	 */
	private static final String TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";

	/**
	 * The character set name passed as a String.
	 */
	private static final String CHARSET_NAME = "UTF-8";

	/**
	 * Prevents instantiation of this utility class.
	 */
	private WriteCharsetNameMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Writes the text with {@code new Formatter(File, String, Locale)}.
	 *
	 * @param text the text to write to the permitted file
	 * @throws IOException if the file cannot be written
	 */
	public static void formatterWithFileAndCharsetName(String text) throws IOException {
		try (Formatter formatter = new Formatter(new File(TRUSTED_FILE), CHARSET_NAME, Locale.ROOT)) {
			formatter.format("%s", text);
		}
	}

	/**
	 * Writes the text with {@code new Formatter(String, String)}.
	 *
	 * @param text the text to write to the permitted file
	 * @throws IOException if the file cannot be written
	 */
	public static void formatterWithFileNameAndCharsetName(String text) throws IOException {
		try (Formatter formatter = new Formatter(TRUSTED_FILE, CHARSET_NAME)) {
			formatter.format("%s", text);
		}
	}

	/**
	 * Writes the text with {@code new Formatter(File, Charset, Locale)}, which
	 * names no character set as a String and so was never affected.
	 *
	 * @param text the text to write to the permitted file
	 * @throws IOException if the file cannot be written
	 */
	public static void formatterWithFileAndCharset(String text) throws IOException {
		try (Formatter formatter = new Formatter(new File(TRUSTED_FILE), StandardCharsets.UTF_8, Locale.ROOT)) {
			formatter.format("%s", text);
		}
	}

	/**
	 * Formats into memory with {@code new Formatter(OutputStream, String)}.
	 *
	 * @throws IOException if the character set is unknown
	 */
	public static void formatterWithStreamAndCharsetName() throws IOException {
		try (Formatter formatter = new Formatter(new ByteArrayOutputStream(), CHARSET_NAME)) {
			formatter.format("%s", "in memory");
		}
	}

	/**
	 * Writes into memory with {@code new OutputStreamWriter(OutputStream, String)}.
	 *
	 * @throws IOException if the character set is unknown
	 */
	public static void outputStreamWriterWithCharsetName() throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream(), CHARSET_NAME)) {
			writer.write("in memory");
		}
	}

	/**
	 * Writes the text with {@code new PrintWriter(File, String)}.
	 *
	 * @param text the text to write to the permitted file
	 * @throws IOException if the file cannot be written
	 */
	public static void printWriterWithFileAndCharsetName(String text) throws IOException {
		try (PrintWriter writer = new PrintWriter(new File(TRUSTED_FILE), CHARSET_NAME)) {
			writer.print(text);
		}
	}

	/**
	 * Reads from memory with {@code new InputStreamReader(InputStream, String)}.
	 *
	 * @return the first character read
	 * @throws IOException if the character set is unknown
	 */
	public static int inputStreamReaderWithCharsetName() throws IOException {
		try (InputStreamReader reader = new InputStreamReader(
				new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)), CHARSET_NAME)) {
			return reader.read();
		}
	}

	/**
	 * Reads from memory with {@code new Scanner(InputStream, String)}.
	 *
	 * @return the first token read
	 */
	public static String scannerWithCharsetName() {
		try (Scanner scanner = new Scanner(new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)),
				CHARSET_NAME)) {
			return scanner.next();
		}
	}

	/**
	 * Formats into memory with {@code new Formatter()}, which takes no argument.
	 *
	 * @return the formatted text
	 */
	public static String formatterWithoutArguments() {
		try (Formatter formatter = new Formatter()) {
			return formatter.format("%s", "x").toString();
		}
	}

	/**
	 * Formats into memory with {@code new Formatter(Appendable)}.
	 *
	 * @return the formatted text
	 */
	public static String formatterWithStringBuilder() {
		try (Formatter formatter = new Formatter(new StringBuilder())) {
			return formatter.format("%s", "x").toString();
		}
	}
}
