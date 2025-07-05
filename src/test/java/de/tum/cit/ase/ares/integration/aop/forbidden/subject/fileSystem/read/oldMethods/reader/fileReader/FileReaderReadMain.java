package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.fileReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

public class FileReaderReadMain {

    private FileReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFileReaderRead() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFileReaderReadCharArray() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFileReaderReadCharArrayOffsetLength() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static int accessFileSystemViaFileReaderReadCharBuffer() throws IOException {
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            CharBuffer buffer = CharBuffer.allocate(1024);
            return reader.read(buffer);
        }
    }
}
