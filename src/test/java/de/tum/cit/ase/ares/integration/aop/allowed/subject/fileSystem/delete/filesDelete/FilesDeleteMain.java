package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FilesDeleteMain {

    private static final Path TRUSTED_FILE = Path.of(
            "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/trusteddir/trusted.txt");

    private FilesDeleteMain() { throw new SecurityException("utility"); }

    public static void accessFileSystemViaFilesDelete() throws IOException {
        Files.delete(TRUSTED_FILE);
    }

    public static void accessFileSystemViaFilesDeleteIfExists() throws IOException {
        Files.deleteIfExists(TRUSTED_FILE);
    }
}
