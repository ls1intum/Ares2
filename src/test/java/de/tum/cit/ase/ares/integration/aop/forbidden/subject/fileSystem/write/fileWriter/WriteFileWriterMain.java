package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.fileWriter;

import java.io.FileWriter;
import java.io.IOException;

public class WriteFileWriterMain {

    private WriteFileWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileWriter} for writing.
     */
    public static void accessFileSystemViaFileWriter() throws IOException {
        try (FileWriter writer = new FileWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/output.txt")) {
            writer.write("Hello, world!\n");
            writer.write("This is a test.");
        }
    }
}