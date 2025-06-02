package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileSystemProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public class DeleteFileSystemProviderMain {

    private DeleteFileSystemProviderMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link FileSystemProvider} class for deletion.
     */
    public static void accessFileSystemViaFileSystemProvider() throws IOException {
        FileSystemProvider provider = FileSystemProvider.installedProviders().getFirst();
        provider.delete(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
    }
}
