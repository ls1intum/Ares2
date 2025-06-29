package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.inflaterOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class InflaterOutputStreamWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private InflaterOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link InflaterOutputStream} directly for writing.
     * (InflaterOutputStream is normally for decompression—used here only to reach the overwrite sink).
     */
    public static void accessFileSystemViaInflaterOutputStream() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            ios.write(100);
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithData() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            ios.write("Hello, world!".getBytes());
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithDataAndOffset() throws IOException {
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            byte[] data = "Hello, world!".getBytes();
            ios.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaInflaterOutputStreamWithCustomInflater() throws IOException {
        Inflater inflater = new Inflater(true);
        try (InflaterOutputStream ios = new InflaterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE), inflater)) {
            ios.write("Hello, world!".getBytes());
            ios.finish();
        }
    }
}
