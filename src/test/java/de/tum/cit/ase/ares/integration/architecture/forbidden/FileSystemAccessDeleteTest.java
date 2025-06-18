package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.file.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
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

class FileSystemAccessDeleteTest {

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

    // <editor-fold desc="accessFileSystemViaFileDelete">
    @Test
    void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
    void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
    void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenWalaAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
    void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
    void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
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
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
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
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
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
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
    void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit,
                FileDeleteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesDelete">
    @Test
    void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenWalaAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
    void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete, DeleteFilesDeleteMain.class);
    }

    @Test
    void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
    void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
                DeleteFilesDeleteMain.class);
    }

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">

    /* Archunit cannot handle third party packages, so we skip this test

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
    }

    @Disabled
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }*/

    @Test
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
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
        testtest(FileSystemAccessDeleteTest.class, "test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
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
