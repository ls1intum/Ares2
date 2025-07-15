package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesNewBufferedReader {

    private FilesNewBufferedReader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.newBufferedReader(Path, Charset) for character reading.
     * Test Case: Files.newBufferedReader(Path, Charset) - Open a BufferedReader directly from a Path
     */
    public static void accessFileSystemViaFilesNewBufferedReader() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine(); // Read a line from the file
        }
    }
}
