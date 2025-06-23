package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.printWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintWriterWriteMain {

    private PrintWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaPrintWriter() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaPrintWriterAppend() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt", true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaPrintWriterWriteCharArray() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf);
        }
    }

    public static void accessFileSystemViaPrintWriterWriteCharArrayPortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            char[] cbuf = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
            writer.write(cbuf, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaPrintWriterWriteInt() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write(65); // Writes 'A'
        }
    }

    public static void accessFileSystemViaPrintWriterWriteStringPortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            String str = "Hello World";
            writer.write(str, 0, 5); // Writes "Hello"
        }
    }

    public static void accessFileSystemViaPrintWriterAppendChar() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaPrintWriterAppendCharSequencePortion() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5); // Appends "Hello"
        }
    }

    public static void accessFileSystemViaPrintWriterPrint() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.print("Print method example");
        }
    }

    public static void accessFileSystemViaPrintWriterPrintln() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.println("Println method example");
        }
    }

    public static void accessFileSystemViaPrintWriterPrintf() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.printf("Formatted %s with value %d", "string", 42);
        }
    }
}
