package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.fileInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class FileInputStreamReadMain {

    private FileInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFileInputStreamRead() throws IOException {
        try (FileInputStream reader = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFileInputStreamReadByteArray() throws IOException {
        try (FileInputStream reader = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFileInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FileInputStream reader = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaFileInputStreamReadAllBytes() throws IOException {
        try (FileInputStream reader = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaFileInputStreamReadNBytes() throws IOException {
        try (FileInputStream reader = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            return reader.readNBytes(1024);
        }
    }
}
