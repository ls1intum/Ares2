package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.reader.fileReader.FileReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.files.FilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FileSystemAccessReadTest extends SystemAccessTest {
    private static final String FILE_READER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader";
    private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage";
    private static final String FILES_READ_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead";
    
    // Additional policy constants for specific test cases
    private static final String WALA_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml";
    private static final String ARCHUNIT_ASPECTJ_POLICY_EVERYTHING_FORBIDDEN = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml";
    private static final String ARCHUNIT_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml";

    // <editor-fold desc="accessFileSystemViaFilesRead">

    @Test
    public void test_accessFileSystemViaFilesReadMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    @Test
    public void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
    public void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @Test
    public void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN, withinPath = THIRD_PARTY_PACKAGE_PATH)
    public void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_EVERYTHING_FORBIDDEN, withinPath = FILE_READER_PATH)
    public void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadAllLines">

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadAllLinesCharset">

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadStringCharset">

    @Test
    public void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesLines">

    @Test
    public void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesLinesCharset">

    @Test
    public void test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    public void test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation_test() {
        executeTestAndExpectSecurityException(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_READ_PATH)
    public void test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }}
