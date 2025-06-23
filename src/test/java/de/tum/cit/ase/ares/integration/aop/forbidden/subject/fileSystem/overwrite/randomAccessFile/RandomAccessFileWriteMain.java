package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.randomAccessFile;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileWriteMain {

    private RandomAccessFileWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing boolean values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteBoolean() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeBoolean(true);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing byte values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteByte() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeByte(65); // ASCII 'A'
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing strings as bytes.
     */
    public static void accessFileSystemViaRandomAccessFileWriteBytes() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeBytes("Hello RandomAccessFile");
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing character values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteChar() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeChar('A'); // Unicode character
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing strings as characters.
     */
    public static void accessFileSystemViaRandomAccessFileWriteChars() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeChars("Hello RandomAccessFile");
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing double values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteDouble() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeDouble(Math.PI); // 3.141592653589793
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing float values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteFloat() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeFloat(3.14f);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing integer values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteInt() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeInt(42);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing long values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteLong() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeLong(Long.MAX_VALUE);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing short values.
     */
    public static void accessFileSystemViaRandomAccessFileWriteShort() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeShort(1000);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing strings in UTF format.
     */
    public static void accessFileSystemViaRandomAccessFileWriteUTF() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.writeUTF("Hello UTF RandomAccessFile");
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing bytes.
     */
    public static void accessFileSystemViaRandomAccessFileWrite() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            raf.write(100); // Write a single byte
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing byte arrays.
     */
    public static void accessFileSystemViaRandomAccessFileWriteByteArray() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            byte[] data = "Hello RandomAccessFile".getBytes();
            raf.write(data);
        }
    }

    /**
     * Access the file system using {@link RandomAccessFile} for writing portions of byte arrays.
     */
    public static void accessFileSystemViaRandomAccessFileWriteByteArrayPortion() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "rw")) {
            byte[] data = "Hello RandomAccessFile".getBytes();
            raf.write(data, 0, 5); // Write only "Hello"
        }
    }
}