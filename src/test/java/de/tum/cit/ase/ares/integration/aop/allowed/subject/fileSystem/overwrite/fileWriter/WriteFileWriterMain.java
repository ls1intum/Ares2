package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileWriter;

import java.io.FileWriter;
import java.io.IOException;

public class WriteFileWriterMain {

    private WriteFileWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileWriter} for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaFileWriter(String text) throws IOException {
        try (FileWriter writer = new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            writer.write(text);
        }
    }
}