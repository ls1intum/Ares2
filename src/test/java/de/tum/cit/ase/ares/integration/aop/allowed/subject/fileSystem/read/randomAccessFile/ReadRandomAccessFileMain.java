package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.randomAccessFile;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadRandomAccessFileMain {

    private ReadRandomAccessFileMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link RandomAccessFile} for reading.
     * @return The content of the trusted file
     */
    public static String accessFileSystemViaRandomAccessFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt", "r")) {
            byte[] data = new byte[(int) file.length()];
            file.readFully(data);
            return new String(data);
        }
    }
}