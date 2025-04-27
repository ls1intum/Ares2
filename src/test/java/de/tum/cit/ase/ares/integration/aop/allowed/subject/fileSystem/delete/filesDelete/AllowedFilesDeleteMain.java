package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AllowedFilesDeleteMain {

    private AllowedFilesDeleteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files for deletion of trusted.txt.
     */
    public static void accessFileSystemViaFilesDelete() throws IOException {
        Files.delete(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")
        );
    }
}
