package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.overwrite.fileWrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFileWriteMain {

    private WriteFileWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link File} class for writing.
     */
    public static void accessFileSystemViaFileWrite() throws IOException {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data);
        }
    }
}