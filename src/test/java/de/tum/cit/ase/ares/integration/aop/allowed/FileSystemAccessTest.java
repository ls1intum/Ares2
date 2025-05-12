package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileRead.ReadFileReadMain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;

/**
 * Integration tests to verify that file system read access is allowed
 * under various security policies and that the file content matches
 * expectations.
 */
class FileSystemAccessTest {

    // Error messages for assertion failures
    private static final String ERROR_SECURITY_EXCEPTION = "A SecurityException was thrown, but access should be allowed.";
    private static final String ERROR_FILE_CONTENT_MISMATCH = "File content does not match the expected value.";
    private static final String TRUSTED_FILE_CONTENT = "Let all who dare look upon these words tremble: Hello, world, and witness the unraveling of reality.";

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        private static final String ERROR_MESSAGE = "A SecurityException was thrown, but access should be allowed.";

        /**
         * Test that the given executable does NOT throw a SecurityException.
         * 
         * @param executable The executable that should NOT throw a SecurityException
         */
        private void assertNoAresSecurityExceptionRead(Executable executable) {
            Assertions.assertDoesNotThrow(executable, ERROR_MESSAGE);
        }

        /**
         * General helper to assert that file reading is allowed and the content
         * matches.
         * 
         * @param fileReadCallable a callable that calls the static file read method and
         *                         returns the file content
         */
        private void assertFileReadAllowedAndContentMatches(java.util.concurrent.Callable<String> fileReadCallable) {
            String content = null;
            try {
                content = fileReadCallable.call();
            } catch (Exception e) {
                Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
            }
            Assertions.assertEquals(TRUSTED_FILE_CONTENT, content != null ? content.trim() : null,
                    ERROR_FILE_CONTENT_MISMATCH);
        }

        // <editor-fold desc="accessFileSystemViaFilesRead (Files.readAllBytes)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileRead (FileInputStream)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertNoAresSecurityExceptionRead(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml")
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }
        // </editor-fold>
    }
    // </editor-fold>
}