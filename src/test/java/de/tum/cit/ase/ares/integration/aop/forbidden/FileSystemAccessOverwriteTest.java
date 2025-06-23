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

class FileSystemAccessOverwriteTest extends SystemAccessTest {

    private static final String FILES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files";
    private static final String BUFFERED_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage";
    private static final String PRINT_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/printWriter";
    private static final String RANDOM_ACCESS_FILE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile";
    private static final String PRINT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/printStream";
    private static final String FILE_CHANNEL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel";
    private static final String CIPHER_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/cipherOutputStream";
    private static final String INFLATER_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream";
    private static final String DATA_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream";
    private static final String OBJECT_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/objectOutputStream";
    private static final String BUFFERED_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/bufferedOutputStream";
    private static final String FILE_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/fileOutputStream";
    private static final String FILTER_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/filterOutputStream";
    private static final String ZIP_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream";
    private static final String GZIP_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream";
    private static final String WRITER_PRINT_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/printWriter";
    private static final String FILTER_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/filterWriter";
    private static final String OUTPUT_STREAM_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/outputStreamWriter";
    private static final String FILE_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/fileWriter";


    // <editor-fold desc="accessFileSystemViaFilesWrite">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWrite,
                WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
                ThirdPartyPackagePenguin.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaNIOChannel">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWrite">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWrite,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWriteBuffers">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelWritePosition">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWritePositionMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWritePositionMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWritePositionMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWritePositionMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWritePosition,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelTruncate">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTruncateMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTruncateMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTruncateMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTruncateMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTruncate,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelTransferTo">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTransferToMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTransferToMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTransferToMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelTransferToMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelTransferTo,
                FileChannelWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WritePrintWriterMain::accessFileSystemViaPrintWriter,
                WritePrintWriterMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteBoolean">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteByte">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByte,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWrite">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWrite,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileWriteByteArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStream,
                PrintStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintStreamWithPrint">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrint,
                PrintStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilesNewOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewOutputStream,
                WriteFilesWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaCipherOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = CIPHER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaCipherOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = CIPHER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaCipherOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = CIPHER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaCipherOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = CIPHER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaCipherOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(CipherOutputStreamWriteMain::accessFileSystemViaCipherOutputStream,
                CipherOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInflaterOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaObjectOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OBJECT_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaObjectOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OBJECT_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaObjectOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OBJECT_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaObjectOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OBJECT_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaObjectOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(ObjectOutputStreamWriteMain::accessFileSystemViaObjectOutputStream,
                ObjectOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStream,
                BufferedOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStream,
                FileOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStream,
                FilterOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaZipOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPrintWriterWrite">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WRITER_PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterWriteMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WRITER_PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterWriteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WRITER_PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterWriteMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WRITER_PRINT_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaPrintWriterWriteMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintWriterWriteMain::accessFileSystemViaPrintWriter,
                PrintWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFilterWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFilterWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFilterWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFilterWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFilterWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterWriterWriteMain::accessFileSystemViaFilterWriter,
                FilterWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaOutputStreamWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OUTPUT_STREAM_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaOutputStreamWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OUTPUT_STREAM_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaOutputStreamWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OUTPUT_STREAM_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaOutputStreamWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = OUTPUT_STREAM_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaOutputStreamWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(OutputStreamWriterWriteMain::accessFileSystemViaOutputStreamWriter,
                OutputStreamWriterWriteMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFileWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFileWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFileWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaFileWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileWriterWriteMain::accessFileSystemViaFileWriter,
                FileWriterWriteMain.class);
    }
    // </editor-fold>

    //<editor-fold desc="accessFileSystemViaBufferedOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithData,
                BufferedOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaBufferedOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithDataAndOffset,
                BufferedOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaBufferedOutputStreamWithFlush">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaBufferedOutputStreamWithFlushMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedOutputStreamWriteMain::accessFileSystemViaBufferedOutputStreamWithFlush,
                BufferedOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaDataOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithData,
                DataOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaDataOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithDataAndOffset,
                DataOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaDataOutputStreamWithPrimitives">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamWithPrimitivesMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStreamWithPrimitives,
                DataOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFileOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithData,
                FileOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFileOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FileOutputStreamWriteMain::accessFileSystemViaFileOutputStreamWithDataAndOffset,
                FileOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilterOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithData,
                FilterOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilterOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILTER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilterOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(FilterOutputStreamWriteMain::accessFileSystemViaFilterOutputStreamWithDataAndOffset,
                FilterOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaGZIPOutputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaGZIPOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithData,
                GZIPOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaGZIPOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithDataAndOffset,
                GZIPOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaGZIPOutputStreamWithCustomBuffer">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamWithCustomBufferMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStreamWithCustomBuffer,
                GZIPOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaInflaterOutputStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithData,
                InflaterOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaInflaterOutputStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithDataAndOffset,
                InflaterOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaInflaterOutputStreamWithCustomInflater">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamWithCustomInflaterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStreamWithCustomInflater,
                InflaterOutputStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaPrintStreamWithData">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithData,
                PrintStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaPrintStreamWithDataAndOffset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithDataAndOffsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithDataAndOffset,
                PrintStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaPrintStreamWithPrintln">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintlnMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintln,
                PrintStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaPrintStreamWithPrintf">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintfMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintfMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintfMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithPrintfMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithPrintf,
                PrintStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaPrintStreamWithAppend">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithAppendMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithAppendMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithAppendMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = PRINT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaPrintStreamWithAppendMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(PrintStreamWriteMain::accessFileSystemViaPrintStreamWithAppend,
                PrintStreamWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesNewBufferedWriterWithCharset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriterWithCharset,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesNewBufferedWriter">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesNewBufferedWriter,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesWriteLinesWithCharset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLinesWithCharset,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesWriteLines">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteLines,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesWriteStringWithCharset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringWithCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteStringWithCharset,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesWriteString">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesWriteStringMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesWriteString,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesCopyFromInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyFromInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopyFromInputStream,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesCopy">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesCopyMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesCopy,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>

    //<editor-fold desc="accessFileSystemViaFilesMove">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_WITHIN_PATH)
    void test_accessFileSystemViaFilesMoveMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(WriteFilesWriteMain::accessFileSystemViaFilesMove,
                WriteFilesWriteMain.class);
    }
    //</editor-fold>
}