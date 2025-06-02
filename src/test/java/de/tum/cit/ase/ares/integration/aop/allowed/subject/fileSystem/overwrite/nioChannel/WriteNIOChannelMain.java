package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.overwrite.nioChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WriteNIOChannelMain {

    private WriteNIOChannelMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using NIO {@link FileChannel} for writing.
     * @param text The text to write to the trusted file
     */
    public static void accessFileSystemViaNIOChannel(String text) throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(text.getBytes());
            channel.write(buffer);
        }
    }
}