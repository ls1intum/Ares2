package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesReadString {

    private FilesReadString() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.readString(Path) and Files.readString(Path, Charset) for whole-file reading as String.
     * Test Case: Files.readString(Path[, Charset]) - Java 11+ shortcut to get the entire file as one String
     */
    public static void accessFileSystemViaFilesReadString() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        Files.readString(path); // Read whole file as String (default charset)
        Files.readString(path, StandardCharsets.UTF_8); // Read whole file as String (UTF-8)
    }
}
