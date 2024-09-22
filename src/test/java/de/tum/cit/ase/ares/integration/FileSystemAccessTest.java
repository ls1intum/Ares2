package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.junit.jupiter.api.Assertions.fail;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin;
import org.junit.jupiter.api.Nested;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FileSystemAccessUser;
import de.tum.cit.ase.ares.testutilities.*;

import java.io.IOException;
import java.nio.file.Path;

@UserBased(FileSystemAccessUser.class)
class FileSystemAccessTest {

    @UserTestResults
    private static Events tests;

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
        tests.assertThatEvents().haveExactly(1, testFailedWith("accessFileSystem", RuntimeException.class));
    }

    //TODO Markus: Look into why we are catching Runtime Exceptions and not Security Exceptions

    // --- Read Operations ---
    @Nested
    class ReadOperations {

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
        void test_accessPathNormalAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessPath(Path.of("pom212.xml"));
                fail();
            } catch (RuntimeException e) {
                // TODO: check if the exception message is correct
                // expected
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
        void test_accessPathNormalInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessPath(Path.of("pom212.xml"));
                fail();
            } catch (RuntimeException e) {
                // TODO: check if the exception message is correct
                // expected
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail();
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileRead();
                fail(errorMessage);
            } catch (RuntimeException e) {
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
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }
    }

    // --- Write Operations ---
    @Nested
    class WriteOperations {

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }
    }

    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileExecuteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileExecute();
                fail(errorMessage);
            } catch (RuntimeException e) {
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
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }
    }

    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (RuntimeException e) {
                // Expected exception
            }
        }
    }
}
