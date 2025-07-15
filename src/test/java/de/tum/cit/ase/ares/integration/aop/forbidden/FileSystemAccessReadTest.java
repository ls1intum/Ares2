package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.FileinputStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.InputStream;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.DataInputStream;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader.InputStreamReader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader.FileReader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader.Reader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader.BufferedReader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.LineTokenReader.ScannerReader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience.FilesReadAllBytes;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience.FilesReadString;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.WholeFileConvenience.FilesReadAllLines;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.LineTokenReader.FilesLines;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.CharacterReader.FilesNewBufferedReader;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.FilesNewInputStream;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.FileChannelRead;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.RandomAccessFileRead;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.FilesNewByteChannel;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream.ClassLoaderGetResourceAsStream;

class FileSystemAccessReadTest extends SystemAccessTest {
    private static final String FILE_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/ByteStream";
    private static final String CHARACTER_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/CharacterReader";
    private static final String LINE_TOKEN_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/LineTokenReader";
    private static final String WHOLE_FILE_CONVENIENCE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/WholeFileConvenience";

    // <editor-fold desc="FileInputStream Tests">

    // <editor-fold desc="accessFileSystemViaFileInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileinputStreamMain::accessFileSystemViaFileInputStream,
                FileinputStreamMain.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileinputStreamMain::accessFileSystemViaFileInputStream,
                FileinputStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileinputStreamMain::accessFileSystemViaFileInputStream,
                FileinputStreamMain.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileinputStreamMain::accessFileSystemViaFileInputStream,
                FileinputStreamMain.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="InputStream Tests">

    // <editor-fold desc="accessFileSystemViaInputStreamRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamRead,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamRead,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamRead,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamRead,
                InputStream.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInputStreamAvailable">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamAvailableMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamAvailable,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamAvailableMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamAvailable,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamAvailableMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamAvailable,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamAvailableMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamAvailable,
                InputStream.class);
    }
    // </editor-fold>

    // <editor-fold desc="accessFileSystemViaInputStreamReadAllBytes">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamReadAllBytes,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamReadAllBytes,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamReadAllBytes,
                InputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InputStream::accessFileSystemViaInputStreamReadAllBytes,
                InputStream.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="DataInputStream Tests">
    
    // <editor-fold desc="accessFileSystemViaDataInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(DataInputStream::accessFileSystemViaDataInputStream,
                DataInputStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStream::accessFileSystemViaDataInputStream,
                DataInputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(DataInputStream::accessFileSystemViaDataInputStream,
                DataInputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaDataInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(DataInputStream::accessFileSystemViaDataInputStream,
                DataInputStream.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="InputStreamReader Tests">
    
    // <editor-fold desc="accessFileSystemViaInputStreamReader">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(InputStreamReader::accessFileSystemViaInputStreamReader,
                InputStreamReader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(InputStreamReader::accessFileSystemViaInputStreamReader,
                InputStreamReader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(InputStreamReader::accessFileSystemViaInputStreamReader,
                InputStreamReader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaInputStreamReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(InputStreamReader::accessFileSystemViaInputStreamReader,
                InputStreamReader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="FileReader Tests">
    
    // <editor-fold desc="accessFileSystemViaFileReader">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileReader::accessFileSystemViaFileReader,
                FileReader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileReader::accessFileSystemViaFileReader,
                FileReader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileReader::accessFileSystemViaFileReader,
                FileReader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFileReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileReader::accessFileSystemViaFileReader,
                FileReader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Reader Tests">
    
    // <editor-fold desc="accessFileSystemViaReader">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(Reader::accessFileSystemViaReader,
                Reader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(Reader::accessFileSystemViaReader,
                Reader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(Reader::accessFileSystemViaReader,
                Reader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(Reader::accessFileSystemViaReader,
                Reader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="BufferedReader Tests">
    
    // <editor-fold desc="accessFileSystemViaBufferedReaderReadLine">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(BufferedReader::accessFileSystemViaBufferedReaderReadLine,
                BufferedReader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReader::accessFileSystemViaBufferedReaderReadLine,
                BufferedReader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(BufferedReader::accessFileSystemViaBufferedReaderReadLine,
                BufferedReader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaBufferedReaderReadLineMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(BufferedReader::accessFileSystemViaBufferedReaderReadLine,
                BufferedReader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Scanner Tests">
    
    // <editor-fold desc="accessFileSystemViaScanner">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ScannerReader::accessFileSystemViaScanner,
                ScannerReader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ScannerReader::accessFileSystemViaScanner,
                ScannerReader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ScannerReader::accessFileSystemViaScanner,
                ScannerReader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaScannerMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ScannerReader::accessFileSystemViaScanner,
                ScannerReader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.readAllBytes Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesReadAllBytes">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllBytesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadAllBytes::accessFileSystemViaFilesReadAllBytes,
                FilesReadAllBytes.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllBytesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadAllBytes::accessFileSystemViaFilesReadAllBytes,
                FilesReadAllBytes.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllBytesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadAllBytes::accessFileSystemViaFilesReadAllBytes,
                FilesReadAllBytes.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllBytesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadAllBytes::accessFileSystemViaFilesReadAllBytes,
                FilesReadAllBytes.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.readString Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesReadString">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadStringMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadString::accessFileSystemViaFilesReadString,
                FilesReadString.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadStringMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadString::accessFileSystemViaFilesReadString,
                FilesReadString.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadStringMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadString::accessFileSystemViaFilesReadString,
                FilesReadString.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadStringMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadString::accessFileSystemViaFilesReadString,
                FilesReadString.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.readAllLines Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesReadAllLines">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesReadAllLines::accessFileSystemViaFilesReadAllLines,
                FilesReadAllLines.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadAllLines::accessFileSystemViaFilesReadAllLines,
                FilesReadAllLines.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesReadAllLines::accessFileSystemViaFilesReadAllLines,
                FilesReadAllLines.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WHOLE_FILE_CONVENIENCE_WITHIN_PATH)
    void test_accessFileSystemViaFilesReadAllLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesReadAllLines::accessFileSystemViaFilesReadAllLines,
                FilesReadAllLines.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.lines Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesLines">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesLinesMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesLines::accessFileSystemViaFilesLines,
                FilesLines.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesLinesMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesLines::accessFileSystemViaFilesLines,
                FilesLines.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesLinesMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesLines::accessFileSystemViaFilesLines,
                FilesLines.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = LINE_TOKEN_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesLinesMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesLines::accessFileSystemViaFilesLines,
                FilesLines.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.newBufferedReader Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesNewBufferedReader">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedReaderMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesNewBufferedReader::accessFileSystemViaFilesNewBufferedReader,
                FilesNewBufferedReader.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedReaderMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewBufferedReader::accessFileSystemViaFilesNewBufferedReader,
                FilesNewBufferedReader.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedReaderMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesNewBufferedReader::accessFileSystemViaFilesNewBufferedReader,
                FilesNewBufferedReader.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = CHARACTER_READER_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewBufferedReaderMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewBufferedReader::accessFileSystemViaFilesNewBufferedReader,
                FilesNewBufferedReader.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.newInputStream Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesNewInputStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewInputStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesNewInputStream::accessFileSystemViaFilesNewInputStream,
                FilesNewInputStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewInputStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewInputStream::accessFileSystemViaFilesNewInputStream,
                FilesNewInputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewInputStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesNewInputStream::accessFileSystemViaFilesNewInputStream,
                FilesNewInputStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewInputStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewInputStream::accessFileSystemViaFilesNewInputStream,
                FilesNewInputStream.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="FileChannel.read Tests">
    
    // <editor-fold desc="accessFileSystemViaFileChannelRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FileChannelRead::accessFileSystemViaFileChannelRead,
                FileChannelRead.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelRead::accessFileSystemViaFileChannelRead,
                FileChannelRead.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FileChannelRead::accessFileSystemViaFileChannelRead,
                FileChannelRead.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFileChannelReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FileChannelRead::accessFileSystemViaFileChannelRead,
                FileChannelRead.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="RandomAccessFile.read Tests">
    
    // <editor-fold desc="accessFileSystemViaRandomAccessFileRead">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileRead::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileRead.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileRead::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileRead.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(RandomAccessFileRead::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileRead.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaRandomAccessFileReadMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(RandomAccessFileRead::accessFileSystemViaRandomAccessFileRead,
                RandomAccessFileRead.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="Files.newByteChannel Tests">
    
    // <editor-fold desc="accessFileSystemViaFilesNewByteChannel">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewByteChannelMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(FilesNewByteChannel::accessFileSystemViaFilesNewByteChannel,
                FilesNewByteChannel.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewByteChannelMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewByteChannel::accessFileSystemViaFilesNewByteChannel,
                FilesNewByteChannel.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewByteChannelMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(FilesNewByteChannel::accessFileSystemViaFilesNewByteChannel,
                FilesNewByteChannel.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaFilesNewByteChannelMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(FilesNewByteChannel::accessFileSystemViaFilesNewByteChannel,
                FilesNewByteChannel.class);
    }
    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="ClassLoader.getResourceAsStream Tests">
    
    // <editor-fold desc="accessFileSystemViaClassLoaderGetResourceAsStream">
    @PublicTest
    @Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaClassLoaderGetResourceAsStreamMavenArchunitAspectJ() {
        assertAresSecurityExceptionRead(ClassLoaderGetResourceAsStream::accessFileSystemViaClassLoaderGetResourceAsStream,
                ClassLoaderGetResourceAsStream.class);
    }

    @PublicTest
    @Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaClassLoaderGetResourceAsStreamMavenArchunitInstrumentation() {
        assertAresSecurityExceptionRead(ClassLoaderGetResourceAsStream::accessFileSystemViaClassLoaderGetResourceAsStream,
                ClassLoaderGetResourceAsStream.class);
    }

    @PublicTest
    @Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaClassLoaderGetResourceAsStreamMavenWalaAspectJ() {
        assertAresSecurityExceptionRead(ClassLoaderGetResourceAsStream::accessFileSystemViaClassLoaderGetResourceAsStream,
                ClassLoaderGetResourceAsStream.class);
    }

    @PublicTest
    @Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_INPUT_STREAM_WITHIN_PATH)
    void test_accessFileSystemViaClassLoaderGetResourceAsStreamMavenWalaInstrumentation() {
        assertAresSecurityExceptionRead(ClassLoaderGetResourceAsStream::accessFileSystemViaClassLoaderGetResourceAsStream,
                ClassLoaderGetResourceAsStream.class);
    }
    // </editor-fold>

    // </editor-fold>

}
