package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.filterReader;

import java.io.FileReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.CharBuffer;

@SuppressWarnings("unused")
public class FilterReaderReadMain {

    private FilterReaderReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaFilterReaderRead() throws IOException {
        try (FilterReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaFilterReaderReadCharArray() throws IOException {
        try (FilterReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaFilterReaderReadCharArrayOffsetLength() throws IOException {
        try (FilterReader reader = new PushbackReader(new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] buffer = new char[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }
}
