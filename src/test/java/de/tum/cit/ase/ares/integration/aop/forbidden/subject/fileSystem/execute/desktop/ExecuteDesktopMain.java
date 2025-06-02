package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ExecuteDesktopMain {

    private ExecuteDesktopMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Desktop} class for execution.
     */
    public static void accessFileSystemViaDesktop() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
            desktop.open(file);
        }
    }
}