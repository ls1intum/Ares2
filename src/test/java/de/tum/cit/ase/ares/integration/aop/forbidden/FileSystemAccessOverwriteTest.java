package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.printWriter.WritePrintWriterMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.randomAccessFile.RandomAccessFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.printStream.PrintStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.fileChannel.FileChannelWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.cipherOutputStream.CipherOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.inflaterOutputStream.InflaterOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.dataOutputStream.DataOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.objectOutputStream.ObjectOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.bufferedOutputStream.BufferedOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.fileOutputStream.FileOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.filterOutputStream.FilterOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.zipOutputStream.ZipOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.gZIPOutputStream.GZIPOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.printWriter.PrintWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.filterWriter.FilterWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.outputStreamWriter.OutputStreamWriterWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.fileWriter.FileWriterWriteMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class FileSystemAccessOverwriteTest {

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
    private void assertAresSecurityExceptionOverwrite(Executable executable, Class<?> clazz) {
        SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessage(se.getMessage(), "illegally overwrite from", "illegal overwrite von", clazz);
    }
    
    // </editor-fold>

    // <editor-fold desc="Overwrite Operations">
    // --- Overwrite Operations ---

    // <editor-fold desc="accessFileSystemViaFilesWrite">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedWriter">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter")
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage")
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaNIOChannel">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWrite">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWriteBuffers">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWritePosition">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWritePositionMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWritePositionMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWritePositionMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelWritePositionMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelTruncate">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTruncateMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTruncateMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTruncateMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTruncateMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelTransferTo">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTransferToMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTransferToMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTransferToMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel")
    void test_accessFileSystemViaFileChannelTransferToMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintWriter">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/printWriter")
    void test_accessFileSystemViaPrintWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/printWriter")
    void test_accessFileSystemViaPrintWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/printWriter")
    void test_accessFileSystemViaPrintWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/printWriter")
    void test_accessFileSystemViaPrintWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteBoolean">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteByte">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWrite">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteByteArray">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile")
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintStreamWithPrint">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }
    // </editor-fold>
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaObjectOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/objectOutputStream")
    void test_accessFileSystemViaObjectOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/objectOutputStream")
    void test_accessFileSystemViaObjectOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/objectOutputStream")
    void test_accessFileSystemViaObjectOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/objectOutputStream")
    void test_accessFileSystemViaObjectOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaZipOutputStream">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream")
    void test_accessFileSystemViaZipOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream")
    void test_accessFileSystemViaZipOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream")
    void test_accessFileSystemViaZipOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream")
    void test_accessFileSystemViaZipOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintWriterWrite">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/printWriter")
    void test_accessFileSystemViaPrintWriterWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/printWriter")
    void test_accessFileSystemViaPrintWriterWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/printWriter")
    void test_accessFileSystemViaPrintWriterWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/printWriter")
    void test_accessFileSystemViaPrintWriterWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterWriter">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/filterWriter")
    void test_accessFileSystemViaFilterWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/filterWriter")
    void test_accessFileSystemViaFilterWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/filterWriter")
    void test_accessFileSystemViaFilterWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/filterWriter")
    void test_accessFileSystemViaFilterWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaOutputStreamWriter">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/outputStreamWriter")
    void test_accessFileSystemViaOutputStreamWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/outputStreamWriter")
    void test_accessFileSystemViaOutputStreamWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/outputStreamWriter")
    void test_accessFileSystemViaOutputStreamWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/outputStreamWriter")
    void test_accessFileSystemViaOutputStreamWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileWriter">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/fileWriter")
    void test_accessFileSystemViaFileWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/fileWriter")
    void test_accessFileSystemViaFileWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/fileWriter")
    void test_accessFileSystemViaFileWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/fileWriter")
    void test_accessFileSystemViaFileWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for BufferedOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream")
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for CipherOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithData,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithData,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithData,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithData,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithDataAndOffset,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithDataAndOffset,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithDataAndOffset,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream")
    void test_accessFileSystemViaCipherOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStreamWithDataAndOffset,
                CipherOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for DataOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream")
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for FileOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream")
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for FilterOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream")
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Tests for GZIPOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream")
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for InflaterOutputStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream")
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for PrintStreamWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintfMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintfMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintfMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithPrintfMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithAppendMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithAppendMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithAppendMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream")
    void test_accessFileSystemViaPrintStreamWithAppendMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="Additional Tests for WriteFilesWriteMain methods">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesNewBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesWriteStringMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesCopyMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesMoveMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesMoveMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesMoveMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedOverwrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files")
    void test_accessFileSystemViaFilesMoveMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }
    // </editor-fold>
}