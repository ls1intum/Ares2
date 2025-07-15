package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FilesDeleteMain {

    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";


    private FilesDeleteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): FilesDeleteMain is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for deletion.
     */
    public static void accessFileSystemViaFilesDelete() throws IOException {
        Files.delete(Path.of(NOT_TRUSTED_FILE));
    }

    public static void accessFileSystemViaFilesDeleteIfExists() throws IOException {
        Files.deleteIfExists(Path.of(NOT_TRUSTED_FILE));
    }

}
