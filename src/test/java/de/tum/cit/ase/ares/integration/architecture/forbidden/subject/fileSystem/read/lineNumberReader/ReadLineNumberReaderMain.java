package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.lineNumberReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class ReadLineNumberReaderMain {

    private ReadLineNumberReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link LineNumberReader} for reading.
     */
    public static void accessFileSystemViaLineNumberReader() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            String line;
            StringBuilder content = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                content.append("Line ").append(reader.getLineNumber()).append(": ").append(line);
                content.append(System.lineSeparator());
            }
        }
    }
}
