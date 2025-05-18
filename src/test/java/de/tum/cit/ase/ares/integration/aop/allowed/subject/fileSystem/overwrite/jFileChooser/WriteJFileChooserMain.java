package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.jFileChooser;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteJFileChooserMain {

    private WriteJFileChooserMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link JFileChooser} class for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaJFileChooser(String text) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                byte[] data = text.getBytes();
                fos.write(data);
            }
        }
    }
}