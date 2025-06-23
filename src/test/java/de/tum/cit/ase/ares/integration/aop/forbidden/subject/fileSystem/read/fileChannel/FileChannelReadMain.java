package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.fileChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileChannelReadMain {

    private FileChannelReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using NIO {@link FileChannel} for reading.
     *
     * @return
     */
    public static int accessFileSystemViaNIOChannel() throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            return channel.read(buffer);
        }
    }

    /**
     * Access the file system using FileChannel.read(ByteBuffer dst).
     *
     * @return
     */
    public static int accessFileSystemViaFileChannelRead() throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            return channel.read(buffer, 0);
        }
    }

    /**
     * Access the file system using FileChannel.read(ByteBuffer[] dsts, int offset, int length).
     *
     * @return
     */
    public static long accessFileSystemViaFileChannelReadArray() throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardOpenOption.READ)) {
            ByteBuffer[] buffers = new ByteBuffer[]{ByteBuffer.allocate(512), ByteBuffer.allocate(512)};
            return channel.read(buffers, 0, buffers.length);
        }
    }

    /**
     * Access the file system using FileChannel.map(MapMode mode, long position, long size).
     *
     * @return
     */
    public static MappedByteBuffer accessFileSystemViaFileChannelMap() throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(channel.size(), 1024));
        }
    }

    /**
     * Access the file system using FileChannel.transferFrom(ReadableByteChannel src, long position, long count).
     *
     * @return
     */
    public static long accessFileSystemViaFileChannelTransferFrom() throws IOException {
        Path sourcePath = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
        Path targetPath = Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/target.txt");
        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ);
             FileChannel targetChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            return targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }
}