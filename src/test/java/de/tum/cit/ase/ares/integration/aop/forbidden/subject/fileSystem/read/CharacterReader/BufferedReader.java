package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader;

import java.io.*;

public class BufferedReader {

    private BufferedReader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using BufferedReader.readLine() for efficient line reading.
     * Test Case: BufferedReader.readLine() - Efficiently read an entire text line
     */
    public static void accessFileSystemViaBufferedReaderReadLine() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             java.io.InputStreamReader isr = new java.io.InputStreamReader(fis);
             java.io.BufferedReader reader = new java.io.BufferedReader(isr)) {
            reader.readLine(); // Efficiently read an entire text line
        }
    }
}
