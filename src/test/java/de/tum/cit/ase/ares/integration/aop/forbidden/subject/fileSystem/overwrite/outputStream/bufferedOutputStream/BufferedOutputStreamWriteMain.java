package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.bufferedOutputStream;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferedOutputStreamWriteMain {

    private BufferedOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link BufferedOutputStream} directly for writing.
     */
    public static void accessFileSystemViaBufferedOutputStream() throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            bos.write(100);
        }
    }

    public static void accessFileSystemViaBufferedOutputStreamWithData() throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            bos.write(data);
        }
    }

    public static void accessFileSystemViaBufferedOutputStreamWithDataAndOffset() throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            bos.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaBufferedOutputStreamWithFlush() throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            bos.write(data);
            bos.flush(); // Explicitly flush the buffer
        }
    }
}
