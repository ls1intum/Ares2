package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.inflaterOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class InflaterOutputStreamWriteMain {

    private InflaterOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link InflaterOutputStream} directly for writing.
     */
    public static void accessFileSystemViaInflaterOutputStream() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            // Note: InflaterOutputStream is typically used for decompression, so this is for demonstration purposes
            ios.write(100);
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithData() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            ios.write(data);
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithDataAndOffset() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            ios.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithCustomInflater() throws IOException {
        Inflater inflater = new Inflater(true);
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), inflater)) {
            byte[] data = "Hello, world!".getBytes();
            ios.write(data);
            ios.finish(); // Finishes writing decompressed data
        }
    }
}
