package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.audioInputStream.AudioInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.bufferedInputStream.BufferedInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.checkedInputStream.CheckedInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.cipherInputStream.CipherInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.deflaterInputStream.DeflaterInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.digestInputStream.DigestInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileCacheImageInputStream.FileCacheImageInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileImageInputStream.FileImageInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.filterInputStream.FilterInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.gZIPInputStream.GZIPInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.imageInputStreamImpl.ImageInputStreamImplReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.inflaterInputStream.InflaterInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.jarInputStream.JarInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.lineNumberInputStream.LineNumberInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.bufferedReader.BufferedReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.dataInputStream.DataInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileInputStream.FileInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.fileReader.FileReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileChannel.FileChannelReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.files.FilesReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.properties.ReadPropertiesMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.randomAccessFile.RandomAccessFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner.ReadScannerMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class FileSystemAccessReadTest {

    private static final String ERROR_MESSAGE = "No Security Exception was thrown. Check if the policy is correctly applied.";
    private static final String ERR_SECURITY_EN = "Ares Security Error";
    private static final String ERR_SECURITY_DE = "Ares Sicherheitsfehler";
    private static final String REASON_EN = "(Reason: Student-Code; Stage: Execution)";
    private static final String REASON_DE = "(Grund: Student-Code; Phase: Ausführung)";
    private static final String TRIED_EN = "tried";
    private static final String TRIED_DE = "hat versucht,";
    private static final String BLOCKED_EN = "was blocked by Ares.";
    private static final String BLOCKED_DE = "wurde jedoch von Ares blockiert.";

    // <editor-fold desc="Helpers">

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g.,
     *                        "illegally read from", "illegally write from",
     *                        "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g.,
     *                        "illegally read from", "illegally write from",
     *                        "illegally execute from").
     */
    private void assertGeneralErrorMessage(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {
        Path expectedPath = Paths.get(
                "src", "test", "java", "de", "tum", "cit", "ase",
                "ares", "integration", "aop", "forbidden", "subject", "nottrusted.txt");
        String nativePath = expectedPath.toString();
        String unixPath = nativePath.replace(expectedPath.getFileSystem().getSeparator(), "/");

        Assertions.assertTrue(
                actualMessage.contains(ERR_SECURITY_EN) || actualMessage.contains(ERR_SECURITY_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        ERR_SECURITY_EN, ERR_SECURITY_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(REASON_EN) || actualMessage.contains(REASON_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        REASON_EN, REASON_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(TRIED_EN) || actualMessage.contains(TRIED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        TRIED_EN, TRIED_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(clazz.getName()),
                () -> String.format(
                        "Exception message should contain '%s', but was:%n%s",
                        clazz.getName(), actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                () -> String.format(
                        "Exception message should indicate the operation by containing '%s' or '%s', but was:%n%s",
                        operationTextEN, operationTextDE, actualMessage));

        Assertions.assertTrue(
                Stream.of(nativePath, unixPath).anyMatch(actualMessage::contains),
                () -> String.format(
                        "Exception message should contain the path '%s' (or '%s'), but was:%n%s",
                        nativePath, unixPath, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(BLOCKED_EN) || actualMessage.contains(BLOCKED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        BLOCKED_EN, BLOCKED_DE, actualMessage));
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message.
     *
     * @param executable The executable that should throw a SecurityException
     */
    private void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von", clazz);
    }
    // </editor-fold>

    // <editor-fold desc="Read Operations">
    // --- Read Operations ---

    // <editor-fold desc="accessFileSystemViaBufferedReader - 3 subtests">
    // <editor-fold desc="accessFileSystemViaBufferedReaderRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedReaderReadCharArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedReaderReadLine">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader")
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead,
                FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray,
                FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold
    // desc="accessFileSystemViaFileInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength,
                FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength,
                FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadAllBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadAllBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadAllBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadAllBytes, FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadNBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadNBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadNBytes, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream")
    void test_accessFileSystemViaFileInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadNBytes, FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>


    // <editor-fold desc="accessFileSystemViaFileReader (FileReader) - 3 subtests">
    // <editor-fold desc="accessFileSystemViaFileReaderRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader")
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaNIOChannel - 6 subtests">

    // <editor-fold desc="accessFileSystemViaNIOChannel">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaNIOChannel,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaNIOChannel,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaNIOChannel,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaNIOChannel,
                FileChannelReadMain.class);
    }
    // </editor

    // <editor-fold desc="accessFileSystemViaFileChannelTransferFrom">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelTransferFromMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelTransferFrom,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelTransferFromMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelTransferFrom,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelTransferFromMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelTransferFrom,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelTransferFromMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelTransferFrom,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelReadArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelReadArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelMap">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelMapMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelMapMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelMapMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedReadOneThreadAllowed.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel")
    void test_accessFileSystemViaFileChannelMapMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesRead,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadAllLines">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLines,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadAllLinesCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadAllLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadAllLinesCharset,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadString">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadString,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadString,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadString,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadString,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesReadStringCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesReadStringCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesReadStringCharset,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesLines">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLines,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesLinesCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesLinesCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesLinesCharset,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewBufferedReader">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReader,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReader,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReader,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReader,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewBufferedReaderCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReaderCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReaderCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReaderCharset,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewBufferedReaderCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewBufferedReaderCharset,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewInputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewInputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewInputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewInputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewInputStream,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewByteChannel">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewByteChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewByteChannel,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewByteChannelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewByteChannel,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewByteChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewByteChannel,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesNewByteChannelMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesNewByteChannel,
                FilesReadMain.class);
    }// </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesMismatch">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMismatchMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMismatch,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMismatchMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMismatch,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMismatchMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMismatch,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMismatchMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMismatch,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="AudioInputStream Operations">
    // --- AudioInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaAudioInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamRead,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamRead,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamRead,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamRead,
                AudioInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaAudioInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArray,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArray,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArray,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArray,
                AudioInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold
    // desc="accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadByteArrayOffsetLength,
                AudioInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaAudioInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadAllBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadAllBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadAllBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadAllBytes,
                AudioInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaAudioInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadNBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadNBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadNBytes,
                AudioInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream")
    void test_accessFileSystemViaAudioInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                AudioInputStreamReadMain::accessFileSystemViaAudioInputStreamReadNBytes,
                AudioInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="BufferedInputStream Operations">
    // --- BufferedInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaBufferedInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamRead, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamRead, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamRead, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamRead, BufferedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArray, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArray, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArray, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArray, BufferedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadByteArrayOffsetLength, BufferedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadAllBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadAllBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadAllBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadAllBytes, BufferedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadNBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadNBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadNBytes, BufferedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/bufferedInputStream")
    void test_accessFileSystemViaBufferedInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedInputStreamReadMain::accessFileSystemViaBufferedInputStreamReadNBytes, BufferedInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="CheckedInputStream Operations">
    // --- CheckedInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaCheckedInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamRead, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamRead, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamRead, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamRead, CheckedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCheckedInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArray, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArray, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArray, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArray, CheckedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadByteArrayOffsetLength, CheckedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCheckedInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadAllBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadAllBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadAllBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadAllBytes, CheckedInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCheckedInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadNBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadNBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadNBytes, CheckedInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/checkedInputStream")
    void test_accessFileSystemViaCheckedInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CheckedInputStreamReadMain::accessFileSystemViaCheckedInputStreamReadNBytes, CheckedInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="CipherInputStream Operations">
    // --- CipherInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaCipherInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamRead, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamRead, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamRead, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamRead, CipherInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArray, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArray, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArray, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArray, CipherInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength, CipherInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadAllBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadAllBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadAllBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadAllBytes, CipherInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadNBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadNBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadNBytes, CipherInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/cipherInputStream")
    void test_accessFileSystemViaCipherInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(CipherInputStreamReadMain::accessFileSystemViaCipherInputStreamReadNBytes, CipherInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="DeflaterInputStream Operations">
    // --- DeflaterInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaDeflaterInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamRead, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamRead, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamRead, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamRead, DeflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDeflaterInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArray, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArray, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArray, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArray, DeflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLength, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLength, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLength, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadByteArrayOffsetLength, DeflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDeflaterInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadAllBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadAllBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadAllBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadAllBytes, DeflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDeflaterInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadNBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadNBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadNBytes, DeflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/deflaterInputStream")
    void test_accessFileSystemViaDeflaterInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DeflaterInputStreamReadMain::accessFileSystemViaDeflaterInputStreamReadNBytes, DeflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="DigestInputStream Operations">
    // --- DigestInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaDigestInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamRead, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamRead, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamRead, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamRead, DigestInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDigestInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArray, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArray, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArray, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArray, DigestInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength, DigestInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDigestInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadAllBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadAllBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadAllBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadAllBytes, DigestInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDigestInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadNBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadNBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadNBytes, DigestInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/digestInputStream")
    void test_accessFileSystemViaDigestInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DigestInputStreamReadMain::accessFileSystemViaDigestInputStreamReadNBytes, DigestInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="FileCacheImageInputStream Operations">
    // --- FileCacheImageInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamRead, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamRead, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamRead, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamRead, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArray, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArray, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArray, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArray, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByteArrayOffsetLength, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadFully">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFully, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFully, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFully, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFully, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFullyWithOffset, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadBoolean, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadBoolean, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadBoolean, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadBoolean, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadByte, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedByte, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadChar">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadChar, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadChar, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadChar, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadChar, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadDouble, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadDouble, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadDouble, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadDouble, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFloat, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFloat, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFloat, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadFloat, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadLong, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadLong, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadLong, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadLong, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadShort, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort, FileCacheImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileCacheImageInputStream")
    void test_accessFileSystemViaFileCacheImageInputStreamReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileCacheImageInputStreamReadMain::accessFileSystemViaFileCacheImageInputStreamReadUnsignedShort, FileCacheImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="FileImageInputStream Operations">
    // --- FileImageInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamRead, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamRead, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamRead, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamRead, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArray, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArray, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArray, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArray, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByteArrayOffsetLength, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadFully">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFully, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFully, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFully, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFully, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadFullyWithOffset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyWithOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFullyWithOffset, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyWithOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFullyWithOffset, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyWithOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFullyWithOffset, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFullyWithOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFullyWithOffset, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadBoolean, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadBoolean, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadBoolean, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadBoolean, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadByte, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadUnsignedByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedByte, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedByte, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadChar">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadChar, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadChar, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadChar, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadChar, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadDouble, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadDouble, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadDouble, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadDouble, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFloat, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFloat, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFloat, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadFloat, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadLong, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadLong, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadLong, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadLong, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadShort, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileImageInputStreamReadUnsignedShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedShort, FileImageInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileImageInputStream")
    void test_accessFileSystemViaFileImageInputStreamReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileImageInputStreamReadMain::accessFileSystemViaFileImageInputStreamReadUnsignedShort, FileImageInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="FilterInputStream Operations">
    // --- FilterInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaFilterInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamRead, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamRead, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamRead, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamRead, FilterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArray, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArray, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArray, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArray, FilterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadByteArrayOffsetLength, FilterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadAllBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadAllBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadAllBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadAllBytes, FilterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadNBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadNBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadNBytes, FilterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/filterInputStream")
    void test_accessFileSystemViaFilterInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilterInputStreamReadMain::accessFileSystemViaFilterInputStreamReadNBytes, FilterInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="GZIPInputStream Operations">
    // --- GZIPInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaGZIPInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamRead, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamRead, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamRead, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamRead, GZIPInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaGZIPInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArray, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArray, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArray, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArray, GZIPInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadByteArrayOffsetLength, GZIPInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaGZIPInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadAllBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadAllBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadAllBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadAllBytes, GZIPInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaGZIPInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadNBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadNBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadNBytes, GZIPInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/gZIPInputStream")
    void test_accessFileSystemViaGZIPInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(GZIPInputStreamReadMain::accessFileSystemViaGZIPInputStreamReadNBytes, GZIPInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="ImageInputStreamImpl Operations">
    // --- ImageInputStreamImpl Operations ---

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplRead, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplRead, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplRead, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplRead, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArray, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArray, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArray, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArray, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByteArrayOffsetLength, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadFully">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFully, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFully, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFully, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFully, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadFullyWithOffset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyWithOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFullyWithOffset, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyWithOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFullyWithOffset, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyWithOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFullyWithOffset, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFullyWithOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFullyWithOffset, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadBoolean, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadBoolean, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadBoolean, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadBoolean, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadByte, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadUnsignedByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedByte, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedByte, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadChar">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadChar, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadChar, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadChar, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadChar, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadDouble, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadDouble, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadDouble, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadDouble, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFloat, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFloat, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFloat, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadFloat, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadLong, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadLong, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadLong, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadLong, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadShort, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaImageInputStreamImplReadUnsignedShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedShort, ImageInputStreamImplReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/imageInputStreamImpl")
    void test_accessFileSystemViaImageInputStreamImplReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ImageInputStreamImplReadMain::accessFileSystemViaImageInputStreamImplReadUnsignedShort, ImageInputStreamImplReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="InflaterInputStream Operations">
    // --- InflaterInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaInflaterInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamRead, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamRead, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamRead, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamRead, InflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArray, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArray, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArray, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArray, InflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadByteArrayOffsetLength, InflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadAllBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadAllBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadAllBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadAllBytes, InflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadNBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadNBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadNBytes, InflaterInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/inflaterInputStream")
    void test_accessFileSystemViaInflaterInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InflaterInputStreamReadMain::accessFileSystemViaInflaterInputStreamReadNBytes, InflaterInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="JarInputStream Operations">
    // --- JarInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaJarInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamRead, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamRead, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamRead, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamRead, JarInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaJarInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArray, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArray, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArray, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArray, JarInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaJarInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArrayOffsetLength, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArrayOffsetLength, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArrayOffsetLength, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadByteArrayOffsetLength, JarInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaJarInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadAllBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadAllBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadAllBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadAllBytes, JarInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaJarInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadNBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadNBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadNBytes, JarInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/jarInputStream")
    void test_accessFileSystemViaJarInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(JarInputStreamReadMain::accessFileSystemViaJarInputStreamReadNBytes, JarInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="LineNumberInputStream Operations">
    // --- LineNumberInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaLineNumberInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamRead, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamRead, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamRead, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamRead, LineNumberInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaLineNumberInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArray, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArray, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArray, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArray, LineNumberInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadByteArrayOffsetLength, LineNumberInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaLineNumberInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadAllBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadAllBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadAllBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadAllBytes, LineNumberInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaLineNumberInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadNBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadNBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadNBytes, LineNumberInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/lineNumberInputStream")
    void test_accessFileSystemViaLineNumberInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(LineNumberInputStreamReadMain::accessFileSystemViaLineNumberInputStreamReadNBytes, LineNumberInputStreamReadMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesCopyPathToOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyPathToOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyPathToOutputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyPathToOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyPathToOutputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyPathToOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyPathToOutputStream,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyPathToOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyPathToOutputStream,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesCopyInputStreamToPath">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyInputStreamToPathMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyInputStreamToPath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyInputStreamToPathMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyInputStreamToPath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyInputStreamToPathMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyInputStreamToPath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesCopyInputStreamToPathMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesCopyInputStreamToPath,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesMovePath">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMovePathMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMovePath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMovePathMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMovePath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMovePathMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMovePath,
                FilesReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files")
    void test_accessFileSystemViaFilesMovePathMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadMain::accessFileSystemViaFilesMovePath,
                FilesReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaProperties - 3 subtests">
    // <editor-fold desc="accessFileSystemViaPropertiesLoadInputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPropertiesLoadReader">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPropertiesLoadFromXML">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties")
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFile (RandomAccessFile)">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScanner (Scanner) - 16 subtests">
    // <editor-fold desc="accessFileSystemViaScanner">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerFileCharsetName">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetNameMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetNameMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetNameMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetNameMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName,
                ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerFileCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerFileCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPath">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPathCharsetName">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetNameMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetNameMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetNameMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName,
                ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetNameMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName,
                ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPathCharset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerPathCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerInputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNext">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerHasNext">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerHasNextMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerHasNextMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerHasNextMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerHasNextMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextInt">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextIntMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextIntMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextIntMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextIntMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner")
    void test_accessFileSystemViaScannerNextBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage (ThirdPartyPackage)">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ReadThirdPartyPackageMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Extended RandomAccessFile Operations">
    // --- Extended RandomAccessFile Operations ---

    // <editor-fold desc="accessFileSystemViaRandomAccessFileRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold
    // desc="accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadChar">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadFullyOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadInt">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadIntMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadIntMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadIntMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadIntMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadLine">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUnsignedByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUnsignedShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUTF">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF,
                RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF,
                RandomAccessFileReadMain.class);
    }
    // </editor-fold>
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="Extended DataInputStream Operations">
    // --- Extended DataInputStream Operations ---

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadAllBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadAllBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadAllBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadAllBytes, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadNBytes">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadNBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadNBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadNBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadNBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadNBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadNBytes, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadNBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadNBytes, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByte, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadFullyWithOffset">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyWithOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFullyWithOffset, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyWithOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFullyWithOffset, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyWithOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFullyWithOffset, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyWithOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFullyWithOffset, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamRead">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold
    // desc="accessFileSystemViaDataInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(
                DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(
                DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(
                DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(
                DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadChar">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadDouble">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadFloat">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadFully">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadFullyMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadLine">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadLong">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUnsignedByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUnsignedShort">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUTF">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUTFMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUTFMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUTFMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF,
                DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream")
    void test_accessFileSystemViaDataInputStreamReadUTFMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF,
                DataInputStreamReadMain.class);
    }
    // </editor-fold>
    // </editor-fold>
    // </editor-fold>
}
