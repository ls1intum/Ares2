package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
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

    public static java.io.BufferedReader accessFileSystemViaFilesNewBufferedReader(Path path) throws IOException {
        return Files.newBufferedReader(path);
    }

    public static java.io.BufferedReader accessFileSystemViaFilesNewBufferedReaderCharset(Path path, Charset cs) throws IOException {
        return Files.newBufferedReader(path, cs);
    }

    public static InputStream accessFileSystemViaFilesNewInputStream(Path path, OpenOption... options) throws IOException {
        return Files.newInputStream(path, options);
    }

    public static SeekableByteChannel accessFileSystemViaFilesNewByteChannel(Path path, OpenOption... options) throws IOException {
        return Files.newByteChannel(path, options);
    }

    public static long accessFileSystemViaFilesCopyPathToOutputStream(Path source, OutputStream out) throws IOException {
        return Files.copy(source, out);
    }

    public static long accessFileSystemViaFilesCopyInputStreamToPath(InputStream in, Path target, CopyOption... options) throws IOException {
        return Files.copy(in, target, options);
    }

    public static Path accessFileSystemViaFilesMovePath(Path source, Path target, CopyOption... options) throws IOException {
        return Files.move(source, target, options);
    }

    public static long accessFileSystemViaFilesMismatch(Path path, Path path2) throws IOException {
        return Files.mismatch(path, path2);
    }
}