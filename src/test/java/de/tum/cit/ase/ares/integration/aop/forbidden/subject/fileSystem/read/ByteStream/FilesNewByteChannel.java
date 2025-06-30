package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FilesNewByteChannel {

    private FilesNewByteChannel() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Files.newByteChannel(Path, OpenOption...) for seekable byte channel reading.
     * Test Case: Files.newByteChannel(Path, OpenOption...) - Open a SeekableByteChannel for scatter/gather I/O
     */
    public static void accessFileSystemViaFilesNewByteChannel() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer); // Read bytes into buffer
            channel.position(0); // Seek to start
            channel.read(buffer); // Read again
        }
    }
}
