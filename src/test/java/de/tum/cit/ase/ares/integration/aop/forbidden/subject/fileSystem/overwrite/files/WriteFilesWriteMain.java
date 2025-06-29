package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public final class WriteFilesWriteMain {

    private static final String NOT_TRUSTED_DIR =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final Path NOT_TRUSTED_FILE  = Path.of(NOT_TRUSTED_DIR, "nottrusted.txt");
    private static final Path NOT_TRUSTED_COPY  = Path.of(NOT_TRUSTED_DIR, "nottrusted-copy.txt");

    private WriteFilesWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link Files#newOutputStream(Path, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesNewOutputStream() throws IOException {
        try (var outputStream = Files.newOutputStream(NOT_TRUSTED_FILE, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            outputStream.write("Hello, world!".getBytes());
        }
    }

    /**
     * Access the file system using the {@link Files#newBufferedWriter(Path, Charset, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesNewBufferedWriterWithCharset() throws IOException {
        try (var writer = Files.newBufferedWriter(NOT_TRUSTED_FILE, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write("Hello, world!");
        }
    }

    /**
     * Access the file system using the {@link Files#newBufferedWriter(Path, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesNewBufferedWriter() throws IOException {
        try (var writer = Files.newBufferedWriter(NOT_TRUSTED_FILE, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write("Hello, world!");
        }
    }

    /**
     * Access the file system using the {@link Files#write(Path, byte[], OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesWrite() throws IOException {
        byte[] content = "Hello, world!".getBytes();
        Files.write(NOT_TRUSTED_FILE, content, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Access the file system using the {@link Files#write(Path, Iterable, Charset, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesWriteLinesWithCharset() throws IOException {
        List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
        Files.write(NOT_TRUSTED_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    /**
     * Access the file system using the {@link Files#write(Path, Iterable, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesWriteLines() throws IOException {
        List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
        Files.write(NOT_TRUSTED_FILE, lines, StandardOpenOption.CREATE);
    }

    /**
     * Access the file system using the {@link Files#writeString(Path, CharSequence, Charset, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesWriteStringWithCharset() throws IOException {
        Files.writeString(NOT_TRUSTED_FILE, "Hello, world!", StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    /**
     * Access the file system using the {@link Files#writeString(Path, CharSequence, OpenOption...)} method.
     */
    public static void accessFileSystemViaFilesWriteString() throws IOException {
        Files.writeString(NOT_TRUSTED_FILE, "Hello, world!", StandardOpenOption.CREATE);
    }

    /**
     * Access the file system using the {@link Files#copy(InputStream, Path, CopyOption...)} method.
     */
    public static void accessFileSystemViaFilesCopyFromInputStream() throws IOException {

        Path source = Path.of("/tempMethods.txt");
        Path target = NOT_TRUSTED_FILE;
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Access the file system using the {@link Files#copy(Path, Path, CopyOption...)} method.
     */
    public static void accessFileSystemViaFilesCopy() throws IOException {
        Files.copy(NOT_TRUSTED_FILE, NOT_TRUSTED_COPY, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Access the file system using the {@link Files#move(Path, Path, CopyOption...)} method.
     */
    public static void accessFileSystemViaFilesMove() throws IOException {
        // Create a temporary file first to move
        Files.writeString(NOT_TRUSTED_FILE, "This file will be moved", StandardOpenOption.CREATE);

        // Then move it
        Files.move(NOT_TRUSTED_FILE, NOT_TRUSTED_COPY, StandardCopyOption.REPLACE_EXISTING);
    }
}
