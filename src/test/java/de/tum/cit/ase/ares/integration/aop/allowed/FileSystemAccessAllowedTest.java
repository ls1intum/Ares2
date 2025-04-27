package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete.AllowedFileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete.AllowedFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileSystemProvider.AllowedFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.jFileChooser.AllowedJFileChooserMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage.AllowedThirdPartyPackageMain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

class FileSystemAccessAllowedTest {

    private final String errorMessage = "Unexpected security exception when deleting trusted.txt";

    @Nested
    class DeleteOperations {

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileDeleteMavenArchunitAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFileDeleteMain::accessFileSystemViaFileDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileDeleteMavenArchunitInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFileDeleteMain::accessFileSystemViaFileDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileDeleteMavenWalaAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFileDeleteMain::accessFileSystemViaFileDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileDeleteMavenWalaInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFileDeleteMain::accessFileSystemViaFileDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFilesDeleteMavenArchunitAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFilesDeleteMain::accessFileSystemViaFilesDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFilesDeleteMavenArchunitInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFilesDeleteMain::accessFileSystemViaFilesDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFilesDeleteMavenWalaAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFilesDeleteMain::accessFileSystemViaFilesDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFilesDeleteMavenWalaInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFilesDeleteMain::accessFileSystemViaFilesDelete,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileSystemProviderMavenArchunitAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileSystemProviderMavenArchunitInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileSystemProviderMavenWalaAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedFileSystemProviderMavenWalaInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedFileSystemProviderMain::accessFileSystemViaFileSystemProvider,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedJFileChooserMavenArchunitAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedJFileChooserMain::accessFileSystemViaJFileChooser,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedJFileChooserMavenArchunitInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedJFileChooserMain::accessFileSystemViaJFileChooser,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedJFileChooserMavenWalaAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedJFileChooserMain::accessFileSystemViaJFileChooser,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedJFileChooserMavenWalaInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedJFileChooserMain::accessFileSystemViaJFileChooser,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedThirdPartyPackageMavenArchunitAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedThirdPartyPackageMavenArchunitInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedThirdPartyPackageMavenWalaAspectJ() {
            Assertions.assertDoesNotThrow(
                    AllowedThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    errorMessage
            );
        }

        @PublicTest
        @Policy(
                value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml",
                withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject"
        )
        void test_allowedThirdPartyPackageMavenWalaInstrumentation() {
            Assertions.assertDoesNotThrow(
                    AllowedThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                    errorMessage
            );
        }
    }
}
