package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.lineTokenReader;

import java.io.*;

public class ScannerReader {

	private ScannerReader() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using Scanner for line or token-oriented reading. Test
	 * Case: Scanner - Token- or line-oriented reader (file or console)
	 */
	public static void accessFileSystemViaScanner() throws IOException {
		try (FileInputStream fis = new FileInputStream(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
				java.util.Scanner scanner = new java.util.Scanner(fis)) {
			if (scanner.hasNextLine()) {
				scanner.nextLine(); // Read a line
			}
			if (scanner.hasNext()) {
				scanner.next(); // Read a token
			}
		}
	}

	/**
	 * Reads the file through Scanner(File), so the Scanner opens it itself.
	 *
	 * @throws IOException if the file cannot be read
	 */
	public static void accessFileSystemViaScannerFile() throws IOException {
		try (java.util.Scanner scanner = new java.util.Scanner(
				new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
			scanner.hasNextLine();
		}
	}

	/**
	 * Reads the file through Scanner(File, String), whose charset name is no path.
	 *
	 * @throws IOException if the file cannot be read
	 */
	public static void accessFileSystemViaScannerFileCharsetName() throws IOException {
		try (java.util.Scanner scanner = new java.util.Scanner(
				new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
				"UTF-8")) {
			scanner.hasNextLine();
		}
	}

	/**
	 * Reads the file through Scanner(Path), so the Scanner opens it itself.
	 *
	 * @throws IOException if the file cannot be read
	 */
	public static void accessFileSystemViaScannerPath() throws IOException {
		try (java.util.Scanner scanner = new java.util.Scanner(java.nio.file.Path
				.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
			scanner.hasNextLine();
		}
	}
}
