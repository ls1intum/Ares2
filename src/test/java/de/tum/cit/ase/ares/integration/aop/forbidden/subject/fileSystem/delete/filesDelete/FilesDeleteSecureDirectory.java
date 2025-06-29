package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;

public final class FilesDeleteSecureDirectory {


    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";
    private static final String NOT_TRUSTED_FILE_DIR = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir";
    private static final String NOT_TRUSTED_FILE_DIR_CHILD_DIR = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/tempEmptyDir";


    private FilesDeleteSecureDirectory() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): "
                        + "Main is a utility class and should not be instantiated.");
    }

    /** {@link SecureDirectoryStream#deleteFile}} */
    public static void accessFileSystemViaSecureDirectoryStreamDeleteFile(SecureDirectoryStream<Path> secureDirectoryStream) throws IOException {
        Path dir  = Path.of(NOT_TRUSTED_FILE_DIR);
        Path file = Path.of(NOT_TRUSTED_FILE);
        if (secureDirectoryStream != null) {
            secureDirectoryStream.deleteFile(file);
        } else {
            Files.delete(dir.resolve(file));
        }
    }

    /** {@link SecureDirectoryStream#deleteDirectory)} */
    public static void accessFileSystemViaSecureDirectoryStreamDeleteDirectory(SecureDirectoryStream<Path> secureDirectoryStream) throws IOException {
        Path subDir  = Path.of(NOT_TRUSTED_FILE_DIR_CHILD_DIR);

        if (secureDirectoryStream != null) {
            secureDirectoryStream.deleteDirectory(subDir);
        } else {
            Files.delete(subDir);
        }
    }
}
