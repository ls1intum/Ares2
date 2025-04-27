package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileSystemProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public class AllowedFileSystemProviderMain {

    private AllowedFileSystemProviderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using FileSystemProvider for deletion of trusted.txt.
     */
    public static void accessFileSystemViaFileSystemProvider() throws IOException {
        FileSystemProvider provider = FileSystemProvider.installedProviders().get(0);
        provider.delete(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")
        );
    }
}
