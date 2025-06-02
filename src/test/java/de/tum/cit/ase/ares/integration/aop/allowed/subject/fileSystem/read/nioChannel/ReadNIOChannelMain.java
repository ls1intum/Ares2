package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.nioChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ReadNIOChannelMain {

    private ReadNIOChannelMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using NIO {@link FileChannel} for reading.
     * @return The content of the trusted file
     */
    public static String accessFileSystemViaNIOChannel() throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return new String(bytes);
        }
    }
}