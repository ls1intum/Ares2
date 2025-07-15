package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

public final class FilesDeleteOnClose {

    private static final Path TRUSTED_FILE = Path.of(
            "src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/delete/trusteddir/trusted.txt");

    private FilesDeleteOnClose() { throw new SecurityException("utility"); }

    public static void closeChannelToDeleteFileInChannel(SeekableByteChannel ch) throws IOException {
        ch.close();    // delete happens here
    }


}
