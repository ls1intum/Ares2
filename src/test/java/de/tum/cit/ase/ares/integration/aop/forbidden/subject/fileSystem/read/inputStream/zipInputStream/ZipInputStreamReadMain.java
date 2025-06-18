package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.zipInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ZipInputStreamReadMain {

    private ZipInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaZipInputStreamRead() throws IOException {
        try (ZipInputStream reader = new ZipInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextEntry();
            return reader.read();
        }
    }

    public static int accessFileSystemViaZipInputStreamReadByteArray() throws IOException {
        try (ZipInputStream reader = new ZipInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextEntry();
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaZipInputStreamReadByteArrayOffsetLength() throws IOException {
        try (ZipInputStream reader = new ZipInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextEntry();
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaZipInputStreamReadAllBytes() throws IOException {
        try (ZipInputStream reader = new ZipInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextEntry();
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaZipInputStreamReadNBytes() throws IOException {
        try (ZipInputStream reader = new ZipInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextEntry();
            return reader.readNBytes(1024);
        }
    }
}

