package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.bufferedReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                content.append(line).append(System.lineSeparator());
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using BufferedReader(Reader in, int sz) constructor with buffer size.
     */
    public static String accessFileSystemViaBufferedReaderWithSize() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"), 8192)) {
            String line = reader.readLine();
            return line != null ? line : "";
        }
    }

    /**
     * Access the file system using BufferedReader.ready() method.
     */
    public static String accessFileSystemViaBufferedReaderReady() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            boolean ready = reader.ready();
            return "Ready: " + ready;
        }
    }

    /**
     * Access the file system using BufferedReader.close() method explicitly.
     */
    public static String accessFileSystemViaBufferedReaderClose() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")));
        reader.close();
        return "Closed";
    }

    /**
     * Access the file system using BufferedReader.skip(long n) method.
     */
    public static String accessFileSystemViaBufferedReaderSkip() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            long skipped = reader.skip(10);
            return "Skipped: " + skipped + " characters";
        }
    }
}
