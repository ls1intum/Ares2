package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.bufferedReader.ReadBufferedReaderMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;

class FileSystemAccessTest {

    private static final String ERROR_MESSAGE = "A SecurityException was thrown, but access should be allowed.";

    /**
     * Test that the given executable does NOT throw a SecurityException.
     * 
     * @param executable The executable that should NOT throw a SecurityException
     */
    private void assertNoAresSecurityExceptionRead(Executable executable) {
        Assertions.assertDoesNotThrow(executable, ERROR_MESSAGE);
    }

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        // <editor-fold desc="accessFileSystemViaFileRead">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertNoAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertNoAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertNoAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertNoAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead);
        }
        // </editor-fold>
    }
    // </editor-fold>

}