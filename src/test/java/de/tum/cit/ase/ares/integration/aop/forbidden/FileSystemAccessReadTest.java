package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.bufferedReader.BufferedReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.dataInputStream.DataInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileInputStream.FileInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.fileReader.FileReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileChannel.FileChannelReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.properties.ReadPropertiesMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.randomAccessFile.RandomAccessFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner.ReadScannerMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;

class FileSystemAccessReadTest extends SystemAccessTest {

    private static final String BUFFERED_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader";
    private static final String FILE_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream";
    private static final String DATA_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream";
    private static final String FILE_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader";
    private static final String FILE_CHANNEL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel";
    private static final String PROPERTIES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties";
    private static final String RANDOM_ACCESS_FILE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile";
    private static final String SCANNER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage";

    // <editor-fold desc="accessFileSystemViaBufferedReaderRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderRead,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedReaderReadCharArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadCharArray,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaBufferedReaderReadLine">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = BUFFERED_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReaderReadMain::accessFileSystemViaBufferedReaderReadLine,
                BufferedReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamRead, FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamReadByteArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArray, FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength, FileInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileInputStreamReadMain::accessFileSystemViaFileInputStreamReadByteArrayOffsetLength, FileInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderRead,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArray,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileReaderReadCharArrayOffsetLength">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderReadCharArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReaderReadMain::accessFileSystemViaFileReaderReadCharArrayOffsetLength,
                FileReaderReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelRead,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelReadArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelReadArray,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaFileChannelMap">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelMapMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelMapMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelMapMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_CHANNEL_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelMapMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelReadMain::accessFileSystemViaFileChannelMap,
                FileChannelReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPropertiesLoadInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadInputStream,
                ReadPropertiesMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPropertiesLoadReader">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadReader,
                ReadPropertiesMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaPropertiesLoadFromXML">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = PROPERTIES_WITHIN_PATH)
    void test_accessFileSystemViaPropertiesLoadFromXMLMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadPropertiesMain::accessFileSystemViaPropertiesLoadFromXML,
                ReadPropertiesMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFile">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFile, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScanner">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScanner, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerFileCharsetName">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetNameMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetNameMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetNameMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetNameMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharsetName, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerFileCharset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerFileCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerFileCharset, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPath">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPath, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPathCharsetName">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetNameMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetNameMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetNameMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetNameMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharsetName, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerPathCharset">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerPathCharsetMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerPathCharset, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerInputStream, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNext">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNext, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerHasNext">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerHasNextMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerHasNextMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerHasNextMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerHasNextMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerHasNext, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextByte">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextByte, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextShort">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextShort, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextInt">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextIntMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextIntMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextIntMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextIntMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextInt, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextLong">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextLong, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextFloat">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextFloat, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextDouble">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextDouble, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaScannerNextBoolean">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = SCANNER_WITHIN_PATH)
    void test_accessFileSystemViaScannerNextBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadScannerMain::accessFileSystemViaScannerNextBoolean, ReadScannerMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaThirdPartyPackage (ThirdPartyPackage)">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ReadThirdPartyPackageMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = THIRD_PARTY_PACKAGE_WITHIN_PATH)
    void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ReadThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage, ReadThirdPartyPackageMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileRead, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadByteArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArray, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByteArrayOffsetLength, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadBoolean">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadBoolean, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadByte">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadByte, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadChar">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadChar, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadDouble">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadDouble, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadFloat">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFloat, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadFullyOffsetLength">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadFullyOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadFullyOffsetLength, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadInt">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadIntMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadIntMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadIntMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadIntMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadInt, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadLine">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLine, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadLong">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadLong, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadShort">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadShort, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUnsignedByte">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedByte, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUnsignedShort">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUnsignedShort, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaRandomAccessFileReadUTF">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF, RandomAccessFileReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = RANDOM_ACCESS_FILE_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadUTFMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileReadMain::accessFileSystemViaRandomAccessFileReadUTF, RandomAccessFileReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamRead, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadByteArray">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArray, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadByteArrayOffsetLength">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadByteArrayOffsetLengthMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadByteArrayOffsetLength, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadBoolean">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadBooleanMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadBoolean, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadChar">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadCharMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadCharMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadCharMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadCharMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadChar, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadDouble">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadDoubleMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadDouble, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadFloat">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFloatMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFloatMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFloatMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFloatMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFloat, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadFully">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFullyMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFullyMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFullyMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadFullyMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadFully, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadLine">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLine, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadLong">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLongMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLongMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLongMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadLongMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadLong, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadShort">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadShort, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUnsignedByte">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedByteMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedByte, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUnsignedShort">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUnsignedShortMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUnsignedShort, DataInputStreamReadMain.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaDataInputStreamReadUTF">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUTFMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUTFMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUTFMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF, DataInputStreamReadMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = DATA_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamReadUTFMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStreamReadMain::accessFileSystemViaDataInputStreamReadUTF, DataInputStreamReadMain.class);
    }
    // </editor-fold>
}
