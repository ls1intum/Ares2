package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.file.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FileSystemAccessDeleteTest extends SystemAccessTest {

    private static final String FILE_DELETE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete";
    private static final String FILES_DELETE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete";
    private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaFileDelete()">
    @Test
    void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileDeleteOnExit()">
    @Test
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
        // Read, as File.new has the parameter
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesDelete()">
    @Test
    void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesDeleteIfExists()">
    @Test
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    /* Archunit cannot handle third party packages, so we skip this test

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    }*/

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>
}
