package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;

public final class FilesDeleteSecureDirectory {


    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";
    private static final String NOT_TRUSTED_FILE_DIR = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir";


    private FilesDeleteSecureDirectory() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): "
                        + "Main is a utility class and should not be instantiated.");
    }

    /** {@link SecureDirectoryStream#deleteFile}} */
    public static void accessFileSystemViaSecureDirectoryStreamDeleteFile() throws IOException {
        Path dir  = Path.of(NOT_TRUSTED_FILE_DIR);
        Path file = Path.of(NOT_TRUSTED_FILE);                          // relative to dir

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            if (ds instanceof SecureDirectoryStream<Path> sds) {
                sds.deleteFile(file);                                   // constant-time, race-safe
            } else {
                // Provider doesn’t support SDS → fall back.
                Files.delete(dir.resolve(file));
            }
        }
    }

    /** {@link SecureDirectoryStream#deleteDirectory)} */
    public static void accessFileSystemViaSecureDirectoryStreamDeleteDirectory() throws IOException {
        Path dir     = Path.of(NOT_TRUSTED_FILE_DIR);
        Path subDir  = Path.of("tempEmptyDir");

        Files.createDirectories(dir.resolve(subDir));                   // ensure it exists

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            if (ds instanceof SecureDirectoryStream<Path> sds) {
                sds.deleteDirectory(subDir);                            // must be empty
            } else {
                Files.delete(dir.resolve(subDir));
            }
        }
    }
}
