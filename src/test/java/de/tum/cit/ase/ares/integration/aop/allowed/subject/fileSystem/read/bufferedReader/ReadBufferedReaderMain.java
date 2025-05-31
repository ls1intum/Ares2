package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.bufferedReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadBufferedReaderMain {

    private ReadBufferedReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using BufferedReader.read()
     * Reads the file character by character.
     */
    public static String accessFileSystemViaBufferedReaderRead() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using BufferedReader.read(char[] cbuf, int off, int len)
     * Reads the file into a buffer.
     */
    public static String accessFileSystemViaBufferedReaderReadCharArray() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
                content.append(buffer, 0, numRead);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using BufferedReader.readLine()
     * Reads the file line by line.
     */
    public static String accessFileSystemViaBufferedReaderReadLine() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using BufferedReader.ready()
     * Checks if the stream is ready to be read, then reads all lines.
     */
    public static String accessFileSystemViaBufferedReaderReady() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            return reader.ready() ? "Yes, the reader is ready." : "No, the reader is not ready.";
        }
    }
}
