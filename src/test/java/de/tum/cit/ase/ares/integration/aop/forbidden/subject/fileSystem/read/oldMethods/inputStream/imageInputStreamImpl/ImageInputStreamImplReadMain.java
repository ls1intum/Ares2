package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.inputStream.imageInputStreamImpl;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageInputStreamImplReadMain {

    private ImageInputStreamImplReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    // Custom implementation of ImageInputStreamImpl for testing purposes
    private static class TestImageInputStreamImpl extends ImageInputStreamImpl {
        private final FileInputStream fis;

        public TestImageInputStreamImpl(FileInputStream fis) {
            this.fis = fis;
        }

        @Override
        public int read() throws IOException {
            return fis.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return fis.read(b, off, len);
        }

        @Override
        public void seek(long pos) throws IOException {
            // Simple implementation for seek - not fully functional but sufficient for testing
            long skip = pos - streamPos;
            if (skip > 0) {
                fis.skip(skip);
            }
            streamPos = pos;
        }
    }

    public static int accessFileSystemViaImageInputStreamImplRead() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaImageInputStreamImplReadByteArray() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static void accessFileSystemViaImageInputStreamImplReadFully() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer);
        }
    }

    public static void accessFileSystemViaImageInputStreamImplReadFullyWithOffset() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            byte[] buffer = new byte[1024];
            reader.readFully(buffer, 0, buffer.length);
        }
    }

    public static boolean accessFileSystemViaImageInputStreamImplReadBoolean() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readBoolean();
        }
    }

    public static byte[] accessFileSystemViaImageInputStreamImplReadByte() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return new byte[]{reader.readByte()};
        }
    }

    public static int accessFileSystemViaImageInputStreamImplReadUnsignedByte() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readUnsignedByte();
        }
    }

    public static char accessFileSystemViaImageInputStreamImplReadChar() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readChar();
        }
    }

    public static double accessFileSystemViaImageInputStreamImplReadDouble() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readDouble();
        }
    }

    public static float accessFileSystemViaImageInputStreamImplReadFloat() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readFloat();
        }
    }

    public static long accessFileSystemViaImageInputStreamImplReadLong() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readLong();
        }
    }

    public static short accessFileSystemViaImageInputStreamImplReadShort() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readShort();
        }
    }

    public static int accessFileSystemViaImageInputStreamImplReadUnsignedShort() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             TestImageInputStreamImpl reader = new TestImageInputStreamImpl(fis)) {
            return reader.readUnsignedShort();
        }
    }
}
