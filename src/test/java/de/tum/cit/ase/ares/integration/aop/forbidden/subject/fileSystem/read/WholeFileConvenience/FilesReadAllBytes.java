package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesReadAllBytes {

    private FilesReadAllBytes() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.readAllBytes(Path) for whole-file reading.
     * Test Case: Files.readAllBytes(Path) - Read whole file into a byte[]
     */
    public static void accessFileSystemViaFilesReadAllBytes() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        Files.readAllBytes(path); // Read whole file into a byte[]
    }
}
