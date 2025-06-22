package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.checkedInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CheckedInputStreamReadMain {

    private CheckedInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaCheckedInputStreamRead() throws IOException {
        try (CheckedInputStream reader = new CheckedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), new CRC32())) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaCheckedInputStreamReadByteArray() throws IOException {
        try (CheckedInputStream reader = new CheckedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), new CRC32())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength() throws IOException {
        try (CheckedInputStream reader = new CheckedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), new CRC32())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaCheckedInputStreamReadAllBytes() throws IOException {
        try (CheckedInputStream reader = new CheckedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), new CRC32())) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaCheckedInputStreamReadNBytes() throws IOException {
        try (CheckedInputStream reader = new CheckedInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), new CRC32())) {
            return reader.readNBytes(1024);
        }
    }
}
