package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.pushbackReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.CharBuffer;

@SuppressWarnings("unused")
public class PushbackReaderReadMain {

    private PushbackReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaPushbackReaderRead() throws IOException {
        try (PushbackReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaPushbackReaderReadCharArray() throws IOException {
        try (PushbackReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaPushbackReaderReadCharArrayOffsetLength() throws IOException {
        try (PushbackReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaPushbackReaderReadCharBuffer() throws IOException {
        try (PushbackReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharBuffer buffer = CharBuffer.allocate(1024);
            return reader.read(buffer);
        }
    }
}
