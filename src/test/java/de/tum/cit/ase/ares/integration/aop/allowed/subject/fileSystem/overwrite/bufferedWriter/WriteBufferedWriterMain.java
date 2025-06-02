package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.bufferedWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteBufferedWriterMain {

    private WriteBufferedWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link BufferedWriter} for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaBufferedWriter(String text) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            writer.write(text);
        }
    }
}