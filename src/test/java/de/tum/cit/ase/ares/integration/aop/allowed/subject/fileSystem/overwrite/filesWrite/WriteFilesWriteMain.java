package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.filesWrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteFilesWriteMain {

    private WriteFilesWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaFilesWrite(String text) throws IOException {
        byte[] content = text.getBytes();
        Files.write(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"), content);
    }
}