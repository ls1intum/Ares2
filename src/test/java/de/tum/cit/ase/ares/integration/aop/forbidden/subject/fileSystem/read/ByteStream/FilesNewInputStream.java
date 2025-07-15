package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesNewInputStream {

    private FilesNewInputStream() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.newInputStream(Path) for low-level byte stream reading.
     * Test Case: Files.newInputStream(Path) - Low-level byte stream (respects OpenOptions)
     */
    public static void accessFileSystemViaFilesNewInputStream() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (InputStream is = Files.newInputStream(path)) {
            is.read(); // Read a single byte
        }
    }
}
