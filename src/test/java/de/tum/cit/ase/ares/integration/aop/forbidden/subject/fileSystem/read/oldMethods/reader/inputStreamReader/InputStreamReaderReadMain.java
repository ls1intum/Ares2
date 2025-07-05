package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.inputStreamReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class InputStreamReaderReadMain {

    private InputStreamReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaInputStreamReaderRead() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaInputStreamReaderReadCharArray() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaInputStreamReaderReadCharArrayOffsetLength() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaInputStreamReaderReadCharBuffer() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharBuffer buffer = CharBuffer.allocate(1024);
            return reader.read(buffer);
        }
    }
}