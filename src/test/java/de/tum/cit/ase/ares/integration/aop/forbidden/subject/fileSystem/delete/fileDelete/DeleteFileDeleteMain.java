package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete;

import java.io.File;

public class DeleteFileDeleteMain {

    private DeleteFileDeleteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link File} class for deletion.
     */
    public static void accessFileSystemViaFileDelete() {
        new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt").delete();
    }
}
