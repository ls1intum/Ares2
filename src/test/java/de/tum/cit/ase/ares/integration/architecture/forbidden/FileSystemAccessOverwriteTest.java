package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
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

class FileSystemAccessOverwriteTest {

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
    private void assertAresSecurityExceptionWrite(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von", clazz);
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

    // <editor-fold desc="accessFileSystemViaFilesWrite">
    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaFilesWriteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/filesWrite")
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionWrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite, WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedWriter">
    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionWrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionWrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionWrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @Test
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionWrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
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
        testtest(FileSystemAccessOverwriteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
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
