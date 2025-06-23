package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FileSystemAccessOverwriteTest extends SystemAccessTest {

    private static final String FILES_WRITE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite";
    private static final String BUFFERED_WRITER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter";
    private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaFilesWrite">
    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedWriter">
    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>
}
