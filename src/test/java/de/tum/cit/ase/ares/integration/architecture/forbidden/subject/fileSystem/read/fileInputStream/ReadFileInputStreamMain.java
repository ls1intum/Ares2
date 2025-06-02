package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.fileSystem.read.fileInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadFileInputStreamMain {

    private ReadFileInputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileInputStream} for reading.
     */
    public static void accessFileSystemViaFileInputStream() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            byte[] buffer = new byte[1024];
            StringBuilder content = new StringBuilder();
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead));
            }
        }
    }
}
