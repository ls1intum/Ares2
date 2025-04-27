package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.filesWrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WriteFilesWriteMain {

    private WriteFilesWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files} class for writing.
     */
    public static void accessFileSystemViaFilesWrite() throws IOException {
        byte[] content = "Hello, world!".getBytes();
        Files.write(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/output.txt"), content);
    }
}