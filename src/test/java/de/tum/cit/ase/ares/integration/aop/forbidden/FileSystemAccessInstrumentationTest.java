package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete.DeleteFileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileSystemProvider.DeleteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;

//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FileSystemAccessInstrumentationTest {

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
    //</editor-fold>

    //<editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        //<editor-fold desc="accessFileSystemViaFilesDelete">

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
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
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
            assertAresSecurityExceptionDelete(DeleteFileDeleteMain::accessFileSystemViaFileDelete, DeleteFileDeleteMain.class);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
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
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
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
