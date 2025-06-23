package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.filterWriter;

import java.io.FilterWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FilterWriterWriteMain {

    private FilterWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    // Custom FilterWriter implementation for demonstration
    static class SimpleFilterWriter extends FilterWriter {
        public SimpleFilterWriter(Writer out) {
            super(out);
        }
    }

    public static void accessFileSystemViaFilterWriter() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaFilterWriterAppend() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaFilterWriterWriteCharArray() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaFilterWriterWriteCharArrayPortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaFilterWriterWriteInt() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write(65); // Writes 'A'
        }
    }

    public static void accessFileSystemViaFilterWriterWriteStringPortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            String str = "Hello World";
            writer.write(str, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaFilterWriterAppendChar() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaFilterWriterAppendCharSequencePortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5); // Appends "Hello"
        }
    }
}
