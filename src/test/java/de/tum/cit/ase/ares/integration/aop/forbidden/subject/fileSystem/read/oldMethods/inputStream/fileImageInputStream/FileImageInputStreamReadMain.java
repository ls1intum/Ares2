package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.fileImageInputStream;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.IOException;

public class FileImageInputStreamReadMain {

    private FileImageInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFileImageInputStreamRead() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFileImageInputStreamReadByteArray() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static void accessFileSystemViaFileImageInputStreamReadFully() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer);
        }
    }

    public static void accessFileSystemViaFileImageInputStreamReadFullyWithOffset() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer, 0, buffer.length);
        }
    }

    public static boolean accessFileSystemViaFileImageInputStreamReadBoolean() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readBoolean();
        }
    }

    public static byte[] accessFileSystemViaFileImageInputStreamReadByte() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return new byte[]{reader.readByte()};
        }
    }

    public static int accessFileSystemViaFileImageInputStreamReadUnsignedByte() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readUnsignedByte();
        }
    }

    public static char accessFileSystemViaFileImageInputStreamReadChar() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readChar();
        }
    }

    public static double accessFileSystemViaFileImageInputStreamReadDouble() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readDouble();
        }
    }

    public static float accessFileSystemViaFileImageInputStreamReadFloat() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readFloat();
        }
    }

    public static long accessFileSystemViaFileImageInputStreamReadLong() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readLong();
        }
    }

    public static short accessFileSystemViaFileImageInputStreamReadShort() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readShort();
        }
    }

    public static int accessFileSystemViaFileImageInputStreamReadUnsignedShort() throws IOException {
        try (FileImageInputStream reader = new FileImageInputStream(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readUnsignedShort();
        }
    }
}
