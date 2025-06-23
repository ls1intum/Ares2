package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.dataOutputStream;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataOutputStreamWriteMain {

    private DataOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link DataOutputStream} directly for writing.
     */
    public static void accessFileSystemViaDataOutputStream() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.write(100);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWithData() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            dos.write(data);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWithDataAndOffset() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            dos.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWithPrimitives() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeInt(42);
            dos.writeDouble(3.14);
            dos.writeBoolean(true);
            dos.writeUTF("Hello DataOutputStream");
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteBoolean() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeBoolean(true);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteByte() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeByte(65); // ASCII 'A'
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteBytes() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeBytes("Hello DataOutputStream");
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteChar() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeChar('A'); // Unicode character
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteChars() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeChars("Hello DataOutputStream");
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteDouble() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeDouble(Math.PI); // 3.141592653589793
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteFloat() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeFloat(3.14f);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteInt() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeInt(42);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteLong() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeLong(Long.MAX_VALUE);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteShort() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeShort(1000);
        }
    }

    public static void accessFileSystemViaDataOutputStreamWriteUTF() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            dos.writeUTF("Hello UTF DataOutputStream");
        }
    }
}
