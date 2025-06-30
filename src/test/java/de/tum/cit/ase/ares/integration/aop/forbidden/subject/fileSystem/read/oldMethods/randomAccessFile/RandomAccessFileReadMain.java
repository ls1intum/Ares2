package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.randomAccessFile;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileReadMain {

    private RandomAccessFileReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaRandomAccessFileRead() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.read();
        }
    }

    public static int accessFileSystemViaRandomAccessFileReadByteArray() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            byte[] buffer = new byte[1024];
            return file.read(buffer);
        }
    }

    public static int accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            byte[] buffer = new byte[1024];
            return file.read(buffer, 0, buffer.length);
        }
    }

    public static boolean accessFileSystemViaRandomAccessFileReadBoolean() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readBoolean();
        }
    }

    public static byte accessFileSystemViaRandomAccessFileReadByte() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readByte();
        }
    }

    public static char accessFileSystemViaRandomAccessFileReadChar() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readChar();
        }
    }

    public static double accessFileSystemViaRandomAccessFileReadDouble() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readDouble();
        }
    }

    public static float accessFileSystemViaRandomAccessFileReadFloat() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readFloat();
        }
    }

    public static void accessFileSystemViaRandomAccessFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            byte[] data = new byte[1024];
            file.readFully(data);
        }
    }

    public static void accessFileSystemViaRandomAccessFileReadFullyOffsetLength() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            byte[] buffer = new byte[1024];
            file.readFully(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaRandomAccessFileReadInt() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readInt();
        }
    }


    public static String accessFileSystemViaRandomAccessFileReadLine() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readLine();
        }
    }

    public static long accessFileSystemViaRandomAccessFileReadLong() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readLong();
        }
    }

    public static short accessFileSystemViaRandomAccessFileReadShort() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readShort();
        }
    }

    public static int accessFileSystemViaRandomAccessFileReadUnsignedByte() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readUnsignedByte();
        }
    }

    public static int accessFileSystemViaRandomAccessFileReadUnsignedShort() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readUnsignedShort();
        }
    }

    public static String accessFileSystemViaRandomAccessFileReadUTF() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            return file.readUTF();
        }
    }
}