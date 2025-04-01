package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem.filesDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
public final class Main {

    private Main() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): FileSystemAccessPenguin is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaFilesDelete() throws IOException {
        Files.delete(Path.of("pom123.xml"));
    }
}
