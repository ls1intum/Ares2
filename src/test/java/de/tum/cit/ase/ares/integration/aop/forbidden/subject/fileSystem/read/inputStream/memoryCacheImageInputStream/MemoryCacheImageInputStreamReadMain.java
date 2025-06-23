package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.memoryCacheImageInputStream;

import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MemoryCacheImageInputStreamReadMain {

    private MemoryCacheImageInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaMemoryCacheImageInputStreamRead() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaMemoryCacheImageInputStreamReadByteArray() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaMemoryCacheImageInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static void accessFileSystemViaMemoryCacheImageInputStreamReadFully() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer);
        }
    }

    public static void accessFileSystemViaMemoryCacheImageInputStreamReadFullyWithOffset() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer, 0, buffer.length);
        }
    }

    public static boolean accessFileSystemViaMemoryCacheImageInputStreamReadBoolean() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readBoolean();
        }
    }

    public static byte[] accessFileSystemViaMemoryCacheImageInputStreamReadByte() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return new byte[]{reader.readByte()};
        }
    }

    public static int accessFileSystemViaMemoryCacheImageInputStreamReadUnsignedByte() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readUnsignedByte();
        }
    }

    public static char accessFileSystemViaMemoryCacheImageInputStreamReadChar() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readChar();
        }
    }

    public static double accessFileSystemViaMemoryCacheImageInputStreamReadDouble() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readDouble();
        }
    }

    public static float accessFileSystemViaMemoryCacheImageInputStreamReadFloat() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readFloat();
        }
    }

    public static long accessFileSystemViaMemoryCacheImageInputStreamReadLong() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readLong();
        }
    }

    public static short accessFileSystemViaMemoryCacheImageInputStreamReadShort() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readShort();
        }
    }

    public static int accessFileSystemViaMemoryCacheImageInputStreamReadUnsignedShort() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             MemoryCacheImageInputStream reader = new MemoryCacheImageInputStream(fis)) {
            return reader.readUnsignedShort();
        }
    }
}
