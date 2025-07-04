package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class FileSystemAccessOverwriteTest extends SystemAccessTest {


    private static final Path NOT_TRUSTED_DIR = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir");
    private static final Path NOT_TRUSTED_GZ = Path.of(NOT_TRUSTED_DIR + "/nottrusted.txt.gz");
    private static final Path NOT_TRUSTED_FILE_PATH = Path.of(NOT_TRUSTED_DIR + "/nottrusted.txt");


    private static final String FILES_WRITE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files";
    private static final String BUFFERED_WRITER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter";
    private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage";


    // <editor-fold desc="accessFileSystemViaFilesWrite">
    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedWriter">
    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation");
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }
    // </editor-fold>


    // ===================== DISABLED TESTS BELOW =====================

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_WRITE_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = BUFFERED_WRITER_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class, NOT_TRUSTED_FILE_PATH);
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class, NOT_TRUSTED_FILE_PATH);
    }
}
