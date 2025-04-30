package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

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
        var x = 0;
        Files.delete(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }
}
