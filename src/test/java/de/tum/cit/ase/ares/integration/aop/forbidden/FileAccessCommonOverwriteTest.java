package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


class FileAccessCommonOverwriteTest extends SystemAccessTest {

    private static final String FILES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files";
    private static final Path NOT_TRUSTED_FILE_PATH = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted.txt");
    private static final Path NOT_TRUSTED_COPY_PATH  = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir/nottrusted-copy.txt");
    private static final Path NOT_TRUSTED_DIR = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir");
    private static final String BUFFERED_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter";


    @BeforeEach
    void ensureNotTrustedFilesExist() throws IOException {
        Files.createDirectories(NOT_TRUSTED_DIR);
        if (Files.notExists(NOT_TRUSTED_COPY_PATH)) {
            Files.createFile(NOT_TRUSTED_COPY_PATH);
        }
        if( Files.notExists(NOT_TRUSTED_FILE_PATH)) {
            Files.createFile(NOT_TRUSTED_FILE_PATH);
        }
    }
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }


    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }


    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }




    @AfterEach
    void cleanUpNotTrustedFiles() throws IOException {
        Files.deleteIfExists(NOT_TRUSTED_DIR.resolve(NOT_TRUSTED_COPY_PATH));
        Files.deleteIfExists(NOT_TRUSTED_DIR.resolve(NOT_TRUSTED_FILE_PATH));

         try (var stream = Files.list(NOT_TRUSTED_DIR)) {
            if (stream.findAny().isEmpty()) {
                Files.delete(NOT_TRUSTED_DIR);
            }
        }
    }
}
