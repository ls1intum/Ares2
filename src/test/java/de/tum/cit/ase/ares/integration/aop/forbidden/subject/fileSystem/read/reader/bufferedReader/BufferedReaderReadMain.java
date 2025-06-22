package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.bufferedReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.stream.Stream;

public class BufferedReaderReadMain {

    private BufferedReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }
    
    public static int accessFileSystemViaBufferedReaderRead() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaBufferedReaderReadCharArray() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaBufferedReaderReadCharArrayOffsetLength() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaBufferedReaderReadCharBuffer() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharBuffer buffer = CharBuffer.allocate(1024);
            return reader.read(buffer);
        }
    }

    public static String accessFileSystemViaBufferedReaderReadLine() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readLine();
        }
    }

    public static Stream<String> accessFileSystemViaBufferedReaderLines() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.lines();
        }
    }

}