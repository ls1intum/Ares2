package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.randomAccessFile;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private RandomAccessFileWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /* ------------------------------------------------------------- */
    /*  Boolean / Byte / Bytes                                       */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaRandomAccessFileWriteBoolean() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeBoolean(true);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteByte() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeByte(65); // 'A'
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteBytes() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeBytes("Hello RandomAccessFile");
        }
    }

    /* ------------------------------------------------------------- */
    /*  Char / Chars                                                 */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaRandomAccessFileWriteChar() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeChar('A');
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteChars() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeChars("Hello RandomAccessFile");
        }
    }

    /* ------------------------------------------------------------- */
    /*  Primitives                                                   */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaRandomAccessFileWriteDouble() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeDouble(Math.PI);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteFloat() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeFloat(3.14f);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteInt() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeInt(42);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteLong() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeLong(Long.MAX_VALUE);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteShort() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeShort(1000);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteUTF() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.writeUTF("Hello UTF RandomAccessFile");
        }
    }

    /* ------------------------------------------------------------- */
    /*  Generic write methods                                        */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaRandomAccessFileWrite() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.write(100);
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteByteArray() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.write("Hello RandomAccessFile".getBytes());
        }
    }

    public static void accessFileSystemViaRandomAccessFileWriteByteArrayPortion() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(NOT_TRUSTED_FILE, "rw")) {
            raf.write("Hello RandomAccessFile".getBytes(), 0, 5);
        }
    }
}
