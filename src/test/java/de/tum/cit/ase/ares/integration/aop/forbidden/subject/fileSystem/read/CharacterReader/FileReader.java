package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader;

import java.io.*;

public class FileReader {

    private FileReader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using FileReader for text file reading.
     * Test Cases: FileReader(String path), read(), read(char[]), read(char[], int, int)
     * - Text-file reader using the platform charset
     */
    public static void accessFileSystemViaFileReader() throws IOException {
        try (java.io.FileReader fr = new java.io.FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            // Test various read operations
            fr.read(); // Single character read
            fr.read(new char[1024]); // Character array read
            fr.read(new char[1024], 0, 512); // Character array with offset/length
            fr.ready(); // Check if ready to read
            fr.skip(10); // Skip characters
        }
    }
}
