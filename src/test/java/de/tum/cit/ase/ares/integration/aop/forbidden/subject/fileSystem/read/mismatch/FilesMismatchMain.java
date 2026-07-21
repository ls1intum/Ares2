package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.mismatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesMismatchMain {

	private FilesMismatchMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using Files.mismatch(Path, Path), reading both files
	 * fully to find the first differing byte.
	 * <p>
	 * Reproduces I-113: {@code Files.mismatch} was completely absent from all four
	 * monitoring layers (AspectJ pointcut, instrumentation method map, and both
	 * ArchUnit/WALA static inventories), so this call previously succeeded
	 * unmonitored under a zero-file-access policy.
	 */
	public static void accessFileSystemViaFilesMismatch() throws IOException {
		Path first = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
		Path second = Paths.get(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt");
		Files.mismatch(first, second);
	}
}
