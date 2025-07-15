package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.zipOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_TXT  = NOT_TRUSTED_DIR + "/nottrusted.txt";   // used for simple demo cases
    private static final String NOT_TRUSTED_ZIP  = NOT_TRUSTED_DIR + "/nottrusted.zip";   // real .zip container

    private ZipOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link ZipOutputStream} directly for writing.
     */
    public static void accessFileSystemViaZipOutputStream() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(NOT_TRUSTED_TXT))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write(100);
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithData() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(NOT_TRUSTED_TXT))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Hello, world!".getBytes());
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithDataAndOffset() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(NOT_TRUSTED_ZIP))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            byte[] data = "Hello, world!".getBytes();
            zos.write(data, 0, data.length);
            zos.closeEntry();
        }
    }

    public static void accessFileSystemViaZipOutputStreamWithMultipleEntries() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(NOT_TRUSTED_ZIP))) {
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
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(NOT_TRUSTED_ZIP))) {
            ZipEntry entry = new ZipEntry("compressed.txt");

            // Set compression method and level
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);

            zos.putNextEntry(entry);
            byte[] largeData = new byte[10_000]; // Simulate large data
            zos.write(largeData);
            zos.closeEntry();
        }
    }
}
