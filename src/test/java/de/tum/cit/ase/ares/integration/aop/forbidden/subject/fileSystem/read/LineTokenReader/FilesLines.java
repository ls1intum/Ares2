package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.LineTokenReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FilesLines {

    private FilesLines() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.lines(Path) for lazy line streaming.
     * Test Case: Files.lines(Path) - Stream the file lazily as a Stream<String>
     */
    public static void accessFileSystemViaFilesLines() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.findFirst(); // Lazily stream the file as lines
        }
    }
}
