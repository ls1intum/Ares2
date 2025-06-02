package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.fileRead;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadFileReadMain {

    private ReadFileReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link File} class for reading.
     */
    public static void accessFileSystemViaFileRead() throws IOException {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
        }
    }
}