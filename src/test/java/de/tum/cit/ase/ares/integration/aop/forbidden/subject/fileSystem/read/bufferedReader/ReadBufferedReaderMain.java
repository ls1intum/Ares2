package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.bufferedReader;

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
    public static void accessFileSystemViaBufferedReaderRead() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            int ch;
            while ((ch = reader.read()) != -1) {
                // Just read the data to trigger the file system access
                @SuppressWarnings("unused")
                char character = (char) ch;
            }
        }
    }

    /**
     * Access the file system using BufferedReader.read(char[] cbuf, int off, int len)
     * Reads the file into a buffer.
     */
    public static void accessFileSystemViaBufferedReaderReadCharArray() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
                // Just read the data to trigger the file system access
                @SuppressWarnings("unused")
                int bytesRead = numRead;
            }
        }
    }    /**
     * Access the file system using BufferedReader.readLine()
     * Reads the file line by line.
     */
    public static void accessFileSystemViaBufferedReaderReadLine() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Just read the data to trigger the file system access
                @SuppressWarnings("unused")
                String readLine = line;
            }
        }
    }
}