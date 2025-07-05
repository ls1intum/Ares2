package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.*;

public class FileinputStreamMain {

    private FileinputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileInputStream} and various read methods.
     * Test Cases: FileInputStream(String path), read(), read(byte[]), read(byte[], int, int), 
     * readAllBytes(), readNBytes(), available(), skip()
     */
    public static void accessFileSystemViaFileInputStream() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            // Test various read operations
            fis.read(); // Single byte read
            fis.read(new byte[1024]); // Byte array read
            fis.read(new byte[1024], 0, 512); // Byte array with offset/length
            fis.readAllBytes(); // Read all bytes
            fis.readNBytes(100); // Read N bytes
            fis.available(); // Check available bytes
            fis.skip(10); // Skip bytes
        }
    }
}
