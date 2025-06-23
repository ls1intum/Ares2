package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.outputStreamWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class OutputStreamWriterWriteMain {

    private OutputStreamWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaOutputStreamWriter() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppend() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteCharArray() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteCharArrayPortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteInt() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write(65); // Writes 'A'
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteStringPortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            String str = "Hello World";
            writer.write(str, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppendChar() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppendCharSequencePortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5); // Appends "Hello"
        }
    }
}
