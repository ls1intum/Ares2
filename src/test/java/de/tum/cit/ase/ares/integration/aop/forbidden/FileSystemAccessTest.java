package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete.DeleteFileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileSystemProvider.DeleteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.nativeCommand.ExecuteNativeCommandMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.processBuilder.ExecuteProcessBuilderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.bufferedReader.ReadBufferedReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.dataInputStream.ReadDataInputStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileInputStream.ReadFileInputStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileReader.ReadFileReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileSystemProvider.ReadFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStreamReader.ReadInputStreamReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.lineNumberReader.ReadLineNumberReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.nioChannel.ReadNIOChannelMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.objectInputStream.ReadObjectInputStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.printWriter.ReadPrintWriterMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.randomAccessFile.ReadRandomAccessFileMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner.ReadScannerMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.bufferedWriter.WriteBufferedWriterMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.fileWrite.WriteFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.filesWrite.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

class FileSystemAccessTest {

    private static final String ERROR_MESSAGE = "No Security Exception was thrown. Check if the policy is correctly applied.";
    private static final String ERR_SECURITY_EN = "Ares Security Error";
    private static final String ERR_SECURITY_DE = "Ares Sicherheitsfehler";
    private static final String REASON_EN = "(Reason: Student-Code; Stage: Execution)";
    private static final String REASON_DE = "(Grund: Student-Code; Phase: Ausführung)";
    private static final String TRIED_EN = "tried";
    private static final String TRIED_DE = "hat versucht,";
    private static final String BLOCKED_EN = "was blocked by Ares.";
    private static final String BLOCKED_DE = "wurde jedoch von Ares blockiert.";

    //<editor-fold desc="Helpers">

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g., "illegally read from", "illegally write from", "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g., "illegally read from", "illegally write from", "illegally execute from").
     */
    private void assertGeneralErrorMessage(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz
    ) {
        Path expectedPath = Paths.get(
                "src", "test", "java", "de", "tum", "cit", "ase",
                "ares", "integration", "aop", "forbidden", "subject", "nottrusted.txt"
        );
        String nativePath = expectedPath.toString();
        String unixPath = nativePath.replace(expectedPath.getFileSystem().getSeparator(), "/");

        Assertions.assertTrue(
                actualMessage.contains(ERR_SECURITY_EN) || actualMessage.contains(ERR_SECURITY_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        ERR_SECURITY_EN, ERR_SECURITY_DE, actualMessage
                )
        );

        Assertions.assertTrue(
                actualMessage.contains(REASON_EN) || actualMessage.contains(REASON_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        REASON_EN, REASON_DE, actualMessage
                )
        );

        Assertions.assertTrue(
                actualMessage.contains(TRIED_EN) || actualMessage.contains(TRIED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        TRIED_EN, TRIED_DE, actualMessage
                )
        );

        Assertions.assertTrue(
                actualMessage.contains(clazz.getName()),
                () -> String.format(
                        "Exception message should contain '%s', but was:%n%s",
                        clazz.getName(), actualMessage
                )
        );

        Assertions.assertTrue(
                actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                () -> String.format(
                        "Exception message should indicate the operation by containing '%s' or '%s', but was:%n%s",
                        operationTextEN, operationTextDE, actualMessage
                )
        );

        Assertions.assertTrue(
                Arrays.asList(nativePath, unixPath).stream().anyMatch(actualMessage::contains),
                () -> String.format(
                        "Exception message should contain the path '%s' (or '%s'), but was:%n%s",
                        nativePath, unixPath, actualMessage
                )
        );

        Assertions.assertTrue(
                actualMessage.contains(BLOCKED_EN) || actualMessage.contains(BLOCKED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        BLOCKED_EN, BLOCKED_DE, actualMessage
                )
        );
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionWrite(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionExecution(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally execute from", "illegal execute von", clazz);
    }

    /**
     * Helper method for delete operations.
     *
     * @param executable Code that is expected to throw a SecurityException.
     */
    private void assertAresSecurityExceptionDelete(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally delete from", "illegal delete von", clazz);
    }
    //</editor-fold>

    //<editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        //<editor-fold desc="accessFileSystemViaFilesRead">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileRead">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }


        // TODO: reference test case for analysis
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedReader">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader, ReadBufferedReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader, ReadBufferedReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader, ReadBufferedReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader, ReadBufferedReaderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaScanner">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaRandomAccessFile">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile, ReadRandomAccessFileMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile, ReadRandomAccessFileMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile, ReadRandomAccessFileMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile, ReadRandomAccessFileMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaInputStreamReader">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader, ReadInputStreamReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader, ReadInputStreamReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader, ReadInputStreamReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader, ReadInputStreamReaderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider, ReadFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider, ReadFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider, ReadFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider, ReadFileSystemProviderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaNIOChannel">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadNIOChannelMain::accessFileSystemViaNIOChannel, ReadNIOChannelMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadNIOChannelMain::accessFileSystemViaNIOChannel, ReadNIOChannelMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadNIOChannelMain::accessFileSystemViaNIOChannel, ReadNIOChannelMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadNIOChannelMain::accessFileSystemViaNIOChannel, ReadNIOChannelMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileInputStream">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileInputStreamMain::accessFileSystemViaFileInputStream, ReadFileInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileInputStreamMain::accessFileSystemViaFileInputStream, ReadFileInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileInputStreamMain::accessFileSystemViaFileInputStream, ReadFileInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileInputStreamMain::accessFileSystemViaFileInputStream, ReadFileInputStreamMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileReader">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader, ReadFileReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader, ReadFileReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader, ReadFileReaderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader, ReadFileReaderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataInputStream">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadDataInputStreamMain::accessFileSystemViaDataInputStream, ReadDataInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadDataInputStreamMain::accessFileSystemViaDataInputStream, ReadDataInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadDataInputStreamMain::accessFileSystemViaDataInputStream, ReadDataInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadDataInputStreamMain::accessFileSystemViaDataInputStream, ReadDataInputStreamMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectInputStream">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream, ReadObjectInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream, ReadObjectInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream, ReadObjectInputStreamMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream, ReadObjectInputStreamMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadPrintWriterMain::accessFileSystemViaPrintWriter, ReadPrintWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadPrintWriterMain::accessFileSystemViaPrintWriter, ReadPrintWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadPrintWriterMain::accessFileSystemViaPrintWriter, ReadPrintWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadPrintWriterMain::accessFileSystemViaPrintWriter, ReadPrintWriterMain.class);
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Overwrite Operations">
    // --- Overwrite Operations ---
    @Nested
    class OverwriteOperations {

        //<editor-fold desc="accessFileSystemViaFilesWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Execute Operations">
    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        //<editor-fold desc="accessFileSystemViaRuntime">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaProcessBuilder">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaNativeCommand">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecuteOneCommandExecutionAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        //<editor-fold desc="accessFileSystemViaFileDelete">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit, DeleteFileDeleteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesDelete">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, DeleteFilesDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists, DeleteFilesDeleteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider, DeleteFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider, DeleteFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider, DeleteFileSystemProviderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider, DeleteFileSystemProviderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>
    }
    //</editor-fold>
}