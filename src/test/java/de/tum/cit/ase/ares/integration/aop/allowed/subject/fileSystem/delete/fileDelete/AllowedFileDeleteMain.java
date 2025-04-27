package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete;

import java.io.File;

public class AllowedFileDeleteMain {

    private AllowedFileDeleteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using File for deletion of trusted.txt.
     */
    public static void accessFileSystemViaFileDelete() {
        new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")
                .delete();
    }
}
