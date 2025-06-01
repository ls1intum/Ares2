package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileReader;

import java.io.FileReader;
import java.io.IOException;

public class ReadFileReaderMain {

    private ReadFileReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using FileReader.read()
     * Reads the file character by character.
     */
    public static String accessFileSystemViaFileReaderRead() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using FileReader.read(char[] cbuf)
     * Reads the file into a character buffer.
     */
    public static String accessFileSystemViaFileReaderReadCharArray() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, numRead);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using FileReader.read(char[] cbuf, int off, int len)
     * Reads the file into a buffer with specific offset and length.
     */
    public static String accessFileSystemViaFileReaderReadCharArrayOffsetLength() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
                content.append(buffer, 0, numRead);
            }
            return content.toString();
        }
    }


}
