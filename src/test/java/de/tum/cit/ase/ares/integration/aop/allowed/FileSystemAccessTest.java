package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

// Imports for read operations
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.bufferedReader.ReadBufferedReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.inputStreamReader.ReadInputStreamReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileSystemProvider.ReadFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.nioChannel.ReadNIOChannelMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.randomAccessFile.ReadRandomAccessFileMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.scanner.ReadScannerMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;

// Imports for overwrite operations
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.bufferedWriter.WriteBufferedWriterMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileOutputStream.WriteFileOutputStreamMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileSystemProvider.WriteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileWrite.WriteFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.fileWriter.WriteFileWriterMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.filesWrite.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.nioChannel.WriteNIOChannelMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.printWriter.WritePrintWriterMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;

// Imports for delete operations
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete.DeleteFileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete.DeleteFilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileSystemProvider.DeleteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

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

    /**
     * Test that the given executable does NOT throw a SecurityException.
     *
     * @param executable The executable that should NOT throw a SecurityException
     */
    private void assertNoAresSecurityException(Executable executable) {
        Assertions.assertDoesNotThrow(executable, ERROR_SECURITY_EXCEPTION);
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

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        // <editor-fold desc="accessFileSystemViaFilesRead (Files.readAllBytes)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/filesRead")
        void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFilesReadMain::accessFileSystemViaFilesRead);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileRead (FileInputStream)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitAspectJ() {
            assertNoAresSecurityException(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileRead")
        void test_accessFileSystemViaFileReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReadMain::accessFileSystemViaFileRead);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaBufferedReader (BufferedReader.readLine)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReader);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaInputStreamReader (InputStreamReader.read)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/inputStreamReader")
        void test_accessFileSystemViaInputStreamReaderMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadInputStreamReaderMain::accessFileSystemViaInputStreamReader);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileSystemProvider (FileSystemProvider)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaJFileChooser (JFileChooser)">
        /* @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadJFileChooserMain::accessFileSystemViaJFileChooser);
        } */
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaNIOChannel (FileChannel)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadNIOChannelMain::accessFileSystemViaNIOChannel);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadNIOChannelMain::accessFileSystemViaNIOChannel);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadNIOChannelMain::accessFileSystemViaNIOChannel);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadNIOChannelMain::accessFileSystemViaNIOChannel);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaRandomAccessFile (RandomAccessFile)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/randomAccessFile")
        void test_accessFileSystemViaRandomAccessFileMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadRandomAccessFileMain::accessFileSystemViaRandomAccessFile);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaScanner (Scanner.nextLine)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadScannerMain::accessFileSystemViaScanner);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadScannerMain::accessFileSystemViaScanner);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadScannerMain::accessFileSystemViaScanner);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/scanner")
        void test_accessFileSystemViaScannerMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadScannerMain::accessFileSystemViaScanner);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaThirdPartyPackage (ThirdPartyPackageClass)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Overwrite Operations">
    // --- Overwrite Operations ---
    @Nested
    class OverwriteOperations {

        // <editor-fold desc="accessFileSystemViaBufferedWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileOutputStream">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileSystemProvider">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteFileWriteMain.accessFileSystemViaFileWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileWriteMain.accessFileSystemViaFileWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteFileWriteMain.accessFileSystemViaFileWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWrite")
        void test_accessFileSystemViaFileWriteMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileWriteMain.accessFileSystemViaFileWrite(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFilesWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaJFileChooser">
        /* @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteJFileChooserMain.accessFileSystemViaJFileChooser(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteJFileChooserMain.accessFileSystemViaJFileChooser(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteJFileChooserMain.accessFileSystemViaJFileChooser(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteJFileChooserMain.accessFileSystemViaJFileChooser(TRUSTED_FILE_CONTENT));
        } */
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaNIOChannel">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaPrintWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertNoAresSecurityException(() -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertNoAresSecurityException(() -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertNoAresSecurityException(() -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertNoAresSecurityException(() -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        private static final Path TRUSTED_FILE_PATH =
                Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");

        @BeforeEach
        public void ensureTrustedFileExistsBefore() throws IOException {
            if (Files.notExists(TRUSTED_FILE_PATH)) {
                Files.createDirectories(TRUSTED_FILE_PATH.getParent());
                Files.createFile(TRUSTED_FILE_PATH);
            }
        }

        @AfterEach
        public void ensureTrustedFileExistsAfter() throws IOException {
            if (Files.notExists(TRUSTED_FILE_PATH)) {
                Files.createDirectories(TRUSTED_FILE_PATH.getParent());
                Files.createFile(TRUSTED_FILE_PATH);
            }
        }

        // <editor-fold desc="accessFileSystemViaFileDelete">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteFileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFilesDelete">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteFilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileSystemProvider">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteFileSystemProviderMain::accessFileSystemViaFileSystemProvider);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaJFileChooser">
        /* @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteJFileChooserMain::accessFileSystemViaJFileChooser);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/jFileChooser")
        void test_accessFileSystemViaJFileChooserMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteJFileChooserMain::accessFileSystemViaJFileChooser);
        } */
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertNoAresSecurityException(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertNoAresSecurityException(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertNoAresSecurityException(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertNoAresSecurityException(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage);
        }
        // </editor-fold>
    }
    // </editor-fold>
}