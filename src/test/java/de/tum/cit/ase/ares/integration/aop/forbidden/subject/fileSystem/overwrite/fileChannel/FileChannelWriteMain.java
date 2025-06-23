package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.fileChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileChannelWriteMain {

    private FileChannelWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using NIO {@link FileChannel} for writing.
     */
    public static void accessFileSystemViaNIOChannel() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap("Hello, world!".getBytes());
            channel.write(buffer);
        }
    }

    /**
     * Access the file system using NIO {@link FileChannel#write(ByteBuffer)} for writing.
     * This is already demonstrated in accessFileSystemViaNIOChannel but included separately for completeness.
     */
    public static void accessFileSystemViaFileChannelWrite() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap("Using write(ByteBuffer) method".getBytes());
            int bytesWritten = channel.write(buffer);
            System.out.println("Bytes written: " + bytesWritten);
        }
    }

    /**
     * Access the file system using NIO {@link FileChannel#write(ByteBuffer[], int, int)} for writing.
     */
    public static void accessFileSystemViaFileChannelWriteBuffers() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer[] buffers = new ByteBuffer[] {
                ByteBuffer.wrap("First buffer. ".getBytes()),
                ByteBuffer.wrap("Second buffer. ".getBytes()),
                ByteBuffer.wrap("Third buffer.".getBytes())
            };

            long bytesWritten = channel.write(buffers, 0, buffers.length);
            System.out.println("Bytes written from multiple buffers: " + bytesWritten);
        }
    }

    /**
     * Access the file system using NIO {@link FileChannel#write(ByteBuffer, long)} for writing at a specific position.
     */
    public static void accessFileSystemViaFileChannelWritePosition() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            // First write some content
            channel.write(ByteBuffer.wrap("Initial content. ".getBytes()));

            // Then write at a specific position
            ByteBuffer buffer = ByteBuffer.wrap("Positioned content.".getBytes());
            int bytesWritten = channel.write(buffer, 20); // Write starting at position 20
            System.out.println("Bytes written at position: " + bytesWritten);
        }
    }

    /**
     * Access the file system using NIO {@link FileChannel#truncate(long)} to resize a file.
     */
    public static void accessFileSystemViaFileChannelTruncate() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            // Write some content
            channel.write(ByteBuffer.wrap("This is a longer content that will be truncated.".getBytes()));

            // Truncate the file to 10 bytes
            FileChannel truncated = channel.truncate(10);
            System.out.println("File size after truncation: " + truncated.size());
        }
    }

    /**
     * Access the file system using NIO {@link FileChannel#transferTo(long, long, WritableByteChannel)} to transfer data to another channel.
     */
    public static void accessFileSystemViaFileChannelTransferTo() throws IOException {
        Path sourcePath = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        Path targetPath = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted-copy.txt");

        // First create source file with content
        try (FileChannel sourceChannel = FileChannel.open(
                sourcePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            sourceChannel.write(ByteBuffer.wrap("Content to be transferred to another file.".getBytes()));
        }

        // Then transfer its content to another file
        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ);
             FileChannel targetChannel = FileChannel.open(
                     targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            long bytesTransferred = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
            System.out.println("Bytes transferred: " + bytesTransferred);
        }
    }
}