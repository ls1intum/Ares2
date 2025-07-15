package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class ExecuteDesktopMain {

    private ExecuteDesktopMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Desktop} class for execution.
     */
    public static void accessFileSystemViaDesktop(String filePath) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File(filePath);
            desktop.open(file);
        }
    }

    /**
     * Access the file system using the {@link Desktop} class for execution with default file.
     */
    public static void accessFileSystemViaDesktop() throws IOException {
        accessFileSystemViaDesktop("test.txt");
    }

    /**
     * Access the file system using the {@link Desktop#browse(URI)} method.
     */
    public static void accessFileSystemViaDesktopBrowse() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            URI uri = URI.create("file:///test.txt");
            desktop.browse(uri);
        }
    }

    /**
     * Access the file system using the {@link Desktop#browseFileDirectory(File)} method.
     */
    public static void accessFileSystemViaDesktopBrowseFileDirectory() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File directory = new File(".");
            desktop.browseFileDirectory(directory);
        }
    }

    /**
     * Access the file system using the {@link Desktop#edit(File)} method.
     */
    public static void accessFileSystemViaDesktopEdit() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("test.txt");
            desktop.edit(file);
        }
    }

    /**
     * Access the file system using the {@link Desktop#print(File)} method.
     */
    public static void accessFileSystemViaDesktopPrint() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("test.txt");
            desktop.print(file);
        }
    }
}