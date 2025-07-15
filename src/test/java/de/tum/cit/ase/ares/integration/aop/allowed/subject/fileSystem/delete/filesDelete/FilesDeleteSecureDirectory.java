package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;

public final class FilesDeleteSecureDirectory {

    private static final Path REL_FILE       = Path.of("trusted.txt");
    private static final Path REL_EMPTY_DIR  = Path.of("tempEmptyDir");

    private FilesDeleteSecureDirectory() { throw new SecurityException("utility"); }

    public static void accessFileSystemViaSecureDirectoryStreamDeleteFile(
            SecureDirectoryStream<Path> secureDirectoryStream) throws IOException {
        secureDirectoryStream.deleteFile(REL_FILE);
    }

    public static void accessFileSystemViaSecureDirectoryStreamDeleteDirectory(
            SecureDirectoryStream<Path> secureDirectoryStream) throws IOException {
        secureDirectoryStream.deleteDirectory(REL_EMPTY_DIR);
    }
}
