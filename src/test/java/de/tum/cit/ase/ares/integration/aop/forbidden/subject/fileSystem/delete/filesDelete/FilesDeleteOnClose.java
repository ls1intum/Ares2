package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * The file is removed automatically when the try-with-resources block exits.
     */
    public static void accessFileSystemViaDeleteOnClose() throws IOException {
        Path p = Path.of(
                NOT_TRUSTED_FILE);

        try (SeekableByteChannel ch = Files.newByteChannel(
                p,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.DELETE_ON_CLOSE)) {

            ch.write(ByteBuffer.wrap("temporary".getBytes(StandardCharsets.UTF_8)));
        }
        // File disappears here.
    }
}
