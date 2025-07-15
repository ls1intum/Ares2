package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.printWriter;

import java.io.IOException;
import java.io.PrintWriter;

public class WritePrintWriterMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private WritePrintWriterMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link PrintWriter} for writing.
     */
    public static void accessFileSystemViaPrintWriter() throws IOException {
        try (PrintWriter writer = new PrintWriter(NOT_TRUSTED_FILE)) {
            writer.println("Hello, world!");
            writer.println("This is a test.");
        }
    }
}
