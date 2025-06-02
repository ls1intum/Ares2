package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadFileInputStreamMain {

    private ReadFileInputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link FileInputStream} for reading.
     * @return The content of the trusted file as string
     */
    public static String accessFileSystemViaFileInputStream() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
            byte[] buffer = new byte[1024];
            StringBuilder content = new StringBuilder();
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                content.append(new String(buffer, 0, bytesRead));
            }
            
            return content.toString();
        }
    }
}
