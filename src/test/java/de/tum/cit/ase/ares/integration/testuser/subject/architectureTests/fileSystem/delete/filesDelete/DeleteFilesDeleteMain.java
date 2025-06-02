package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
public final class DeleteFilesDeleteMain {

    private DeleteFilesDeleteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for deletion.
     */
    public static void accessFileSystemViaFilesDelete() throws IOException {
        Files.delete(Path.of("pom123.xml"));
    }
}
