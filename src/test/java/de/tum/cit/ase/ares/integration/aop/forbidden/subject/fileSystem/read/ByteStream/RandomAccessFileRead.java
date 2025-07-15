package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileRead {

    private RandomAccessFileRead() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using RandomAccessFile.read(byte[]) for seekable byte stream reading.
     * Test Case: RandomAccessFile.read(byte[]) - Seekable reads anywhere in the file
     */
    public static void accessFileSystemViaRandomAccessFileRead() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", "r")) {
            byte[] buffer = new byte[1024];
            raf.read(buffer); // Read bytes into buffer
            raf.seek(0); // Seek to start
            raf.read(buffer); // Read again
        }
    }
}
