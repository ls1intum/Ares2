package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.filterOutputStream;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

public class FilterOutputStreamWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private FilterOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FilterOutputStream} directly for writing.
     */
    public static void accessFileSystemViaFilterOutputStream() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            fos.write(100);
        }
    }

    public static void accessFileSystemViaFilterOutputStreamWithData() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            fos.write("Hello, world!".getBytes());
        }
    }

    public static void accessFileSystemViaFilterOutputStreamWithDataAndOffset() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream(NOT_TRUSTED_FILE))) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data, 0, data.length);
        }
    }
}
