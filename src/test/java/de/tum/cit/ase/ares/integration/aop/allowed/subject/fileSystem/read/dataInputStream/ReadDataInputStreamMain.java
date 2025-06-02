package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.dataInputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadDataInputStreamMain {

    private ReadDataInputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link DataInputStream} for reading.
     * @return The content of the trusted file as string
     */
    public static String accessFileSystemViaDataInputStream() throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
            StringBuilder content = new StringBuilder();
            try {
                while (true) {
                    byte b = dis.readByte();
                    content.append((char) b);
                }
            } catch (java.io.EOFException e) {
                // Expected when reaching end of file
            }
            
            return content.toString();
        }
    }
}
