package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.reader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ReadReaderMain {

    private ReadReaderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Reader.read() method.
     */
    public static void accessFileSystemViaReaderRead() throws IOException {
        try (Reader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/reader/trustedFile.txt")) {
            @SuppressWarnings("unused")
            int ch = reader.read();
            // Just read one character
        }
    }

    /**
     * Access the file system using Reader.read(char[] cbuf) method.
     */
    public static void accessFileSystemViaReaderReadCharArray() throws IOException {
        try (Reader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/reader/trustedFile.txt")) {
            char[] buffer = new char[1024];
            @SuppressWarnings("unused")
            int charsRead = reader.read(buffer);
            // Just read into buffer
        }
    }

    /**
     * Access the file system using Reader.read(char[] cbuf, int off, int len) method.
     */
    public static void accessFileSystemViaReaderReadCharArrayOffsetLength() throws IOException {
        try (Reader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/reader/trustedFile.txt")) {
            char[] buffer = new char[1024];
            @SuppressWarnings("unused")
            int charsRead = reader.read(buffer, 0, buffer.length);
            // Just read with offset and length
        }
    }
}
