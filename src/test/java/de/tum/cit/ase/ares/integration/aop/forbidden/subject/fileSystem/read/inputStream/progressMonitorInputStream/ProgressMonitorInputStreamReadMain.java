package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.progressMonitorInputStream;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class ProgressMonitorInputStreamReadMain {

    private ProgressMonitorInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaProgressMonitorInputStreamRead() throws IOException {
        try (ProgressMonitorInputStream reader = new ProgressMonitorInputStream(null, "Reading", new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaProgressMonitorInputStreamReadByteArray() throws IOException {
        try (ProgressMonitorInputStream reader = new ProgressMonitorInputStream(null, "Reading", new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaProgressMonitorInputStreamReadByteArrayOffsetLength() throws IOException {
        try (ProgressMonitorInputStream reader = new ProgressMonitorInputStream(null, "Reading", new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaProgressMonitorInputStreamReadAllBytes() throws IOException {
        try (ProgressMonitorInputStream reader = new ProgressMonitorInputStream(null, "Reading", new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaProgressMonitorInputStreamReadNBytes() throws IOException {
        try (ProgressMonitorInputStream reader = new ProgressMonitorInputStream(null, "Reading", new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            return reader.readNBytes(1024);
        }
    }
}

