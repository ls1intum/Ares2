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
    public static void accessFileSystemViaFileReaderRead() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
        }
    }

    /**
     * Access the file system using FileReader.read(char[] cbuf)
     * Reads the file into a character buffer.
     */
    public static void accessFileSystemViaFileReaderReadCharArray() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            char[] buffer = new char[1024];
            while (reader.read(buffer) != -1) {
                // Reading into buffer
            }
        }
    }

    /**
     * Access the file system using FileReader.read(char[] cbuf, int off, int len)
     * Reads the file into a buffer with specific offset and length.
     */
    public static void accessFileSystemViaFileReaderReadCharArrayOffsetLength() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            char[] buffer = new char[1024];
            while (reader.read(buffer, 0, buffer.length) != -1) {
                // Reading into buffer with offset and length
            }
        }
    }


}
