package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.*;

public class InputStream {

    private InputStream() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using InputStream.read() method.
     * Test Case: InputStream.read() - Read one byte; returns -1 at EOF
     */
    public static void accessFileSystemViaInputStreamRead() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            while (fis.read() != -1) {
                // Reading one byte at a time
            }
        }
    }

    /**
     * Access the file system using InputStream.available() method.
     * Test Case: InputStream.available() - Bytes that can be read without blocking
     */
    public static void accessFileSystemViaInputStreamAvailable() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            fis.available(); // Check available bytes
        }
    }

    /**
     * Access the file system using InputStream.readAllBytes() method.
     * Test Case: InputStream.readAllBytes() - Slurp the whole file into a byte[]
     */
    public static void accessFileSystemViaInputStreamReadAllBytes() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            fis.readAllBytes(); // Read entire file into byte array
        }
    }
}
