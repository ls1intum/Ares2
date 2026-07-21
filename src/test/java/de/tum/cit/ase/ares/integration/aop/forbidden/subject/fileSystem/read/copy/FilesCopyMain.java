package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.copy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FilesCopyMain {

	private FilesCopyMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using
	 * {@code Files.copy(source, destination, REPLACE_EXISTING)}.
	 * <p>
	 * Reproduces I-114's real privilege-escalation consequence: under the
	 * accompanying policy, the source path is OVERWRITE-permitted but explicitly
	 * NOT read-permitted, while the destination directory is fully create/overwrite
	 * permitted. Before the fix, the whole call was checked as one uniform
	 * "overwrite" action, so the source being overwrite-allowed also (wrongly)
	 * allowed reading it. Now the source must independently satisfy the READ
	 * allow-list, which it does not, so this must be denied.
	 */
	public static void accessFileSystemViaFilesCopy() throws IOException {
		Path source = Paths.get(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/copy/copySource.txt");
		Path destination = Paths.get(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/copy/copyDestination.txt");
		Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
	}
}
