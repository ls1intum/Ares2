package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete;

import java.io.File;
import java.nio.file.Path;

public final class FileDeleteMain {

    private static final String TRUSTED_FILE =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/trusteddir/trusted.txt";

    private FileDeleteMain() {
        throw new SecurityException("utility class");
    }

    /** java.io.File#delete() */
    public static void accessFileSystemViaFileDelete() {
        new File(TRUSTED_FILE).delete();
    }

    /** java.io.File#deleteOnExit() */
    public static void accessFileSystemViaFileDeleteOnExit() {
        new File(TRUSTED_FILE).deleteOnExit();
    }

    /** Path → File → delete() bridge */
    public static void accessFileSystemViaPathToFileDelete() {
        Path.of(TRUSTED_FILE).toFile().delete();
    }
}
