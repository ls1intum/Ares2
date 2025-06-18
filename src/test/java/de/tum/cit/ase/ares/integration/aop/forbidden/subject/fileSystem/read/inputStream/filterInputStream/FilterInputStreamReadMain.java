package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.filterInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public class FilterInputStreamReadMain {

    private FilterInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFilterInputStreamRead() throws IOException {
        try (FilterInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFilterInputStreamReadByteArray() throws IOException {
        try (FilterInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FilterInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaFilterInputStreamReadAllBytes() throws IOException {
        try (FilterInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaFilterInputStreamReadNBytes() throws IOException {
        try (FilterInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}
