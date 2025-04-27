package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete.DeleteFileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileSystemProvider.DeleteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.desktop.ExecuteDesktopMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.jFileChooser.ExecuteJFileChooserMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.nativeCommand.ExecuteNativeCommandMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.processBuilder.ExecuteProcessBuilderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.runtime.ExecuteRuntimeMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.execute.thirdPartyPackage.ExecuteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.bufferedReader.ReadBufferedReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.bufferedWriter.WriteBufferedWriterMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.fileWrite.WriteFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.filesWrite.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.write.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;

class FileSystemAccessTest {

    private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g., "illegally read from", "illegally write from", "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g., "illegally read from", "illegally write from", "illegally execute from").
     */
    private void assertGeneralErrorMessage(String actualMessage, String operationTextEN, String operationTextDE, Class<?> clazz) {
        Assertions.assertTrue(actualMessage.contains("Ares Security Error") || actualMessage.contains("Ares Sicherheitsfehler"),
                "Exception message should contain 'Ares Security Error' or 'Ares Sicherheitsfehler', but is:" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains("(Reason: Student-Code; Stage: Execution)") || actualMessage.contains("(Grund: Student-Code; Phase: Ausführung)"),
                "Exception message should contain '(Reason: Student-Code; Stage: Execution)' or '(Grund: Student-Code; Phase: Ausführung)', but is:" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains("has tried,") || actualMessage.contains("hat versucht,"),
                "Exception message should contain 'has tried,' or 'hat versucht,' but is:" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains(clazz.getName()),
                "Exception message should contain '" + clazz.getName() + "', but is:" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                "Exception message should indicate the expected operation by containing '" + operationTextEN + "'" + System.lineSeparator() + actualMessage + "or '" + operationTextDE + "'" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/nottrusted.txt"),
                "Exception message should contain 'src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/nottrusted.txt', but is:" + System.lineSeparator() + actualMessage);
        Assertions.assertTrue(actualMessage.contains("was blocked by Ares.") || actualMessage.contains("wurde jedoch von Ares blockiert."),
                "Exception message should contain 'but was blocked by Ares.' or 'wurde jedoch von Ares blockiert.', but is:" + System.lineSeparator() + actualMessage);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionWrite(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionExecution(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally execute from", "illegal execute von", clazz);
    }

    /**
     * Helper method for delete operations.
     *
     * @param executable        Code that is expected to throw a SecurityException.
     */
    private void assertAresSecurityExceptionDelete(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally delete from", "illegal delete von", clazz);
    }

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
    }

    // --- Write Operations ---
    @Nested
    class WriteOperations {

        //<editor-fold desc="accessFileSystemViaFilesWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter, WriteBufferedWriterMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/write/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>
    }

    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        //<editor-fold desc="accessFileSystemViaRuntime">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaProcessBuilder">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder, ExecuteProcessBuilderMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaNativeCommand">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand, ExecuteNativeCommandMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ThirdPartyPackagePenguin.class);
        }
        //</editor-fold>
    }

    //<editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

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
        //</editor-fold>

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