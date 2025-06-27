package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete;

import java.io.File;
import java.nio.file.Path;

public final class FileDeleteMain {

    private FileDeleteMain() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): "
                        + "FileDeleteMain is a utility class and should not be instantiated.");
    }

    /** {@link java.io.File#delete()} */
    public static void accessFileSystemViaFileDelete() {
        new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")
                .delete();
    }

    /** {@link java.io.File#deleteOnExit()} */
    public static void accessFileSystemViaFileDeleteOnExit() {
        new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")
                .deleteOnExit();
    }

    /**
     * {@code Path → File → delete()} bridge:
     * {@link java.nio.file.Path#toFile()} → {@link java.io.File#delete()}.
     */
    public static void accessFileSystemViaPathToFileDelete() {
        Path path = Path.of(
                "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        path.toFile().delete();
    }
}
