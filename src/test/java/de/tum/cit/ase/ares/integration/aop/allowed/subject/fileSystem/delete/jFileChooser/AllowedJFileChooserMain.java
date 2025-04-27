package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.jFileChooser;

import javax.swing.JFileChooser;

public class AllowedJFileChooserMain {

    private AllowedJFileChooserMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using JFileChooser.
     */
    public static void accessFileSystemViaJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);
    }
}
