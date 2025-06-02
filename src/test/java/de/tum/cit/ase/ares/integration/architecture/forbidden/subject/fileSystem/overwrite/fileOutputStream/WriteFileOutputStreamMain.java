package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.overwrite.fileOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFileOutputStreamMain {

    private WriteFileOutputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileOutputStream} directly for writing.
     */
    public static void accessFileSystemViaFileOutputStream() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data);
        }
    }
}