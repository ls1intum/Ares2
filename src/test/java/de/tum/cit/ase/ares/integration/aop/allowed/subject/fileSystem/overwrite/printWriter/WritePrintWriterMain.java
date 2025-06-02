package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.printWriter;

import java.io.IOException;
import java.io.PrintWriter;

public class WritePrintWriterMain {

    private WritePrintWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link PrintWriter} for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaPrintWriter(String text) throws IOException {
        try (PrintWriter writer = new PrintWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            writer.print(text);
        }
    }
}