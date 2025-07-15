package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileReader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    /**
     * Access the file system using FileReader(File file) constructor.
     */
    public static String accessFileSystemViaFileReaderFile() throws IOException {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");
        try (FileReader reader = new FileReader(file)) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using FileReader(File file, Charset charset) constructor.
     */
    public static String accessFileSystemViaFileReaderFileCharset() throws IOException {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using FileReader(String fileName, Charset charset) constructor.
     */
    public static String accessFileSystemViaFileReaderStringCharset() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt", StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            return content.toString();
        }
    }

    /**
     * Access the file system using FileReader(FileDescriptor fd) constructor.
     */
    public static String accessFileSystemViaFileReaderFileDescriptor() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            FileDescriptor fd = fis.getFD();
            try (FileReader reader = new FileReader(fd)) {
                StringBuilder content = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    content.append((char) ch);
                }
                return content.toString();
            }
        }
    }

    /**
     * Access the file system using FileReader.ready() method.
     */
    public static String accessFileSystemViaFileReaderReady() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            boolean ready = reader.ready();
            return "Ready: " + ready;
        }
    }

    /**
     * Access the file system using FileReader.close() method explicitly.
     */
    public static String accessFileSystemViaFileReaderClose() throws IOException {
        FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");
        reader.close();
        return "Closed";
    }
}
