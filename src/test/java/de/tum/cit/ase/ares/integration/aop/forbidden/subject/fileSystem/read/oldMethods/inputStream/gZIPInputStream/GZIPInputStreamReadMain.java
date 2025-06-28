package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.gZIPInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GZIPInputStreamReadMain {

    private GZIPInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaGZIPInputStreamRead() throws IOException {
        try (GZIPInputStream reader = new GZIPInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaGZIPInputStreamReadByteArray() throws IOException {
        try (GZIPInputStream reader = new GZIPInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength() throws IOException {
        try (GZIPInputStream reader = new GZIPInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaGZIPInputStreamReadAllBytes() throws IOException {
        try (GZIPInputStream reader = new GZIPInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaGZIPInputStreamReadNBytes() throws IOException {
        try (GZIPInputStream reader = new GZIPInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}

