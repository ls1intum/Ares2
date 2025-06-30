package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

public class DeleteThroughFileSystemProvider {

    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";


    private DeleteThroughFileSystemProvider() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): DeleteThroughFilesystemProvider is a utility class and should not be instantiated.");
    }
    /**
     * Access the file system using the {@link FileSystemProvider} class for deletion.
     */
    public static void accessFileSystemViaFileSystemProvider() throws IOException {
        FileSystemProvider provider = FileSystemProvider.installedProviders().getFirst();
        provider.delete(Path.of(NOT_TRUSTED_FILE));
    }
}
