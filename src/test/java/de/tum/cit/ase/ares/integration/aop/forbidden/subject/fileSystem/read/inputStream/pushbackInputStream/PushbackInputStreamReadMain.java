package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.pushbackInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class PushbackInputStreamReadMain {

    private PushbackInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaPushbackInputStreamRead() throws IOException {
        try (PushbackInputStream reader = new PushbackInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaPushbackInputStreamReadByteArray() throws IOException {
        try (PushbackInputStream reader = new PushbackInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaPushbackInputStreamReadByteArrayOffsetLength() throws IOException {
        try (PushbackInputStream reader = new PushbackInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaPushbackInputStreamReadAllBytes() throws IOException {
        try (PushbackInputStream reader = new PushbackInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaPushbackInputStreamReadNBytes() throws IOException {
        try (PushbackInputStream reader = new PushbackInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}

