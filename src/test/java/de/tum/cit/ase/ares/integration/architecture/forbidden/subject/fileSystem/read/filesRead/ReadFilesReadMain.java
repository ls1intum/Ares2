package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.filesRead;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReadFilesReadMain {

    private ReadFilesReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for reading.
     */
    public static void accessFileSystemViaFilesRead() throws IOException {
        byte[] content = Files.readAllBytes(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }
}