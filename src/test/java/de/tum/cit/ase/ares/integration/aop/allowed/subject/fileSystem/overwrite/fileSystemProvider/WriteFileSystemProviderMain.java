package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileSystemProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;

public class WriteFileSystemProviderMain {

    private WriteFileSystemProviderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link FileSystemProvider} class for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaFileSystemProvider(String text) throws IOException {
        FileSystemProvider provider = FileSystemProvider.installedProviders().getFirst();
        try (OutputStream os = provider.newOutputStream(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            os.write(text.getBytes());
        }
    }
}