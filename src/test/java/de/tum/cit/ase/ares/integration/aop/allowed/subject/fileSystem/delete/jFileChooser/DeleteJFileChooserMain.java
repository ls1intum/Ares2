package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.jFileChooser;

import javax.swing.JFileChooser;

public class DeleteJFileChooserMain {

    private DeleteJFileChooserMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link JFileChooser} class for deletion.
     */
    public static void accessFileSystemViaJFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
    }
}
