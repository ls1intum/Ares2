package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.fileWriter;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private FileWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaFileWriter() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaFileWriterAppend() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE, true)) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaFileWriterWriteCharArray() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaFileWriterWriteCharArrayPortion() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaFileWriterWriteInt() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            writer.write(65); // Writes 'A'
        }
    }

    public static void accessFileSystemViaFileWriterWriteStringPortion() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            String str = "Hello World";
            writer.write(str, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaFileWriterAppendChar() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaFileWriterAppendCharSequencePortion() throws IOException {
        try (FileWriter writer = new FileWriter(NOT_TRUSTED_FILE)) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5); // Appends "Hello"
        }
    }
}