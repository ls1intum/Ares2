package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.filesRead;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReadFilesReadMain {

    private ReadFilesReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for reading.
     * @return the content of the trusted file as a String
     */
    public static String accessFileSystemViaFilesRead() throws IOException {
        byte[] content = Files.readAllBytes(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"));
        return new String(content);
    }
}