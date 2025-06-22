package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.printStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class PrintStreamWriteMain {

    private PrintStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link PrintStream} directly for writing.
     */
    public static void accessFileSystemViaPrintStream() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ps.write(100);
        }
    }

    public static void accessFileSystemViaPrintStreamWithData() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            ps.write(data);
        }
    }

    public static void accessFileSystemViaPrintStreamWithDataAndOffset() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] data = "Hello, world!".getBytes();
            ps.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaPrintStreamWithPrint() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ps.print("Hello, PrintStream!");
        }
    }

    public static void accessFileSystemViaPrintStreamWithPrintln() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ps.println("Hello, PrintStream with line break!");
        }
    }

    public static void accessFileSystemViaPrintStreamWithPrintf() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ps.printf("Formatted %s with value %d", "string", 42);
        }
    }

    public static void accessFileSystemViaPrintStreamWithAppend() throws IOException {
        try (PrintStream ps = new PrintStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ps.append('A').append("BC").append("DEF", 1, 3);
        }
    }
}
