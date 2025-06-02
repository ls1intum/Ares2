package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileWrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFileWriteMain {

    private WriteFileWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link File} class for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaFileWrite(String text) throws IOException {
        File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = text.getBytes();
            fos.write(data);
        }
    }
}