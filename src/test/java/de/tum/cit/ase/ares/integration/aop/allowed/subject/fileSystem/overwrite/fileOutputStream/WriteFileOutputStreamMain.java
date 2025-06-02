package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFileOutputStreamMain {

    private WriteFileOutputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileOutputStream} directly for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaFileOutputStream(String text) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            byte[] data = text.getBytes();
            fos.write(data);
        }
    }
}