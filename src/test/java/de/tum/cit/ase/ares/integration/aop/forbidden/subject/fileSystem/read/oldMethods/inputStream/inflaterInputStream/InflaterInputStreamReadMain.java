package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.inflaterInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

public class InflaterInputStreamReadMain {

    private InflaterInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaInflaterInputStreamRead() throws IOException {
        try (InflaterInputStream reader = new InflaterInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaInflaterInputStreamReadByteArray() throws IOException {
        try (InflaterInputStream reader = new InflaterInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength() throws IOException {
        try (InflaterInputStream reader = new InflaterInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaInflaterInputStreamReadAllBytes() throws IOException {
        try (InflaterInputStream reader = new InflaterInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaInflaterInputStreamReadNBytes() throws IOException {
        try (InflaterInputStream reader = new InflaterInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}

