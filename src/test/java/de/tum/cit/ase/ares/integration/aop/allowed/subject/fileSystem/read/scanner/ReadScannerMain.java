package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ReadScannerMain {

	private ReadScannerMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using {@link Scanner} for reading.
	 * 
	 * @return The content of the trusted file
	 */
	public static String accessFileSystemViaScanner() throws IOException {
		try (Scanner scanner = new Scanner(
				new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
			StringBuilder content = new StringBuilder();
			while (scanner.hasNextLine()) {
				content.append(scanner.nextLine());
				content.append(System.lineSeparator());
			}
			return content.toString();
		}
	}

	/**
	 * Reads the permitted file through Scanner(File, String), whose charset name is
	 * no path.
	 *
	 * @return The content of the trusted file
	 * @throws IOException if the file cannot be read
	 */
	public static String accessFileSystemViaScannerFileCharsetName() throws IOException {
		try (Scanner scanner = new Scanner(
				new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"), "UTF-8")) {
			return scanner.nextLine();
		}
	}

	/**
	 * Scans a text through Scanner(String), which opens no file at all.
	 *
	 * @return the sum of the two numbers in the text
	 */
	public static int scanText() {
		try (Scanner scanner = new Scanner("4 5")) {
			return scanner.nextInt() + scanner.nextInt();
		}
	}

	/**
	 * Reads a text through a StringReader, which opens no file at all.
	 *
	 * @return the first line of the text
	 * @throws IOException never, as the text is in memory
	 */
	public static String readText() throws IOException {
		try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader("4 5"))) {
			return reader.readLine();
		}
	}
}
