package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.filterWriter;

import java.io.FilterWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FilterWriterWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private FilterWriterWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /* ------------------------------------------------------------- */
    /*  Simple FilterWriter impl                                     */
    /* ------------------------------------------------------------- */

    static class SimpleFilterWriter extends FilterWriter {
        SimpleFilterWriter(Writer out) { super(out); }
    }

    /* ------------------------------------------------------------- */
    /*  Write / Append variants                                      */
    /* ------------------------------------------------------------- */

    public static void accessFileSystemViaFilterWriter() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("Hello, world!");
        }
    }

    public static void accessFileSystemViaFilterWriterAppend() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE, true))) {
            writer.append("Appending this line.");
        }
    }

    public static void accessFileSystemViaFilterWriterWriteCharArray() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write(new char[]{'H','e','l','l','o',' ','W','o','r','l','d'});
        }
    }

    public static void accessFileSystemViaFilterWriterWriteCharArrayPortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            char[] cbuf = "Hello World".toCharArray();
            writer.write(cbuf, 0, 5); // "Hello"
        }
    }

    public static void accessFileSystemViaFilterWriterWriteInt() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write(65); // 'A'
        }
    }

    public static void accessFileSystemViaFilterWriterWriteStringPortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.write("Hello World", 0, 5);
        }
    }

    public static void accessFileSystemViaFilterWriterAppendChar() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            writer.append('A');
        }
    }

    public static void accessFileSystemViaFilterWriterAppendCharSequencePortion() throws IOException {
        try (FilterWriter writer = new SimpleFilterWriter(new FileWriter(NOT_TRUSTED_FILE))) {
            CharSequence csq = "Hello World";
            writer.append(csq, 0, 5);
        }
    }
}
