package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class DesktopExecuteMain {

    private DesktopExecuteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static void accessFileSystemViaDesktopBrowse() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            URI uri = URI.create("file:///src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
            desktop.browse(uri);
        }
    }

    /**
     * Access the file system using the {@link Desktop#browseFileDirectory(File)} method.
     */
    public static void accessFileSystemViaDesktopBrowseFileDirectory() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File directory = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject");
            desktop.browseFileDirectory(directory);
        }
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

    /**
     * Access the file system using the {@link Desktop#edit(File)} method.
     */
    public static void accessFileSystemViaDesktopEdit() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
            desktop.edit(file);
        }
    }

    /**
     * Access the file system using the {@link Desktop#print(File)} method.
     */
    public static void accessFileSystemViaDesktopPrint() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
            desktop.print(file);
        }
    }
}