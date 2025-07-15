package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.dataInputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class DataInputStreamReadMain {

    private DataInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaDataInputStreamRead() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaDataInputStreamReadByteArray() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaDataInputStreamReadByteArrayOffsetLength() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaDataInputStreamReadAllBytes() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaDataInputStreamReadNBytes() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }

    public static boolean accessFileSystemViaDataInputStreamReadBoolean() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readBoolean();
        }
    }

    public static byte[] accessFileSystemViaDataInputStreamReadByte() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return new byte[]{reader.readByte()};
        }
    }

    public static char accessFileSystemViaDataInputStreamReadChar() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readChar();
        }
    }

    public static double accessFileSystemViaDataInputStreamReadDouble() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readDouble();
        }
    }

    public static float accessFileSystemViaDataInputStreamReadFloat() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readFloat();
        }
    }

    public static void accessFileSystemViaDataInputStreamReadFully() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer);
        }
    }

    public static void accessFileSystemViaDataInputStreamReadFullyWithOffset() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer, 0, buffer.length);
        }
    }

    public static String accessFileSystemViaDataInputStreamReadLine() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readLine();
        }
    }

    public static long accessFileSystemViaDataInputStreamReadLong() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readLong();
        }
    }

    public static short accessFileSystemViaDataInputStreamReadShort() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readShort();
        }
    }

    public static int accessFileSystemViaDataInputStreamReadUnsignedByte() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readUnsignedByte();
        }
    }

    public static int accessFileSystemViaDataInputStreamReadUnsignedShort() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readUnsignedShort();
        }
    }

    public static byte[] accessFileSystemViaDataInputStreamReadUTF() throws IOException {
        try (DataInputStream reader = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readUTF().getBytes();
        }
    }
}

