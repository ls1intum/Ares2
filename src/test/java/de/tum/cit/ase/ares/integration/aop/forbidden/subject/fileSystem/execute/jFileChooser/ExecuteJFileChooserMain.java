package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.jFileChooser;

import javax.swing.JFileChooser;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ExecuteJFileChooserMain {

    private ExecuteJFileChooserMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link JFileChooser} class for execution.
     */
    public static void accessFileSystemViaJFileChooser() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(selectedFile);
            }
        }
    }
}