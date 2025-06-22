package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileCacheImageInputStream;

import javax.imageio.stream.FileCacheImageInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileCacheImageInputStreamReadMain {

    private FileCacheImageInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFileCacheImageInputStreamRead() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFileCacheImageInputStreamReadByteArray() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static void accessFileSystemViaFileCacheImageInputStreamReadFully() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer);
        }
    }

    public static void accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer, 0, buffer.length);
        }
    }

    public static boolean accessFileSystemViaFileCacheImageInputStreamReadBoolean() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readBoolean();
        }
    }

    public static byte[] accessFileSystemViaFileCacheImageInputStreamReadByte() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return new byte[]{reader.readByte()};
        }
    }

    public static int accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readUnsignedByte();
        }
    }

    public static char accessFileSystemViaFileCacheImageInputStreamReadChar() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readChar();
        }
    }

    public static double accessFileSystemViaFileCacheImageInputStreamReadDouble() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readDouble();
        }
    }

    public static float accessFileSystemViaFileCacheImageInputStreamReadFloat() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readFloat();
        }
    }

    public static long accessFileSystemViaFileCacheImageInputStreamReadLong() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readLong();
        }
    }

    public static short accessFileSystemViaFileCacheImageInputStreamReadShort() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readShort();
        }
    }

    public static int accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort() throws IOException {
        try (FileCacheImageInputStream reader = new FileCacheImageInputStream(
                new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject"))) {
            return reader.readUnsignedShort();
        }
    }
}
