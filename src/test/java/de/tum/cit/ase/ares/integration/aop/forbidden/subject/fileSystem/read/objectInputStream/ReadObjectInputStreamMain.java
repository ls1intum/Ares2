package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.objectInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReadObjectInputStreamMain {

    private ReadObjectInputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link ObjectInputStream} for reading.
     */
    public static void accessFileSystemViaObjectInputStream() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            // Try to read object, even if it fails due to format issues, the file system access is tested
            try {
                ois.readObject();
            } catch (Exception e) {
                // Expected for non-object files, but file system access is still attempted
            }
        }
    }
}
