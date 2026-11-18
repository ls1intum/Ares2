package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.charsetStatic.scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Student code that reads a file with {@code new Scanner(Path)}, which a policy
 * without any file rule refuses before the test runs.
 */
public final class ScannerPathMain {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private ScannerPathMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Reads the first token of the file.
	 *
	 * @param path the file to read
	 * @return the first token
	 * @throws IOException if the file cannot be read
	 */
	public static String read(String path) throws IOException {
		try (Scanner scanner = new Scanner(Path.of(path))) {
			return scanner.next();
		}
	}
}
