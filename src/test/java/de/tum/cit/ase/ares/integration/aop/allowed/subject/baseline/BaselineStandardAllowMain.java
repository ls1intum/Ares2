package de.tum.cit.ase.ares.integration.aop.allowed.subject.baseline;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

/**
 * Student code using what the secure baseline allows without any policy entry:
 * random numbers, UUIDs, the system timezone, locale and charset data, and
 * temporary files in the default temp directory.
 */
public final class BaselineStandardAllowMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private BaselineStandardAllowMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): BaselineStandardAllowMain is a utility class and should not be instantiated.");
	}

	/**
	 * Creates a temporary file with {@link File#createTempFile(String, String)}.
	 *
	 * @return the created file
	 * @throws IOException if the file cannot be created
	 */
	public static File createTempFileWithFile() throws IOException {
		return File.createTempFile("ares-baseline-", ".tmp");
	}

	/**
	 * Creates a temporary file with
	 * {@link Files#createTempFile(String, String, java.nio.file.attribute.FileAttribute...)}.
	 *
	 * @return the created file
	 * @throws IOException if the file cannot be created
	 */
	public static Path createTempFileWithFiles() throws IOException {
		return Files.createTempFile("ares-baseline-", ".tmp");
	}

	/**
	 * Draws a random number and a seed from {@link SecureRandom}.
	 *
	 * @return the seed length, so the work cannot be optimised away
	 */
	public static int drawSecureRandomNumbers() {
		SecureRandom random = new SecureRandom();
		return random.nextInt(10) + random.generateSeed(8).length;
	}

	/**
	 * Creates a random UUID.
	 *
	 * @return the UUID
	 */
	public static UUID createRandomUuid() {
		return UUID.randomUUID();
	}

	/**
	 * Reads the system timezone and the current time in it.
	 *
	 * @return the current time in the system timezone
	 */
	public static ZonedDateTime readSystemTimezone() {
		return ZonedDateTime.now(ZoneId.systemDefault());
	}

	/**
	 * Formats a number and a date for Germany and encodes text in a legacy charset.
	 *
	 * @return the formatted text
	 */
	public static String formatWithLocaleAndCharsetData() {
		String number = NumberFormat.getInstance(Locale.GERMANY).format(1234.5);
		String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.GERMANY)
				.format(LocalDate.of(2026, 10, 8));
		byte[] encoded = Charset.forName("windows-1252").encode(number + " " + date).array();
		return number + " " + date + " " + encoded.length;
	}
}
