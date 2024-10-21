package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.junit.jupiter.api.Assertions.fail;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FileSystemAccessUser;
import de.tum.cit.ase.ares.testutilities.*;

import java.awt.*;
import java.io.IOException;

@UserBased(FileSystemAccessUser.class)
class FileSystemAccessTest {

    @UserTestResults
    private static Events tests;

    //<editor-fold desc="Constants">
    private final String accessPathAllFiles = "accessPathAllFiles";
    private final String accessPathAllowed = "accessPathAllowed";
    private final String accessPathNormal = "accessPathNormal";
    private final String accessPathNormalAllowed = "accessPathNormalAllowed";
    private final String accessPathRelativeGlobA = "accessPathRelativeGlobA";
    private final String accessPathRelativeGlobB = "accessPathRelativeGlobB";
    private final String accessPathRelativeGlobDirectChildrenAllowed = "accessPathRelativeGlobDirectChildrenAllowed";
    private final String accessPathRelativeGlobDirectChildrenBlacklist = "accessPathRelativeGlobDirectChildrenBlacklist";
    private final String accessPathRelativeGlobDirectChildrenForbidden = "accessPathRelativeGlobDirectChildrenForbidden";
    private final String accessPathRelativeGlobRecursiveAllowed = "accessPathRelativeGlobRecursiveAllowed";
    private final String accessPathRelativeGlobRecursiveBlacklist = "accessPathRelativeGlobRecursiveBlacklist";
    private final String accessPathRelativeGlobRecursiveForbidden = "accessPathRelativeGlobRecursiveForbidden";
    private final String accessPathTest = "accessPathTest";
    private final String weAccessPath = "weAccessPath";

    private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";
    //</editor-fold>

    // TODO Markus: Look into why we cannot structure the rest of the tests like "Other Tests"
    //<editor-fold desc="Other Tests">
	/* OUTCOMMENTED: Conceptually not possible anymore
	@TestTest
	void test_accessPathAllFiles() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathAllFiles, SecurityException.class));
	}

	@TestTest
	void test_accessPathAllowed() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathAllowed));
	}
	*/

    @TestTest
    void test_accessPathAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathNormalAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobA() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobA));
    }

    @TestTest
    void test_accessPathRelativeGlobB() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobB));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobDirectChildrenAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenBlacklist() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobDirectChildrenBlacklist, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenForbidden() {
        // tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobDirectChildrenForbidden, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobRecursiveAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveBlacklist() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobRecursiveBlacklist, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveForbidden() {
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobRecursiveForbidden, SecurityException.class));
    }

    @TestTest
    void test_accessPathTest() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathTest, SecurityException.class));
    }

    @TestTest
    void test_weAccessPath() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weAccessPath));
    }

    @TestTest
    void test_accessFileSystem() {
        tests.assertThatEvents().haveExactly(1, testFailedWith("accessFileSystem", SecurityException.class,
                """
                        \n - Ares Security Error (Reason: Student-Code; Stage: Execution):
                          - Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaBufferedReader()> calls constructor <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:84) accesses <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:84)"""));
    }
    //</editor-fold>

    //TODO Markus: Look into why we are catching Runtime Exceptions and not Security Exceptions

    //<editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        //<editor-fold desc="accessFileSystemViaRandomAccessFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReadInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamInstrumentation() throws IOException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderAspectJ() throws IOException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaScanner">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamAspectJ() throws IOException, ClassNotFoundException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamInstrumentation() throws IOException, ClassNotFoundException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaInputStreamReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Write Operations">
    // --- Write Operations ---
    @Nested
    class WriteOperations {

        //<editor-fold desc="accessFileSystemViaRandomAccessFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaOutputStreamWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileHandler">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Execute Operations">
    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        //<editor-fold desc="accessFileSystemViaFileExecute">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileExecuteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileExecute();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileExecuteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileExecute();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDesktop">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopAspectJ() throws IOException {
            try {
                Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopInstrumentation() throws IOException {
            try {
                Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        //<editor-fold desc="accessFileSystemViaFilesDelete">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaJFileChooser">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaJFileChooserAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaJFileChooserInstrumentation() {
            try {
                Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
                FileSystemAccessPenguin.accessFileSystemViaJFileChooser();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>
}
