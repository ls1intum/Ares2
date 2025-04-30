package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ReadScannerMain {

    private ReadScannerMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link Scanner} for reading.
     */
    public static void accessFileSystemViaScanner() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
                content.append(System.lineSeparator());
            }
        }
    }
}