package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesReadAllLines {

    private FilesReadAllLines() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.readAllLines(Path) for whole-file reading as lines.
     * Test Case: Files.readAllLines(Path) - Load all lines into List<String>
     */
    public static void accessFileSystemViaFilesReadAllLines() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        Files.readAllLines(path); // Load all lines into List<String>
    }
}
