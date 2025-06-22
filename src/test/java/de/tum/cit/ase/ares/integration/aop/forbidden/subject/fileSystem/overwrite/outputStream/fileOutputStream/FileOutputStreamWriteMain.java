package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.fileOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputStreamWriteMain {

    private FileOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileOutputStream} directly for writing.
     */
    public static void accessFileSystemViaFileOutputStream() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            fos.write(100);
        }
    }

    public static void accessFileSystemViaFileOutputStreamWithData() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data);
        }
    }

    public static void accessFileSystemViaFileOutputStreamWithDataAndOffset() throws IOException {
        try (FileOutputStream fos = new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data, 0, data.length);
        }
    }
}