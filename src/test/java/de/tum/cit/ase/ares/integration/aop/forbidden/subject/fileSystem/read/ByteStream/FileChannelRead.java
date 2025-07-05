package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelRead {

    private FileChannelRead() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using FileChannel.read(ByteBuffer) for high-throughput, seekable byte stream reading.
     * Test Case: FileChannel.read(ByteBuffer) - High-throughput read into a buffer; supports random access
     */
    public static void accessFileSystemViaFileChannelRead() throws IOException {
        Path path = Paths.get("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer); // Read bytes into buffer
            channel.position(0); // Seek to start (random access)
            channel.read(buffer); // Read again
        }
    }
}
