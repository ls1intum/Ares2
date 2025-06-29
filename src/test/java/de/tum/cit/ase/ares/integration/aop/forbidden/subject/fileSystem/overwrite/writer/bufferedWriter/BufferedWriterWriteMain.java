package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BufferedWriterWriteMain {

    private static final String NOT_TRUSTED_FILE =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt";

    private BufferedWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaBufferedWriter() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaBufferedWriterAppend() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE, true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaBufferedWriterWriteCharArray() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaBufferedWriterWriteCharArrayPortion() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaBufferedWriterWriteInt() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write(65); // Writes 'A'
        }
    }

    public static void accessFileSystemViaBufferedWriterWriteStringPortion() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            String str = "Hello World";
            writer.write(str, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaBufferedWriterAppendChar() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaBufferedWriterAppendCharSequencePortion() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5); // Appends "Hello"
        }
    }

    public static void accessFileSystemViaBufferedWriterNewLine() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("First line");
            writer.newLine();
            writer.write("Second line");
        }
    }
}