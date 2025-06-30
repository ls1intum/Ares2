package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader;

import java.io.*;

public class Reader {

    private Reader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Reader for character reading.
     * Test Case: Reader.read() - Read one Unicode code unit; -1 at EOF
     */
    public static void accessFileSystemViaReader() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             java.io.InputStreamReader isr = new java.io.InputStreamReader(fis);
             java.io.BufferedReader reader = new java.io.BufferedReader(isr)) {
            
            // Test various Reader operations
            reader.read(); // Single character read - returns -1 at EOF
        }
    }
}
