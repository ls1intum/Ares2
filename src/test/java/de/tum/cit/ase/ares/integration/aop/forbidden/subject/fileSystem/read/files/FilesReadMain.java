package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FilesReadMain {

    private FilesReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static byte[] accessFileSystemViaFilesRead() throws IOException {
        return Files.readAllBytes(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static List<String> accessFileSystemViaFilesReadAllLines() throws IOException {
        return Files.readAllLines(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static List<String> accessFileSystemViaFilesReadAllLinesCharset() throws IOException {
        return Files.readAllLines(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8);
    }

    public static String accessFileSystemViaFilesReadString() throws IOException {
        return Files.readString(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static String accessFileSystemViaFilesReadStringCharset() throws IOException {
        return Files.readString(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8);
    }

    public static Stream<String> accessFileSystemViaFilesLines() throws IOException {
        return Files.lines(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static Stream<String> accessFileSystemViaFilesLinesCharset() throws IOException {
        return Files.lines(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8);
    }

    public static java.io.BufferedReader accessFileSystemViaFilesNewBufferedReaderCharset() throws IOException {
        return Files.newBufferedReader(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8);
    }

    public static java.io.BufferedReader accessFileSystemViaFilesNewBufferedReader() throws IOException {
        return Files.newBufferedReader(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static InputStream accessFileSystemViaFilesNewInputStream() throws IOException {
        return Files.newInputStream(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static SeekableByteChannel accessFileSystemViaFilesNewByteChannel() throws IOException {
        return Files.newByteChannel(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }

    public static long accessFileSystemViaFilesCopyPathToOutputStream() throws IOException {
        return Files.copy(
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
            new java.io.ByteArrayOutputStream()
        );
    }

    public static long accessFileSystemViaFilesCopyInputStreamToPath() throws IOException {
        return Files.copy(
            new java.io.ByteArrayInputStream("test".getBytes()),
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")
        );
    }

    public static Path accessFileSystemViaFilesMovePath() throws IOException {
        return Files.move(
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted_moved.txt")
        );
    }

    public static long accessFileSystemViaFilesMismatch() throws IOException {
        return Files.mismatch(
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
            Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")
        );
    }
}