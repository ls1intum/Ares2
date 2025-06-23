package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.filterOutputStream;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

public class FilterOutputStreamWriteMain {

    private FilterOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FilterOutputStream} directly for writing.
     */
    public static void accessFileSystemViaFilterOutputStream() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            fos.write(100);
        }
    }

    public static void accessFileSystemViaFilterOutputStreamWithData() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data);
        }
    }

    public static void accessFileSystemViaFilterOutputStreamWithDataAndOffset() throws IOException {
        try (FilterOutputStream fos = new FilterOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            fos.write(data, 0, data.length);
        }
    }
}
