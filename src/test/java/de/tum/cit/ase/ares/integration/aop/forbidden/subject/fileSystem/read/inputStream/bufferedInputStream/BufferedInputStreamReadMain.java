package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.bufferedInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class BufferedInputStreamReadMain {

    private BufferedInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaBufferedInputStreamRead() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaBufferedInputStreamReadByteArray() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaBufferedInputStreamReadAllBytes() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaBufferedInputStreamReadNBytes() throws IOException {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}
