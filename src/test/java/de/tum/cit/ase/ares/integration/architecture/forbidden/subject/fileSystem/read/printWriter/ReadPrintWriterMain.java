package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.printWriter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ReadPrintWriterMain {

    private ReadPrintWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link PrintWriter} for reading (though it's primarily for writing, this tests the file access).
     */
    public static void accessFileSystemViaPrintWriter() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            // Even if we don't actually read, the file system access for opening the file is tested
            writer.checkError(); // This forces file system interaction
        }
    }
}
