package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.lineNumberReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.CharBuffer;
import java.util.stream.Stream;

public class LineNumberReaderReadMain {

    private LineNumberReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static String accessFileSystemViaLineNumberReader() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readLine();
        }
    }

    public static int accessFileSystemViaLineNumberReaderRead() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaLineNumberReaderReadCharArray() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaLineNumberReaderReadCharArrayOffsetLength() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaLineNumberReaderReadLine() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharBuffer buffer = CharBuffer.allocate(1024);
            return reader.read(buffer);
        }
    }

    public static Stream<String> accessFileSystemViaLineNumberReaderLines() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.lines();
        }
    }
}
