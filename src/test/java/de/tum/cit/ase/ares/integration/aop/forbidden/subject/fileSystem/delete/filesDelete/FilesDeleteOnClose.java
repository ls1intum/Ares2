package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;

public final class FilesDeleteOnClose {

    private static final String NOT_TRUSTED_FILE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt";


    private FilesDeleteOnClose() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Test): "
                        + "FilesDeleteOnClose is a utility class and should not be instantiated.");
    }





    /**
     * Open a channel with {@link StandardOpenOption#DELETE_ON_CLOSE}.
     * Any file open in channel will be deleted when the channel is closed.
     */
    public static void closeChannelToDeleteFileInChannel(SeekableByteChannel ch) throws IOException {
        ch.close();
    }
}
