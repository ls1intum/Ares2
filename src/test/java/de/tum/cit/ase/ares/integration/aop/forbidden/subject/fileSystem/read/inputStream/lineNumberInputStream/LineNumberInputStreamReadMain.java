package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.lineNumberInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.LineNumberInputStream;

@SuppressWarnings("deprecation")
public class LineNumberInputStreamReadMain {

    private LineNumberInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaLineNumberInputStreamRead() throws IOException {
        try (LineNumberInputStream reader = new LineNumberInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaLineNumberInputStreamReadByteArray() throws IOException {
        try (LineNumberInputStream reader = new LineNumberInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength() throws IOException {
        try (LineNumberInputStream reader = new LineNumberInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaLineNumberInputStreamReadAllBytes() throws IOException {
        try (LineNumberInputStream reader = new LineNumberInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaLineNumberInputStreamReadNBytes() throws IOException {
        try (LineNumberInputStream reader = new LineNumberInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}

