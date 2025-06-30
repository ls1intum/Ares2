package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.LineTokenReader;

import java.io.*;

public class ScannerReader {

    private ScannerReader() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Scanner for line or token-oriented reading.
     * Test Case: Scanner - Token- or line-oriented reader (file or console)
     */
    public static void accessFileSystemViaScanner() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             java.util.Scanner scanner = new java.util.Scanner(fis)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Read a line
            }
            if (scanner.hasNext()) {
                scanner.next(); // Read a token
            }
        }
    }
}
