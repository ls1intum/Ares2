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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests every JDK delete entry‑point
 */
class FileSystemAccessDeleteTest extends SystemAccessTest {

    private static final String FILE_DELETE_WITHIN_PATH  = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete";
    private static final String FILES_DELETE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete";
    private static final String THIRD_PARTY_WITHIN_PATH  = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage";
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


    /* -------------------------------------------------------------------- */
    /*  java.io.File : delete()                                             */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_archunit_aspectj() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDelete_wala_aspectj() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
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
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_fileDeleteOnExit_wala_aspectj() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
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

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_archunit_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteOnClose::accessFileSystemViaDeleteOnClose, FilesDeleteOnClose.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteOnClose::accessFileSystemViaDeleteOnClose, FilesDeleteOnClose.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_wala_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteOnClose::accessFileSystemViaDeleteOnClose, FilesDeleteOnClose.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_deleteOnClose_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteOnClose::accessFileSystemViaDeleteOnClose, FilesDeleteOnClose.class);
    }

    /* -------------------------------------------------------------------- */
    /*  SecureDirectoryStream.deleteFile() – WALA variants                  */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteFile_wala_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteFile, FilesDeleteSecureDirectory.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteFile_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteFile, FilesDeleteSecureDirectory.class);
    }

    /* -------------------------------------------------------------------- */
    /*  SecureDirectoryStream.deleteDirectory()                             */
    /* -------------------------------------------------------------------- */

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_archunit_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteDirectory, FilesDeleteSecureDirectory.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_archunit_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteDirectory, FilesDeleteSecureDirectory.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_wala_aspectj() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteDirectory, FilesDeleteSecureDirectory.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_sdsDeleteDirectory_wala_instrumentation() {
        assertAresSecurityExceptionDelete(FilesDeleteSecureDirectory::accessFileSystemViaSecureDirectoryStreamDeleteDirectory, FilesDeleteSecureDirectory.class);
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
