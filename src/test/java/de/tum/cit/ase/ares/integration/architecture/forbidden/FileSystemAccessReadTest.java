package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.fileReader.FileReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.files.FilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

class FileSystemAccessReadTest {

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

    // <editor-fold desc="accessFileSystemViaFilesRead">

    @Test
    void test_accessFileSystemViaFilesReadMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
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
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadAllLines">

    @Test
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadStringCharset">

    @Test
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesLines">

    @Test
    void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/filesRead")
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset, FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesIsRegularFile">

    @Test
    void test_accessFileSystemViaFilesIsRegularFileMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesIsRegularFileMavenArchunitAspectJ");
    }

    @Test
    void test_accessFileSystemViaFilesIsRegularFileMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesIsRegularFileMavenArchunitInstrumentation");
    }

    @Test
    void test_accessFileSystemViaFilesIsRegularFileMavenWalaAspectJ_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesIsRegularFileMavenWalaAspectJ");
    }

    @Test
    void test_accessFileSystemViaFilesIsRegularFileMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessReadTest.class, "test_accessFileSystemViaFilesIsRegularFileMavenWalaInstrumentation");
    }
    // </editor-fold>
}
