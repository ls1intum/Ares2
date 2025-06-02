package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.overwrite.bufferedWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteBufferedWriterMain {

    private WriteBufferedWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link BufferedWriter} for writing.
     */
    public static void accessFileSystemViaBufferedWriter() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            writer.write("Hello, world!");
            writer.newLine();
            writer.write("This is a test.");
        }
    }
}