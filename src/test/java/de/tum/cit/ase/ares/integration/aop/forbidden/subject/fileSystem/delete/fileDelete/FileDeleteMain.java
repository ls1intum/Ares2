package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete;

import java.io.File;
import java.nio.file.Path;

public final class FileDeleteMain {


    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";

    private FileDeleteMain() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): "
                        + "FileDeleteMain is a utility class and should not be instantiated.");
    }

    /** {@link java.io.File#delete()} */
    public static void accessFileSystemViaFileDelete() {
        new File(NOT_TRUSTED_FILE)
                .delete();
    }

    /** {@link java.io.File#deleteOnExit()} */
    public static void accessFileSystemViaFileDeleteOnExit() {
        new File(NOT_TRUSTED_FILE)
                .deleteOnExit();
    }

    /**
     * {@code Path → File → delete()} bridge:
     * {@link java.nio.file.Path#toFile()} → {@link java.io.File#delete()}.
     */
    public static void accessFileSystemViaPathToFileDelete() {
        Path path = Path.of(
                NOT_TRUSTED_FILE);
        path.toFile().delete();
    }
}
