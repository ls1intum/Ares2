package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileReader;

import java.io.FileReader;
import java.io.IOException;

public class ReadFileReaderMain {

    private ReadFileReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileReader} for reading.
     */
    public static void accessFileSystemViaFileReader() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            char[] buffer = new char[1024];
            StringBuilder content = new StringBuilder();
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
        }
    }
}
