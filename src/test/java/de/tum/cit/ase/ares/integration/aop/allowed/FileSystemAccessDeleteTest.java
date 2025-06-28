package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete.FileDeleteMain;

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.*;


/**
 * Positive ("allowed") counterpart to the forbidden‑suite. All operations
 * should complete without an Ares SecurityException.
 */
class FileSystemAccessDeleteTest extends SystemAccessTest {
    private static final String FILE_DELETE_WITHIN_PATH  =
            "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete";
    private static final String FILES_DELETE_WITHIN_PATH =
            "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete";
    private static final String THIRD_PARTY_WITHIN_PATH  =
            "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/thirdPartyPackage";

    private static final Path TRUSTED_DIR  = Path.of(
            "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/trusteddir");
    private static final Path TRUSTED_FILE = TRUSTED_DIR.resolve("trusted.txt");
    private static final Path TRUSTED_EMPTY_DIR = TRUSTED_DIR.resolve("tempEmptyDir");


    @BeforeEach
    public void ensureTrustedFileExistsBefore() throws IOException {
        if (Files.notExists(TRUSTED_FILE)) {
            Files.createDirectories(TRUSTED_FILE.getParent());
            Files.createFile(TRUSTED_FILE);
        }
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_DELETE, withinPath = FILE_DELETE_WITHIN_PATH)
    void fileDelete_archunit_aspectj() {
        assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDelete);
    }
}