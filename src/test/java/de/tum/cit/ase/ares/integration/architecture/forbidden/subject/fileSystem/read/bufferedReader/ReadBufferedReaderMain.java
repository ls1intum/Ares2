package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.bufferedReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadBufferedReaderMain {

    private ReadBufferedReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link BufferedReader} for reading.
     */
    public static void accessFileSystemViaBufferedReader() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }
    }
}