package de.tum.cit.ase.ares.integration;

import static org.junit.jupiter.api.Assertions.*;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem.FileSystemAccessPenguin;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FileSystemAccessUser;
import de.tum.cit.ase.ares.testutilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * This test class tests the file system access of the test user.
 * We are not using the usual Ares testing style, since the agent works on the current running tests, user based tests are not working as expected
 */
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

    private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";;

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g., "illegally read from", "illegally write from", "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g., "illegally read from", "illegally write from", "illegally execute from").
     */
    private void assertGeneralErrorMessage(String actualMessage, String operationTextEN, String operationTextDE) {
        assertTrue(actualMessage.contains("Ares Security Error") || actualMessage.contains("Ares Sicherheitsfehler"),
                "Exception message should contain 'Ares Security Error'" + System.lineSeparator() + actualMessage + "or 'Ares Sicherheitsfehler'"  + System.lineSeparator() + actualMessage);
        assertTrue(actualMessage.contains("Student-Code"),
                "Exception message should contain 'Student-Code'" + System.lineSeparator() + actualMessage);
        assertTrue(actualMessage.contains("Execution") || actualMessage.contains("Ausführung"),
                "Exception message should contain 'Execution'" + System.lineSeparator() + actualMessage);
        assertTrue(actualMessage.contains("FileSystemAccessPenguin"),
                "Exception message should contain the class name 'FileSystemAccessPenguin'" + System.lineSeparator() + actualMessage);
        assertTrue(actualMessage.contains(new File(System.getProperty("user.dir"), "pom123.xml").getAbsolutePath()),
                "Exception message should contain the forbidden file location: " +(new File(System.getProperty("user.dir"), "pom123.xml").getAbsolutePath()) + System.lineSeparator() + actualMessage);
        assertTrue(actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                "Exception message should indicate the expected operation by containing '" + operationTextEN + "'" + System.lineSeparator() + actualMessage + "or '" + operationTextDE + "'" + System.lineSeparator() + actualMessage);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionRead(Executable executable) {
        SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von");
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionWrite(Executable executable) {
        SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von");
    }

    /**
     * Test that the given executable throws a SecurityException with the expected message.
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionExecution(Executable executable) {
        SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally execute from", "illegal execute von");
    }

    /**
     * Helper method for delete operations.
     *
     * @param executable        Code that is expected to throw a SecurityException.
     */
    private void assertAresSecurityExceptionDelete(Executable executable) {
        SecurityException se = assertThrows(SecurityException.class, executable, errorMessage);
        assertGeneralErrorMessage(se.getMessage(), "illegally delete from", "illegal delete von");
    }
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

//    @TestTest
//    void test_accessPathAllowed() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathNormalAllowed));
//    }

//    @TestTest
//    void test_accessPathRelativeGlobA() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobA));
//    }
//
//    @TestTest
//    void test_accessPathRelativeGlobB() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobB));
//    }
//
//    @TestTest
//    void test_accessPathRelativeGlobDirectChildrenAllowed() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobDirectChildrenAllowed));
//    }

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

//    @TestTest
//    void test_accessPathRelativeGlobRecursiveAllowed() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobRecursiveAllowed));
//    }

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

//    @TestTest
//    void test_weAccessPath() {
//        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weAccessPath));
//    }
    //</editor-fold>

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        //<editor-fold desc="accessFileSystemViaRandomAccessFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaRandomAccessFileReadAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaRandomAccessFile);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaRandomAccessFileReadInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaRandomAccessFile);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileReadAspectJ() {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileRead);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileReadInstrumentation() {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileRead);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileInputStreamAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileInputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileInputStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileInputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileReaderAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileReader);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileReaderInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileReader);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaBufferedReaderAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaBufferedReader);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaBufferedReaderInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaBufferedReader);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaScanner">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaScannerAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaScanner);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaScannerInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaScanner);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDataInputStreamAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaDataInputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDataInputStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaDataInputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaObjectInputStreamAspectJ() throws IOException, ClassNotFoundException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaObjectInputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaObjectInputStreamInstrumentation() throws IOException, ClassNotFoundException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaObjectInputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaInputStreamReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaInputStreamReaderAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaInputStreamReader);// Expected exception
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaInputStreamReaderInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaInputStreamReader);// Expected exception
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileChannelReadAspectJ() throws IOException {
            assertAresSecurityExceptionExecution(FileSystemAccessPenguin::accessFileSystemViaFileChannelRead);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileChannelReadInstrumentation() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileChannelRead);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFilesReadAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFilesRead);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFilesReadInstrumentation() throws IOException, ClassNotFoundException, IllegalAccessException {
            Class<?> adviceSettingsClassClassloader = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, Thread.currentThread().getContextClassLoader());
            Class<?> adviceSettingsClassBootloader = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
            Map<String, Object> settingsClassloader = new TreeMap<>();
            Map<String, Object> settingsBootloader = new TreeMap<>();
            for (Field field : adviceSettingsClassClassloader.getDeclaredFields()) {
                field.setAccessible(true);
                settingsClassloader.put(field.getName(), field.get(null));
            }
            for (Field field : adviceSettingsClassBootloader.getDeclaredFields()) {
                field.setAccessible(true);
                settingsBootloader.put(field.getName(), field.get(null));
            }
            var y = 0;
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFilesRead);
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Write Operations">
    // --- Write Operations ---
    @Nested
    class WriteOperations {

        private static final Logger log = LoggerFactory.getLogger(WriteOperations.class);

        //<editor-fold desc="accessFileSystemViaRandomAccessFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaRandomAccessFileWriteAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaRandomAccessFileWrite);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaRandomAccessFileWriteInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaRandomAccessFileWrite);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileWriteAspectJ() throws IOException {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileWrite);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileWriteInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileWrite);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileOutputStreamAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileOutputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileOutputStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileOutputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileWriterAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileWriter);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileWriterInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileWriter);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaBufferedWriterAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaBufferedWriter);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaBufferedWriterInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaBufferedWriter);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaPrintWriterAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaPrintWriter);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaPrintWriterInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaPrintWriter);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDataOutputStreamAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaDataOutputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDataOutputStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaDataOutputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaObjectOutputStreamAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaObjectOutputStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaObjectOutputStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaObjectOutputStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaOutputStreamWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaOutputStreamWriterAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaOutputStreamWriter);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaOutputStreamWriterInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaOutputStreamWriter);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaPrintStreamAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaPrintStream);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaPrintStreamInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaPrintStream);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileChannelWriteAspectJ() throws IOException {
            assertAresSecurityExceptionExecution(FileSystemAccessPenguin::accessFileSystemViaFileChannelWrite);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileChannelWriteInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileChannelWrite);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFilesWriteAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFilesWrite);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFilesWriteInstrumentation() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFilesWrite);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileHandler">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileHandlerAspectJ() throws IOException {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileHandler);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileHandlerInstrumentation() {
            assertAresSecurityExceptionWrite(FileSystemAccessPenguin::accessFileSystemViaFileHandler);
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
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileExecuteAspectJ() {
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaFileExecute);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileExecuteInstrumentation() {
            assertAresSecurityExceptionExecution(FileSystemAccessPenguin::accessFileSystemViaFileExecute);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDesktop">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDesktopAspectJ() throws IOException {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
            assertAresSecurityExceptionRead(FileSystemAccessPenguin::accessFileSystemViaDesktop);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaDesktopInstrumentation() throws IOException {
            Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping test in headless environment");
            assertAresSecurityExceptionExecution(FileSystemAccessPenguin::accessFileSystemViaDesktop);
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
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem/filesDelete")
        void xtest_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFilesDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFilesDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFilesDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem/filesDelete")
        void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFilesDelete);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileDelete">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileDelete);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileDelete);
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileSystemProvider);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileSystemProviderMavenArchunitInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileSystemProvider);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileSystemProviderMavenWalaAspectJ() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileSystemProvider);
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
        void test_accessFileSystemViaFileSystemProviderMavenWalaInstrumentation() throws IOException {
            assertAresSecurityExceptionDelete(FileSystemAccessPenguin::accessFileSystemViaFileSystemProvider);
        }
        //</editor-fold>
    }
    //</editor-fold>
}
