package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStreamReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadInputStreamReaderMain {

    private ReadInputStreamReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link InputStreamReader} for reading.
     */
    public static void accessFileSystemViaInputStreamReader() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/trusted.txt"))) {
            char[] buffer = new char[1024];
            StringBuilder content = new StringBuilder();
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
        }
    }
}