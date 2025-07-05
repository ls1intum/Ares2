package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class InputStreamReader {

    private InputStreamReader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using InputStreamReader for character conversion.
     * Test Cases: InputStreamReader(InputStream in[, Charset]), read(), read(char[]), read(char[], int, int)
     * - Decode raw bytes → Unicode text
     */
    public static void accessFileSystemViaInputStreamReader() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            // Test InputStreamReader with default charset
            java.io.InputStreamReader isr1 = new java.io.InputStreamReader(fis);
            isr1.read(); // Single character read
            isr1.read(new char[1024]); // Character array read
            isr1.read(new char[1024], 0, 512); // Character array with offset/length
            isr1.close();
            
            // Test InputStreamReader with explicit charset
            try (FileInputStream fis2 = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
                 java.io.InputStreamReader isr2 = new java.io.InputStreamReader(fis2, StandardCharsets.UTF_8)) {
                isr2.read(); // Single character read with UTF-8
            }
        }
    }
}
