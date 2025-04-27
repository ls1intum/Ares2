package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.jFileChooser;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadJFileChooserMain {

    private ReadJFileChooserMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link JFileChooser} class for reading.
     */
    public static void accessFileSystemViaJFileChooser() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] data = new byte[(int) selectedFile.length()];
                fis.read(data);
            }
        }
    }
}