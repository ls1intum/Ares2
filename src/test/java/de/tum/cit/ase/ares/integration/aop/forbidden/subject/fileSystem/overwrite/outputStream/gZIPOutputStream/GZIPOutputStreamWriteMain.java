package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.gZIPOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamWriteMain {

    private GZIPOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link GZIPOutputStream} directly for writing.
     */
    public static void accessFileSystemViaGZIPOutputStream() throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt.gz"))) {
            gos.write(100);
        }
    }

    public static void accessFileSystemViaGZIPOutputStreamWithData() throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt.gz"))) {
            byte[] data = "Hello, world!".getBytes();
            gos.write(data);
        }
    }

    public static void accessFileSystemViaGZIPOutputStreamWithDataAndOffset() throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt.gz"))) {
            byte[] data = "Hello, world!".getBytes();
            gos.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaGZIPOutputStreamWithCustomBuffer() throws IOException {
        // Create with custom buffer size
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt.gz"), 8192)) {
            byte[] data = "Hello, world!".getBytes();
            gos.write(data);
            gos.finish(); // Finish the compressed output
        }
    }
}
