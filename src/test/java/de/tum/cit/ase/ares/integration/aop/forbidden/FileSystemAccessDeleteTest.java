package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.file.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

class FileSystemAccessDeleteTest extends SystemAccessTest {

    private static final String FILE_DELETE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete";
    private static final String FILES_DELETE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaFileDelete">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete,
                FileDeleteMain.class);
    }
    // </editor-fold>

    //<editor-fold desc="accessFileSystemViaFileDeleteOnExit">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesDelete">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesDeleteIfExists">

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesCopy">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesCopy,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesCopy,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesCopy,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesCopy,
                DeleteFilesDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesMove">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenArchunitAspectJ() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesMove,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenArchunitInstrumentation() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesMove,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenWalaAspectJ() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesMove,
                DeleteFilesDeleteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILES_DELETE_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenWalaInstrumentation() {
        assertAresSecurityExceptionExecution(DeleteFilesDeleteMain::accessFileSystemViaFilesMove,
                DeleteFilesDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>
}
