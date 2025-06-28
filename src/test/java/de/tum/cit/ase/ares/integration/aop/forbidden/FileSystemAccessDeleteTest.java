package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.FilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.FilesDeleteOnClose;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.FilesDeleteSecureDirectory;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

/**
 * Tests every JDK delete entry‑point
 */
class FileSystemAccessDeleteTest extends SystemAccessTest {

    private static final String FILE_DELETE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete";
    private static final String FILES_DELETE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete";
    private static final String THIRD_PARTY_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage";
    private static final Path NOT_TRUSTED_DIR = Path.of("src/test/java/…/fileSystem/delete/nottrusteddir");
    private static final Path NOT_TRUSTED_FILE_PATH = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt");


    /**
     * Ensure the test artefact exists before each test so that deletion calls
     * have a real target. The directory is created if missing; the file is
     * (re)‑created empty if it was removed by a previous test.
     */
    @BeforeEach
    void createNotTrustedFile() throws IOException {
        Files.createDirectories(NOT_TRUSTED_FILE_PATH.getParent());
        if (Files.notExists(NOT_TRUSTED_FILE_PATH)) {
            Files.createFile(NOT_TRUSTED_FILE_PATH);
        }
    }

    private SecureDirectoryStream<Path> openSecureDirectoryStream() throws IOException {
        DirectoryStream<Path> ds = Files.newDirectoryStream(NOT_TRUSTED_DIR);
        if (ds instanceof SecureDirectoryStream<Path> secureDirectoryStream) return secureDirectoryStream;
        throw new UnsupportedOperationException("FS lacks secureDirectoryStream support");
    }


    /* -------------------------------------------------------------------- */
    /*  java.io.File : delete()                                             */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_archunit_aspectj() {
        // This method of deleting files requires a read  before, hence we test for read related errors.
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_wala_aspectj() {
        // This method of deleting files requires a read  before, hence we test for read related errors.
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class,
                NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    /* -------------------------------------------------------------------- */
    /*  java.io.File : deleteOnExit()                                       */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_archunit_aspectj() {
        // This method of deleting files requires a read  before, hence we test for read violation.
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_wala_aspectj() {
        // This method of deleting files requires a read  before, hence we test for read violation.
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
    }

    /* -------------------------------------------------------------------- */
    /*  Path → File → delete()                                              */
    /* -------------------------------------------------------------------- */


    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_pathToFileDelete_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaPathToFileDelete, FileDeleteMain.class);
    }

    @Disabled("This test is disabled because aspectj does not test for parameterless methods ")
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_pathToFileDelete_wala_aspectj() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaPathToFileDelete, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_pathToFileDelete_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaPathToFileDelete, FileDeleteMain.class);
    }

    /* -------------------------------------------------------------------- */
    /*  java.nio.file.Files : delete(Path)                                  */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDelete_archunit_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDelete_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDelete_wala_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDelete_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
    }

    /* -------------------------------------------------------------------- */
    /*  java.nio.file.Files : deleteIfExists(Path)                          */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDeleteIfExists_archunit_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDeleteIfExists_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDeleteIfExists_wala_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, FilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_filesDeleteIfExists_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, FilesDeleteMain.class);
    }

    /* -------------------------------------------------------------------- */
    /*  DELETE_ON_CLOSE via Channels                                        */
    /* -------------------------------------------------------------------- */

    @Disabled("This test is disabled because AspectJ does not test for parameterless methods.")
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_archunit_aspectj() {
        assertAresSecurityExceptionDelete(() -> {
            try (java.nio.channels.SeekableByteChannel ch =
                         java.nio.file.Files.newByteChannel(
                                 NOT_TRUSTED_FILE_PATH,
                                 java.nio.file.StandardOpenOption.CREATE,
                                 java.nio.file.StandardOpenOption.WRITE,
                                 java.nio.file.StandardOpenOption.DELETE_ON_CLOSE)) {

                FilesDeleteOnClose.closeChannelToDeleteFileInChannel(ch);
            }
        }, FilesDeleteOnClose.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(() -> {
            try (java.nio.channels.SeekableByteChannel ch =
                         java.nio.file.Files.newByteChannel(
                                 NOT_TRUSTED_FILE_PATH,
                                 java.nio.file.StandardOpenOption.CREATE,
                                 java.nio.file.StandardOpenOption.WRITE,
                                 java.nio.file.StandardOpenOption.DELETE_ON_CLOSE)) {

                FilesDeleteOnClose.closeChannelToDeleteFileInChannel(ch);
            }
        }, FilesDeleteOnClose.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_wala_aspectj() {
        assertAresSecurityExceptionDelete(() -> {
            try (java.nio.channels.SeekableByteChannel ch =
                         java.nio.file.Files.newByteChannel(
                                 NOT_TRUSTED_FILE_PATH,
                                 java.nio.file.StandardOpenOption.CREATE,
                                 java.nio.file.StandardOpenOption.WRITE,
                                 java.nio.file.StandardOpenOption.DELETE_ON_CLOSE)) {

                FilesDeleteOnClose.closeChannelToDeleteFileInChannel(ch);
            }
        }, FilesDeleteOnClose.class);
    }


    @Disabled("This test is disabled because testing in this manner is not possible. The trusted code opens the channel and closing it is seen as allowed delete." +
            "Alternatively, creating the channel in the student code would violate read and not really test deleteOnClose()")
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_wala_instrumentation() {
        assertAresSecurityExceptionDelete(() -> {

            // Trusted test code opens the channel
            SeekableByteChannel ch = Files.newByteChannel(
                    NOT_TRUSTED_FILE_PATH,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.DELETE_ON_CLOSE);

            // student code triggers the delete
            FilesDeleteOnClose.closeChannelToDeleteFileInChannel(ch);

        }, FilesDeleteOnClose.class);
    }

    /* -------------------------------------------------------------------- */
    /*  SecureDirectoryStream.deleteFile() – WALA variants                  */
    /* -------------------------------------------------------------------- */

    @Disabled("This test is disabled because AspectJ does not test for parameterless methods.")
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteFile_wala_aspectj() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteFile(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }


    @Disabled("This test is disabled because testing in this manner is not possible. The trusted code opens the stream and closing it is seen as allowed delete." +
            "Alternatively, creating the stream in the student code would violate read and not really test secureDirectoryStream.deleteFile()")
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteFile_wala_instrumentation() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteDirectory(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }

    /* -------------------------------------------------------------------- */
    /*  SecureDirectoryStream.deleteDirectory()                             */
    /* -------------------------------------------------------------------- */


    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_archunit_aspectj() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteDirectory(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }


    @Disabled("This test is disabled because testing in this manner is not possible. The trusted code opens the stream and closing it is seen as allowed delete." +
            "Alternatively, creating the stream in the student code would violate read and not really test secureDirectoryStream.deleteDirectory()")
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteDirectory(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }


    @Disabled("This test is disabled because testing in this manner is not possible. The trusted code opens the stream and closing it is seen as allowed delete." +
            "Alternatively, creating the stream in the student code would violate read and not really test secureDirectoryStream.deleteDirectory()")
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_wala_aspectj() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteDirectory(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }


    @Disabled("This test is disabled because testing in this manner is not possible. The trusted code opens the stream and closing it is seen as allowed delete." +
            "Alternatively, creating the stream in the student code would violate read and not really test secureDirectoryStream.deleteDirectory()")
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE,
            withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_wala_instrumentation() {
        assertAresSecurityExceptionDelete(() -> {
            try (SecureDirectoryStream<Path> secureDirectoryStream = openSecureDirectoryStream()) {
                FilesDeleteSecureDirectory.accessFileSystemViaSecureDirectoryStreamDeleteDirectory(secureDirectoryStream);
            }
        }, FilesDeleteSecureDirectory.class);
    }

    /* -------------------------------------------------------------------- */
    /*  Third‑party wrapper call                                            */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_WITHIN_PATH)
    void test_thirdPartyDelete_archunit_aspectj() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_WITHIN_PATH)
    void test_thirdPartyDelete_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_WITHIN_PATH)
    void test_thirdPartyDelete_wala_aspectj() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_WITHIN_PATH)
    void test_thirdPartyDelete_wala_instrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
    }
}
