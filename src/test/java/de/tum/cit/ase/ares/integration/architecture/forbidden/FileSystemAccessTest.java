package de.tum.cit.ase.ares.integration.architecture.forbidden;

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
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileReader.ReadFileReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.bufferedWriter.WriteBufferedWriterMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.fileWrite.WriteFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.filesWrite.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

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

    // <editor-fold desc="Helpers">

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g.,
     *                        "illegally read from", "illegally write from",
     *                        "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g.,
     *                        "illegally read from", "illegally write from",
     *                        "illegally execute from").
     */
    private void assertGeneralErrorMessage(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {
        Path expectedPath = Paths.get(
                "src", "test", "java", "de", "tum", "cit", "ase",
                "ares", "integration", "aop", "forbidden", "subject", "nottrusted.txt");
        String nativePath = expectedPath.toString();
        String unixPath = nativePath.replace(expectedPath.getFileSystem().getSeparator(), "/");

        Assertions.assertTrue(
                actualMessage.contains(ERR_SECURITY_EN) || actualMessage.contains(ERR_SECURITY_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        ERR_SECURITY_EN, ERR_SECURITY_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(REASON_EN) || actualMessage.contains(REASON_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        REASON_EN, REASON_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(TRIED_EN) || actualMessage.contains(TRIED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        TRIED_EN, TRIED_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(clazz.getName()),
                () -> String.format(
                        "Exception message should contain '%s', but was:%n%s",
                        clazz.getName(), actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                () -> String.format(
                        "Exception message should indicate the operation by containing '%s' or '%s', but was:%n%s",
                        operationTextEN, operationTextDE, actualMessage));

        Assertions.assertTrue(
                Arrays.asList(nativePath, unixPath).stream().anyMatch(actualMessage::contains),
                () -> String.format(
                        "Exception message should contain the path '%s' (or '%s'), but was:%n%s",
                        nativePath, unixPath, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(BLOCKED_EN) || actualMessage.contains(BLOCKED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        BLOCKED_EN, BLOCKED_DE, actualMessage));
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionWrite(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message.
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

    private void testtest(Class<?> clazz, String methodName) {
        Events testEvents = EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter(
                        "junit.jupiter.conditions.deactivate",
                        "org.junit.*DisabledCondition"
                )
                .selectors(selectMethod(clazz, methodName))
                .execute()
                .testEvents();

        testEvents.assertStatistics(stats -> stats.failed(1).aborted(0).succeeded(0));
        testEvents.assertThatEvents().haveExactly(1, event(test(methodName), finishedWithFailure(instanceOf(SecurityException.class))));
    }
    // </editor-fold>

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        // <editor-fold desc="accessFileSystemViaFilesRead">

        @Test
        void test_accessFileSystemViaFilesReadMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFilesReadMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFilesReadMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesReadMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFilesReadMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesReadMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFilesReadMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFilesReadMain::accessFileSystemViaFilesRead, ReadFilesReadMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileRead">
        @Test
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReadMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReadMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReadMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReadMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReadMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead, ReadFileReadMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaBufferedReader - 3 subtests">        // <editor-fold desc="accessFileSystemViaBufferedReader">
        @Test
        void test_accessFileSystemViaBufferedReaderMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaBufferedReaderMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader,
                    ReadBufferedReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedReaderMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaBufferedReaderMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader,
                    ReadBufferedReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedReaderMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaBufferedReaderMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader,
                    ReadBufferedReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedReaderMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaBufferedReaderMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadBufferedReaderMain::accessFileSystemViaBufferedReader,
                    ReadBufferedReaderMain.class);
        }
        // </editor-fold>
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaFileReader (FileReader) - 3 subtests">
        // <editor-fold desc="accessFileSystemViaFileReader">
        @Test
        void test_accessFileSystemViaFileReaderMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReader,
                    ReadFileReaderMain.class);
        }
        // </editor-fold>// <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray,
                    ReadFileReaderMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                    ReadFileReaderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                    ReadFileReaderMain.class);
        }
        // </editor-fold>
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ReadOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Overwrite Operations">
    // --- Overwrite Operations ---
    @Nested
    class OverwriteOperations {

        // <editor-fold desc="accessFileSystemViaFilesWrite">
        @Test
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFilesWriteMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFilesWriteMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFilesWriteMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileWrite">
        @Test
        void test_accessFileSystemViaFileWriteMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFileWriteMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileWriteMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFileWriteMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileWriteMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFileWriteMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileWriteMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaFileWriteMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteFileWriteMain::accessFileSystemViaFileWrite, WriteFileWriteMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaBufferedWriter">
        @Test
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter,
                    WriteBufferedWriterMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter,
                    WriteBufferedWriterMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaBufferedWriterMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter,
                    WriteBufferedWriterMain.class);
        }

        @Test
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteBufferedWriterMain::accessFileSystemViaBufferedWriter,
                    WriteBufferedWriterMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.OverwriteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionWrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Execute Operations">
    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        // <editor-fold desc="accessFileSystemViaRuntime">
        @Test
        void test_accessFileSystemViaRuntimeMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaRuntimeMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        @Test
        void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaRuntimeMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        @Test
        void test_accessFileSystemViaRuntimeMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaRuntimeMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        @Test
        void test_accessFileSystemViaRuntimeMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaRuntimeMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/runtime")
        void test_accessFileSystemViaRuntimeMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteRuntimeMain::accessFileSystemViaRuntime, ExecuteRuntimeMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaProcessBuilder">
        @Test
        void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder,
                    ExecuteProcessBuilderMain.class);
        }

        @Test
        void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder,
                    ExecuteProcessBuilderMain.class);
        }

        @Test
        void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaProcessBuilderMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder,
                    ExecuteProcessBuilderMain.class);
        }

        @Test
        void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/processBuilder")
        void test_accessFileSystemViaProcessBuilderMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteProcessBuilderMain::accessFileSystemViaProcessBuilder,
                    ExecuteProcessBuilderMain.class);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaNativeCommand">
        @Test
        void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaNativeCommandMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand,
                    ExecuteNativeCommandMain.class);
        }

        @Test
        void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand,
                    ExecuteNativeCommandMain.class);
        }

        @Test
        void test_accessFileSystemViaNativeCommandMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaNativeCommandMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand,
                    ExecuteNativeCommandMain.class);
        }

        @Test
        void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaNativeCommandMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/nativeCommand")
        void test_accessFileSystemViaNativeCommandMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteNativeCommandMain::accessFileSystemViaNativeCommand,
                    ExecuteNativeCommandMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.ExecuteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/execute/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionExecution(ExecuteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        // <editor-fold desc="accessFileSystemViaFileDelete">
        @Test
        void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteMavenArchunitAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteMavenWalaAspectJ");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }
        @Test
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteMavenWalaInstrumentation");
        }
        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);        }

        @Test
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                    DeleteFileDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                    DeleteFileDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
            // Read, as File.new has the parameter
            assertAresSecurityExceptionRead(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                    DeleteFileDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                    DeleteFileDeleteMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaFilesDelete">
        @Test
        void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete,                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                    DeleteFilesDeleteMain.class);
        }

        @Test
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                    DeleteFilesDeleteMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaFileSystemProvider">
        @Test
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    DeleteFileSystemProviderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    DeleteFileSystemProviderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    DeleteFileSystemProviderMain.class);
        }

        @Test
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    DeleteFileSystemProviderMain.class);
        }
        // </editor-fold>        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }

        @Test
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
            testtest(FileSystemAccessTest.DeleteOperations.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
        }

        @Disabled
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    ThirdPartyPackagePenguin.class);
        }
        // </editor-fold>
    }
    // </editor-fold>
}