package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.outputStreamWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class OutputStreamWriterWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private OutputStreamWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /* ------------------------------------------------------------- */
    /*  Basic write / append calls                                   */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaOutputStreamWriter() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppend() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE, true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteCharArray() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.write(new char[]{'H','e','l','l','o',' ','W','o','r','l','d'});
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteCharArrayPortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            char[] cbuf = "Hello World".toCharArray();
            writer.write(cbuf, 0, 5); // "Hello"
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteInt() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.write(65); // 'A'
        }
    }

    public static void accessFileSystemViaOutputStreamWriterWriteStringPortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.write("Hello World", 0, 5);
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppendChar() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaOutputStreamWriterAppendCharSequencePortion() throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(NOT_TRUSTED_FILE))) {
            writer.append("Hello World", 0, 5);
        }
    }
}