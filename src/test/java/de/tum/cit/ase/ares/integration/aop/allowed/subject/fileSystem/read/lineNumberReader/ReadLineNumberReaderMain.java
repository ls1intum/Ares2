package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.lineNumberReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class ReadLineNumberReaderMain {

    private ReadLineNumberReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link LineNumberReader} for reading.
     * @return The content of the trusted file as string with line numbers
     */
    public static String accessFileSystemViaLineNumberReader() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            String line;
            StringBuilder content = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }            
            return content.toString();
        }
    }
}
