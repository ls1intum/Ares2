package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.inputStreamReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadInputStreamReaderMain {

    private ReadInputStreamReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link InputStreamReader} for reading.
     * @return The content of the trusted file
     */
    public static String accessFileSystemViaInputStreamReader() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            char[] buffer = new char[1024];
            StringBuilder content = new StringBuilder();
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using InputStreamReader.read(char[] cbuf, int offset, int length).
     */
    public static String accessFileSystemViaInputStreamReaderReadCharArrayOffsetLength() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            char[] buffer = new char[1024];
            StringBuilder content = new StringBuilder();
            int charsRead;
            
            while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                content.append(buffer, 0, charsRead);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using InputStreamReader.ready() method.
     */
    public static String accessFileSystemViaInputStreamReaderReady() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            boolean ready = reader.ready();
            return "Ready: " + ready;
        }
    }
}