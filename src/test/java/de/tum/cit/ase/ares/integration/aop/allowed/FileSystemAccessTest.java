package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileRead.ReadFileReadMain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

/**
 * Integration tests to verify that file system read access is allowed
 * under various security policies and that the file content matches expectations.
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

        /**
         * Helper method to assert that file reading is allowed and the content matches.
         * This method checks that no SecurityException is thrown and the file content is as expected.
         */
        private void assertFileReadAllowedAndContentMatches() {
            String content = Assertions.assertDoesNotThrow(
                ReadFileReadMain::accessFileSystemViaFileRead,
                ERROR_SECURITY_EXCEPTION
            );
            Assertions.assertEquals(TRUSTED_FILE_CONTENT, content.trim(), ERROR_FILE_CONTENT_MISMATCH);
        }

        // <editor-fold desc="accessFileSystemViaFileRead">

        /**
         * Test file read access using Maven + ArchUnit + AspectJ policy.
         */
        @PublicTest
        @Policy(
            value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml"
        )
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches();
        }

        /**
         * Test file read access using Maven + ArchUnit + Instrumentation policy.
         */
        @PublicTest
        @Policy(
            value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml"
        )
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches();
        }

        /**
         * Test file read access using Maven + WALA + AspectJ policy.
         */
        @PublicTest
        @Policy(
            value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml"
        )
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches();
        }

        /**
         * Test file read access using Maven + WALA + Instrumentation policy.
         */
        @PublicTest
        @Policy(
            value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml"
        )
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches();
        }
        // </editor-fold>
    }
    // </editor-fold>
}