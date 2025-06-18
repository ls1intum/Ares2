package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.zipOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWriteMain {

    private ZipOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link ZipOutputStream} directly for writing.
     */
    public static void accessFileSystemViaZipOutputStream() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write(100);
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithData() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            byte[] data = "Hello, world!".getBytes();
            zos.write(data);
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithDataAndOffset() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.zip"))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            byte[] data = "Hello, world!".getBytes();
            zos.write(data, 0, data.length);
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithMultipleEntries() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.zip"))) {
            // First entry
            ZipEntry entry1 = new ZipEntry("file1.txt");
            zos.putNextEntry(entry1);
            zos.write("First file content".getBytes());
            zos.closeEntry();

            // Second entry
            ZipEntry entry2 = new ZipEntry("file2.txt");
            zos.putNextEntry(entry2);
            zos.write("Second file content".getBytes());
            zos.closeEntry();

            // Directory entry
            ZipEntry dirEntry = new ZipEntry("subdir/");
            zos.putNextEntry(dirEntry);
            zos.closeEntry();

            // File in directory
            ZipEntry entry3 = new ZipEntry("subdir/file3.txt");
            zos.putNextEntry(entry3);
            zos.write("File in subdirectory".getBytes());
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithCompression() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.zip"))) {
            ZipEntry entry = new ZipEntry("compressed.txt");

            // Set compression method and level
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9); // Maximum compression

            zos.putNextEntry(entry);
            byte[] largeData = new byte[10000]; // Simulate large data
            zos.write(largeData);
            zos.closeEntry();
        }
    }
}
