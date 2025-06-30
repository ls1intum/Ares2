package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.fileChannel.FileChannelWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.files.WriteFilesWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.dataOutputStream.DataOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.gZIPOutputStream.GZIPOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.inflaterOutputStream.InflaterOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.zipOutputStream.ZipOutputStreamWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.randomAccessFile.RandomAccessFileWriteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.thirdPartyPackage.WriteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.writer.bufferedWriter.BufferedWriterWriteMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


class FileAccessCommonOverwriteTest extends SystemAccessTest {

    private static final Path NOT_TRUSTED_DIR = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir");
    private static final Path NOT_TRUSTED_GZ    = Path.of(NOT_TRUSTED_DIR + "/nottrusted.txt.gz");
    private static final Path NOT_TRUSTED_FILE_PATH = Path.of(NOT_TRUSTED_DIR + "/nottrusted.txt");

    private static final String FILES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/files";
    private static final String BUFFERED_WRITER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/writer/bufferedWriter";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/thirdPartyPackage";
    private static final String FILE_CHANNEL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/fileChannel";
    private static final String GZIP_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/gZIPOutputStream";
    private static final String RANDOM_ACCESS_FILE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/randomAccessFile";
    private static final String DATA_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/dataOutputStream";
    private static final String INFLATER_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/inflaterOutputStream";
    private static final String ZIP_OUTPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/outputStream/zipOutputStream";


    @BeforeEach
    void ensureNotTrustedFilesExist() throws IOException {
        Files.createDirectories(NOT_TRUSTED_DIR);

        if (Files.notExists(NOT_TRUSTED_FILE_PATH)) {
            Files.createFile(NOT_TRUSTED_FILE_PATH);
        }
        if (Files.notExists(NOT_TRUSTED_GZ)) {
            Files.createFile(NOT_TRUSTED_GZ);
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

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = BUFFERED_WRITER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedWriterMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(BufferedWriterWriteMain::accessFileSystemViaBufferedWriter,
                BufferedWriterWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {

        assertAresSecurityExceptionOverwrite(
                () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(NOT_TRUSTED_FILE_PATH),
                ThirdPartyPackagePenguin.class,
                NOT_TRUSTED_FILE_PATH
        );
    }


    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(
                () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(NOT_TRUSTED_FILE_PATH),
                ThirdPartyPackagePenguin.class,
                NOT_TRUSTED_FILE_PATH
        );
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(
                () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(NOT_TRUSTED_FILE_PATH),
                ThirdPartyPackagePenguin.class,
                NOT_TRUSTED_FILE_PATH
        );
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(
                () -> WriteThirdPartyPackageMain.accessFileSystemViaThirdPartyPackage(NOT_TRUSTED_FILE_PATH),
                ThirdPartyPackagePenguin.class,
                NOT_TRUSTED_FILE_PATH
        );
    }



    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenArchunitInstrumentation() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaNIOChannelMavenWalaInstrumentation() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(FileChannelWriteMain::accessFileSystemViaNIOChannel,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }





    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenArchunitInstrumentation() {
        //
        assertAresSecurityExceptionRead(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelWriteBuffersMavenWalaInstrumentation() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(FileChannelWriteMain::accessFileSystemViaFileChannelWriteBuffers,
                FileChannelWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }


    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class, NOT_TRUSTED_GZ);
    }


    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class, NOT_TRUSTED_GZ);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class, NOT_TRUSTED_GZ);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = GZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaGZIPOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(GZIPOutputStreamWriteMain::accessFileSystemViaGZIPOutputStream,
                GZIPOutputStreamWriteMain.class, NOT_TRUSTED_GZ);
    }



    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitAspectJ() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaAspectJ() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteBoolean,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }


    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = DATA_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(DataOutputStreamWriteMain::accessFileSystemViaDataOutputStream,
                DataOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = INFLATER_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInflaterOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(InflaterOutputStreamWriteMain::accessFileSystemViaInflaterOutputStream,
                InflaterOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }



    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = ZIP_OUTPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaZipOutputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(ZipOutputStreamWriteMain::accessFileSystemViaZipOutputStream,
                ZipOutputStreamWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitAspectJ() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaAspectJ() {
        // first file is opened so we test for read access
        assertAresSecurityExceptionRead(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileWriteByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionOverwrite(RandomAccessFileWriteMain::accessFileSystemViaRandomAccessFileWriteByteArray,
                RandomAccessFileWriteMain.class, NOT_TRUSTED_FILE_PATH);
    }




    @AfterEach
    void cleanUpNotTrustedFiles() throws IOException {
        Files.deleteIfExists(NOT_TRUSTED_FILE_PATH);
        Files.deleteIfExists(NOT_TRUSTED_GZ);

        try (var stream = Files.list(NOT_TRUSTED_DIR)) {
            if (stream.findAny().isEmpty()) {
                Files.delete(NOT_TRUSTED_DIR);
            }
        }
    }
}
