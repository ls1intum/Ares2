package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.files.FilesReadMain;
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
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.memoryCacheImageInputStream.MemoryCacheImageInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.objectInputStream.ObjectInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.bufferedReader.BufferedReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.dataInputStream.DataInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.fileInputStream.FileInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.reader.fileReader.FileReaderReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileChannel.FileChannelReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.properties.ReadPropertiesMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.randomAccessFile.RandomAccessFileReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner.ReadScannerMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.thirdPartyPackage.ReadThirdPartyPackageMain;

class FileSystemAccessReadTest extends SystemAccessTest {    private static final String BUFFERED_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/bufferedReader";
    private static final String FILE_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/fileInputStream";
    private static final String DATA_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/dataInputStream";
    private static final String AUDIO_INPUT_STREAM_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/inputStream/audioInputStream";
    private static final String FILE_READER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/reader/fileReader";
    private static final String FILE_CHANNEL_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/fileChannel";
    private static final String FILES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/files";
    private static final String PROPERTIES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/properties";
    private static final String RANDOM_ACCESS_FILE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/randomAccessFile";
    private static final String SCANNER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/scanner";
    private static final String THIRD_PARTY_PACKAGE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/thirdPartyPackage";


}
