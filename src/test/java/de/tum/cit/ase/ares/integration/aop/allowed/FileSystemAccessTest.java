package de.tum.cit.ase.ares.integration.aop.allowed;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

// Imports for read operations
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.filesRead.ReadFilesReadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileRead.ReadFileReadMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileInputStream.ReadFileInputStreamMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileReader.ReadFileReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.bufferedReader.ReadBufferedReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.dataInputStream.ReadDataInputStreamMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.inputStreamReader.ReadInputStreamReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.lineNumberReader.ReadLineNumberReaderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileSystemProvider.ReadFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.nioChannel.ReadNIOChannelMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.objectInputStream.ReadObjectInputStreamMain;
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
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileDelete.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete.FilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.fileSystemProvider.DeleteFileSystemProviderMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;

// Imports for execute operations
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.desktop.ExecuteDesktopMain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * Executes the platform-specific trusted script and verifies console output.
     */
    private String returnPlatformSpecificTrustedScriptPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows")
                ? "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted_execute.bat"
                : "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted_execute.sh";
    }

    /**
     * Sets up the input file with the expected trusted content for read tests.
     *
     * @param inputFilePath The path to the input file
     */
    private void setupInputFile(String inputFilePath) {
        try {
            java.nio.file.Files.writeString(java.nio.file.Paths.get(inputFilePath), TRUSTED_FILE_CONTENT);
        } catch (java.io.IOException e) {
            Assertions.fail("Failed to setup input file: " + e.getMessage(), e);
        }
    }

    /**
     * Cleans up the output file if it exists.
     */
    private void cleanupOutputFile(String outputFilePath) {
        java.io.File outputFile = new java.io.File(outputFilePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    /**
     * Verifies that the output file was created and contains the expected content.
     */
    private void verifyOutputFileCreatedWithCorrectContent(String outputFilePath) throws java.io.IOException {
        java.io.File outputFile = new java.io.File(outputFilePath);

        // Verify file exists
        Assertions.assertTrue(outputFile.exists(),
                "Output file was not created at: " + outputFilePath);

        // Verify file content
        String content = java.nio.file.Files.readString(java.nio.file.Paths.get(outputFilePath)).trim();
        Assertions.assertEquals(TRUSTED_FILE_CONTENT, content,
                "Output file content does not match the expected trusted file content.");
    }


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
        // Define the input file path
        String inputFilePath = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";
        
        try {
            // Set up the input file with expected content
            setupInputFile(inputFilePath);
            
            // Call the file read method
            String content = fileReadCallable.call();
            
            // Verify that the reading was correct
            Assertions.assertEquals(TRUSTED_FILE_CONTENT, content != null ? content.trim() : null,
                    ERROR_FILE_CONTENT_MISMATCH);
        } catch (SecurityException e) {
            Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
        } catch (Exception e) {
            Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
        }
    }    /**
     * Helper method to assert that execution is allowed and creates
     * the expected output file with correct content.
     *
     * @param executeExecutable an executable that calls the static execute method
     */
    private void assertExecuteAllowedWithFileCreation(Executable executeExecutable) {
        // Define the output file path (in the same directory as the scripts)
        String outputFilePath = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted_output.txt";

        try {
            // Clean up any existing output file before test
            cleanupOutputFile(outputFilePath);

            // Execute the method (this should not throw SecurityException and should create
            // the file)
            executeExecutable.execute();

            // Verify the file was created and has correct content
            verifyOutputFileCreatedWithCorrectContent(outputFilePath);

        } catch (SecurityException e) {
            Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
        } catch (Throwable e) {
            Assertions.fail("Unexpected exception during execution: " + e.getMessage(), e);
        } finally {
            // Clean up the output file after test
            cleanupOutputFile(outputFilePath);
        }
    }

    /**
     * General helper to assert that file overwriting is allowed and the content
     * matches after writing.
     *
     * @param fileWriteCallable a callable that calls the static file write method
     */
    private void assertFileOverwriteAllowedAndContentMatches(java.util.concurrent.Callable<Void> fileWriteCallable) {
        // Define the output file path
        String outputFilePath = "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt";
        
        try {
            // Clean up the output file (empty it)
            cleanupOutputFile(outputFilePath);
            
            // Call the file write method
            fileWriteCallable.call();
            
            // Verify that the writing was correct
            verifyOutputFileCreatedWithCorrectContent(outputFilePath);
        } catch (SecurityException e) {
            Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
        } catch (Exception e) {
            Assertions.fail(ERROR_SECURITY_EXCEPTION, e);
        }
    }




    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {        
        
        // <editor-fold desc="accessFileSystemViaBufferedReader - 3 subtests">
        // <editor-fold desc="accessFileSystemViaBufferedReaderRead">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderRead);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaBufferedReaderReadCharArray">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(
                    ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(
                    ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(
                    ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(
                    ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadCharArray);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaBufferedReaderReadLine">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadLine);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadLine);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadLineMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadLine);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/bufferedReader")
        void test_accessFileSystemViaBufferedReaderReadLineMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadBufferedReaderMain::accessFileSystemViaBufferedReaderReadLine);
        }
        // </editor-fold>
        // </editor-fold>

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

        // <editor-fold desc="accessFileSystemViaObjectInputStream (ObjectInputStream.readObject)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/objectInputStream")
        void test_accessFileSystemViaObjectInputStreamMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadObjectInputStreamMain::accessFileSystemViaObjectInputStream);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaLineNumberReader (LineNumberReader.readLine)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/lineNumberReader")
        void test_accessFileSystemViaLineNumberReaderMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadLineNumberReaderMain::accessFileSystemViaLineNumberReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/lineNumberReader")
        void test_accessFileSystemViaLineNumberReaderMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadLineNumberReaderMain::accessFileSystemViaLineNumberReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/lineNumberReader")
        void test_accessFileSystemViaLineNumberReaderMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadLineNumberReaderMain::accessFileSystemViaLineNumberReader);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/lineNumberReader")
        void test_accessFileSystemViaLineNumberReaderMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadLineNumberReaderMain::accessFileSystemViaLineNumberReader);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileInputStream (FileInputStream.read)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileInputStreamMain::accessFileSystemViaFileInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileInputStreamMain::accessFileSystemViaFileInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileInputStreamMain::accessFileSystemViaFileInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream")
        void test_accessFileSystemViaFileInputStreamMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileInputStreamMain::accessFileSystemViaFileInputStream);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaDataInputStream (DataInputStream.readByte)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadDataInputStreamMain::accessFileSystemViaDataInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadDataInputStreamMain::accessFileSystemViaDataInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadDataInputStreamMain::accessFileSystemViaDataInputStream);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/dataInputStream")
        void test_accessFileSystemViaDataInputStreamMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadDataInputStreamMain::accessFileSystemViaDataInputStream);
        }
        // </editor-fold>
        
        // <editor-fold desc="accessFileSystemViaFileReader (FileReader) - 3 subtests">
        // <editor-fold desc="accessFileSystemViaFileReaderRead">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderRead);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderRead);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArray);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
            assertFileReadAllowedAndContentMatches(
                    ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
            assertFileReadAllowedAndContentMatches(
                    ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
            assertFileReadAllowedAndContentMatches(
                    ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileReader")
        void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
            assertFileReadAllowedAndContentMatches(
                    ReadFileReaderMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength);
        }
        // </editor-fold>
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
    class OverwriteOperations {        // <editor-fold desc="accessFileSystemViaBufferedWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
            assertFileOverwriteAllowedAndContentMatches(
                    () -> {
                        WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT);
                        return null;
                    });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
            assertFileOverwriteAllowedAndContentMatches(
                    () -> {
                        WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT);
                        return null;
                    });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
            assertFileOverwriteAllowedAndContentMatches(
                    () -> {
                        WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT);
                        return null;
                    });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/bufferedWriter")
        void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
            assertFileOverwriteAllowedAndContentMatches(
                    () -> {
                        WriteBufferedWriterMain.accessFileSystemViaBufferedWriter(TRUSTED_FILE_CONTENT);
                        return null;
                    });
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileOutputStream">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileOutputStream")
        void test_accessFileSystemViaFileOutputStreamMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileOutputStreamMain.accessFileSystemViaFileOutputStream(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFileSystemProvider">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileSystemProvider")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileSystemProviderMain.accessFileSystemViaFileSystemProvider(TRUSTED_FILE_CONTENT));
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
            assertNoAresSecurityException(
                    () -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/fileWriter")
        void test_accessFileSystemViaFileWriterMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFileWriterMain.accessFileSystemViaFileWriter(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFilesWrite">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/filesWrite")
        void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteFilesWriteMain.accessFileSystemViaFilesWrite(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaNIOChannel">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/nioChannel")
        void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteNIOChannelMain.accessFileSystemViaNIOChannel(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaPrintWriter">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/printWriter")
        void test_accessFileSystemViaPrintWriterMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WritePrintWriterMain.accessFileSystemViaPrintWriter(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
            assertNoAresSecurityException(
                    () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/overwrite/thirdPartyPackage")
        void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
            assertNoAresSecurityException(
                    () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(TRUSTED_FILE_CONTENT));
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        private static final Path TRUSTED_FILE_PATH = Paths
                .get("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt");

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
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOverwriteDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/fileDelete")
        void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
            assertNoAresSecurityException(FileDeleteMain::accessFileSystemViaFileDeleteOnExit);
        }
        // </editor-fold>

        // <editor-fold desc="accessFileSystemViaFilesDelete">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDelete);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/filesDelete")
        void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
            assertNoAresSecurityException(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists);
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

    // <editor-fold desc="Execute Operations">
    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        // <editor-fold desc="accessFileSystemViaDesktop (Desktop execution)">
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/desktop")
        void test_accessFileSystemViaDesktopMavenArchunitAspectJ() {
            assertExecuteAllowedWithFileCreation(() -> {
                try {
                    ExecuteDesktopMain.accessFileSystemViaDesktop(returnPlatformSpecificTrustedScriptPath());
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/desktop")
        void test_accessFileSystemViaDesktopMavenArchunitInstrumentation() {
            assertExecuteAllowedWithFileCreation(() -> {
                try {
                    ExecuteDesktopMain.accessFileSystemViaDesktop(returnPlatformSpecificTrustedScriptPath());
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/desktop")
        void test_accessFileSystemViaDesktopMavenWalaAspectJ() {
            assertExecuteAllowedWithFileCreation(() -> {
                try {
                    ExecuteDesktopMain.accessFileSystemViaDesktop(returnPlatformSpecificTrustedScriptPath());
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/execute/desktop")
        void test_accessFileSystemViaDesktopMavenWalaInstrumentation() {
            assertExecuteAllowedWithFileCreation(() -> {
                try {
                    ExecuteDesktopMain.accessFileSystemViaDesktop(returnPlatformSpecificTrustedScriptPath());
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // </editor-fold>

    }
    // </editor-fold>
}