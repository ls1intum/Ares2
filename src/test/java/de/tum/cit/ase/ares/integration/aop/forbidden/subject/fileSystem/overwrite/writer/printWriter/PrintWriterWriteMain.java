package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.printWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintWriterWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private PrintWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaPrintWriter() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaPrintWriterAppend() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE, true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaPrintWriterWriteCharArray() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            char[] cbuf = {'H','e','l','l','o',' ','W','o','r','l','d'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaPrintWriterWriteCharArrayPortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            char[] cbuf = "Hello World".toCharArray();
            writer.write(cbuf, 0, 5); // "Hello"
        }
    }

    public static void accessFileSystemViaPrintWriterWriteInt() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write(65);
        }
    }

    public static void accessFileSystemViaPrintWriterWriteStringPortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("Hello World", 0, 5);
        }
    }

    public static void accessFileSystemViaPrintWriterAppendChar() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaPrintWriterAppendCharSequencePortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.append("Hello World", 0, 5);
        }
    }

    public static void accessFileSystemViaPrintWriterPrint() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.print("Print method example");
        }
    }

    public static void accessFileSystemViaPrintWriterPrintln() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.println("Println method example");
        }
    }

    public static void accessFileSystemViaPrintWriterPrintf() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.printf("Formatted %s with value %d", "string", 42);
        }
    }
}
