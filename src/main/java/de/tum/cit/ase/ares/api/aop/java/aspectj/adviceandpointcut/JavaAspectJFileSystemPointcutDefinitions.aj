package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

public aspect JavaAspectJFileSystemPointcutDefinitions {

    // These are the FileSystem related methods which we want to ban

    pointcut randomAccessFileExecuteMethods() :
                    call(java.io.RandomAccessFile.new(..));

    pointcut fileReadMethods() :
            (call(* java.io.File.canRead(..)) ||
                    call(* java.io.File.exists(..)) ||
                    call(* java.io.File.getFreeSpace(..)) ||
                    call(* java.io.File.getTotalSpace(..)) ||
                    call(* java.io.File.getUsableSpace(..)) ||
                    call(* java.io.File.isDirectory(..)) ||
                    call(* java.io.File.isFile(..)) ||
                    call(* java.io.File.isHidden(..)) ||
                    call(* java.io.File.lastModified(..)) ||
                    call(* java.io.File.length(..)) ||
                    call(* java.io.File.normalizedList(..)));

    pointcut fileWriteMethods() :
            (call(* java.io.File.canWrite(..)) ||
                    call(* java.io.File.createNewFile(..)) ||
                    call(* java.io.File.createTempFile(..)) ||
                    call(* java.io.File.setExecutable(..)) ||
                    call(* java.io.File.setLastModified(..)) ||
                    call(* java.io.File.setReadOnly(..)) ||
                    call(* java.io.File.setReadable(..)) ||
                    call(* java.io.File.setWritable(..)) ||
                    call(* java.io.File.mkdir(..)));

    pointcut fileExecuteMethods() :
            (call(* java.io.File.canExecute(..)) ||
                    call(* java.io.File.renameTo(..))||
                    call(* java.io.File.toPath(..)) ||
                    call(* java.io.File.toURI(..))||
                    call(* java.io.File.mkdirs(..))||
                    call(* java.io.File.listFiles(..)));

    pointcut fileDeleteMethods() :
            (call(* java.io.File.delete(..)) ||
                    call(* java.io.File.deleteOnExit(..)));


    pointcut fileInputStreamInitMethods() : call(java.io.FileInputStream.new(..));

    pointcut fileOutputStreamInitMethods() : call(java.io.FileOutputStream.new(..));

    pointcut objectStreamClassMethods() : call(* java.io.ObjectStreamClass.getField(..));

    pointcut randomAccessFileInitMethods() : call(java.io.RandomAccessFile.new(..));

    pointcut fileTypeDetectorProbeContentTypeMethods() : call(* java.nio.file.spi.FileTypeDetector.probeContentType(..));

    pointcut fileImageSourceInitMethods() : call(sun.awt.image.FileImageSource.new(..));

    pointcut imageConsumerQueueInitMethods() : call(sun.awt.image.ImageConsumerQueue.new(..));

    pointcut filesReadMethods() :
            (call(* java.nio.file.Files.readAllBytes(..)) ||
                    call(* java.nio.file.Files.readAllLines(..)) ||
                    call(* java.nio.file.Files.readString(..)) ||
                    call(* java.nio.file.Files.readAttributes(..)) ||
                    call(* java.nio.file.Files.lines(..)) ||
                    call(* java.nio.file.Files.newBufferedReader(..)) ||
                    call(* java.nio.file.Files.newInputStream(..)) ||
                    call(* java.nio.file.Files.probeContentType(..)) ||
                    call(* java.nio.file.Files.isReadable(..)));

    pointcut filesWriteMethods() :
            (call(* java.nio.file.Files.write(..)) ||
                    call(* java.nio.file.Files.writeString(..)) ||
                    call(* java.nio.file.Files.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files.newOutputStream(..)) ||
                    call(* java.nio.file.Files.setAttribute(..)) ||
                    call(* java.nio.file.Files.setLastModifiedTime(..)) ||
                    call(* java.nio.file.Files.setOwner(..)) ||
                    call(* java.nio.file.Files.setPosixFilePermissions(..)) ||
                    call(* java.nio.file.Files.isWritable(..)));

    pointcut filesExecuteMethods() :
            (call(* java.nio.file.Files.walk(..)) ||
                    call(* java.nio.file.Files.walkFileTree(..)) ||
                    call(* java.nio.file.Files.find(..)) ||
                    call(* java.nio.file.Files.list(..)) ||
                    call(* java.nio.file.Files.copy(..)) ||
                    call(* java.nio.file.Files.move(..)) ||
                    call(* java.nio.file.Files.createDirectory(..)) ||
                    call(* java.nio.file.Files.createDirectories(..)) ||
                    call(* java.nio.file.Files.createFile(..)) ||
                    call(* java.nio.file.Files.createLink(..)) ||
                    call(* java.nio.file.Files.createSymbolicLink(..)) ||
                    call(* java.nio.file.Files.isExecutable(..)));

    pointcut filesDeleteMethods() :
            (call(* java.nio.file.Files.delete(..)) ||
                    call(* java.nio.file.Files.deleteIfExists(..)) ||
                    call(* java.nio.file.Files.isDirectory(..)) ||
                    call(* java.nio.file.Files.isRegularFile(..)) ||
                    call(* java.nio.file.Files.isSameFile(..)) ||
                    call(* java.nio.file.Files.isSymbolicLink(..)) ||
                    call(* java.nio.file.Files.notExists(..)) ||
                    call(* java.nio.file.Files.size(..)));

    pointcut pathReadMethods() :
            (call(* java.nio.file.Path.toFile(..)) ||
                    call(* java.nio.file.Path.getFileName(..)) ||
                    call(* java.nio.file.Path.getName(..)) ||
                    call(* java.nio.file.Path.getNameCount(..)) ||
                    call(* java.nio.file.Path.getParent(..)) ||
                    call(* java.nio.file.Path.getRoot(..)) ||
                    call(* java.nio.file.Path.iterator(..)) ||
                    call(* java.nio.file.Path.subpath(..)) ||
                    call(* java.nio.file.Path.toAbsolutePath(..)) ||
                    call(* java.nio.file.Path.toRealPath(..)) ||
                    call(* java.nio.file.Path.toUri(..)) ||
                    call(* java.nio.file.Path.toString(..)) ||
                    call(* java.nio.file.Path.resolve(..)) ||
                    call(* java.nio.file.Path.resolveSibling(..)) ||
                    call(* java.nio.file.Path.relativize(..)) ||
                    call(* java.nio.file.Path.normalize(..)));

    pointcut pathWriteMethods() :
            (call(* java.nio.file.Path.register(..)) ||
                    call(* java.nio.file.Path.toFile(..)));

    pointcut pathExecuteMethods() :
            (call(* java.nio.file.Path.compareTo(..)) ||
                    call(* java.nio.file.Path.endsWith(..)) ||
                    call(* java.nio.file.Path.startsWith(..)) ||
                    call(* java.nio.file.Path.equals(..)));

    pointcut pathDeleteMethods() :
            (call(* java.nio.file.Path.deleteIfExists(..)) ||
                    call(* java.nio.file.Path.delete(..)));

    pointcut fileSystemReadMethods() :
            (call(* java.nio.file.FileSystem.getFileStores(..)) ||
                    call(* java.nio.file.FileSystem.getPath(..)) ||
                    call(* java.nio.file.FileSystem.getPathMatcher(..)) ||
                    call(* java.nio.file.FileSystem.getRootDirectories(..)) ||
                    call(* java.nio.file.FileSystem.provider(..)) ||
                    call(* java.nio.file.FileSystem.supportedFileAttributeViews(..)) ||
                    call(* java.nio.file.FileSystem.isOpen(..)) ||
                    call(* java.nio.file.FileSystem.isReadOnly(..)));

    pointcut fileSystemWriteMethods() :
            (call(* java.nio.file.FileSystem.newWatchService(..)) ||
                    call(* java.nio.file.FileSystem.close(..)));

    pointcut fileSystemExecuteMethods() :
            (call(* java.nio.file.FileSystem.equals(..)) ||
                    call(* java.nio.file.FileSystem.hashCode(..)) ||
                    call(* java.nio.file.FileSystem.toString(..)));

    pointcut fileChannelExecuteMethods() :
            (call(* java.nio.channels.FileChannel.open(..)) ||
                    call(* java.nio.channels.AsynchronousFileChannel.open()) ||
                    call(* java.nio.channels.FileChannel.position(..)) ||
                    call(* java.nio.channels.FileChannel.tryLock(..)) ||
                    call(* java.nio.channels.AsynchronousFileChannel.tryLock()));

    pointcut fileChannelReadMethods() :
            (call(* java.nio.channels.FileChannel.read(..)) ||
                    call(* java.nio.channels.FileChannel.size(..)));

    pointcut fileChannelWriteMethods() :
            (call(* java.nio.channels.FileChannel.write(..)) ||
                    call(* java.nio.channels.FileChannel.force(..)));

    pointcut fileWriterMethods() :
            (call(java.io.FileWriter.new(..)) ||
                    call(* java.io.FileWriter.append(..)) ||
                    call(* java.io.FileWriter.write(..)) ||
                    call(* java.io.FileWriter.flush(..)) ||
                    call(* java.io.FileWriter.close(..)));

    pointcut fileHandlerMethods() :
            (call(java.util.logging.FileHandler.new(..)) ||
                    call(* java.util.logging.FileHandler.close(..)) ||
                    call(* java.util.logging.FileHandler.flush(..)) ||
                    call(* java.util.logging.FileHandler.publish(..)) ||
                    call(* java.util.logging.FileHandler.setLevel(..)) ||
                    call(* java.util.logging.FileHandler.setFormatter(..)));

    pointcut midiSystemMethods() :
            (call(* javax.sound.midi.MidiSystem.getMidiDevice(..)) ||
                    call(* javax.sound.midi.MidiSystem.getMidiDeviceInfo(..)) ||
                    call(* javax.sound.midi.MidiSystem.getReceiver(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSequencer(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSoundbank(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSynthesizer(..)) ||
                    call(* javax.sound.midi.MidiSystem.getTransmitter(..)));

        pointcut fileSystemsReadMethods() :
                (call(* java.nio.file.FileSystems.getDefault(..)) ||
                        call(* java.nio.file.FileSystems.getFileSystem(..)));

        pointcut fileSystemsExecuteMethods() :
                call(* java.nio.file.FileSystems.newFileSystem(..));

        pointcut defaultFileSystemExecuteMethods() :
                call(* java.io.DefaultFileSystem.getFileSystem(..));

        // FileSystemProvider Methods
        pointcut fileSystemProviderReadMethods() :
                (call(* java.nio.file.spi.FileSystemProvider.checkAccess(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.getFileAttributeView(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.getFileStore(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.getPath(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.getScheme(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.isHidden(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.isSameFile(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.isSymbolicLink(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newAsynchronousFileChannel(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newByteChannel(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newDirectoryStream(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newFileChannel(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newInputStream(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newOutputStream(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.newWatchService(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.readAttributes(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.readSymbolicLink(..)));

        pointcut fileSystemProviderWriteMethods() :
                (call(* java.nio.file.spi.FileSystemProvider.copy(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.createDirectory(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.createLink(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.createSymbolicLink(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.move(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.setAttribute(..))||
                        call(* java.nio.file.spi.FileSystemProvider.newFileSystem(..)));

        pointcut fileSystemProviderExecuteMethods() :
                call(* java.nio.file.spi.FileSystemProvider.checkAccess(..));

        pointcut fileSystemProviderDeleteMethods() :
                (call(* java.nio.file.spi.FileSystemProvider.delete(..)) ||
                        call(* java.nio.file.spi.FileSystemProvider.deleteIfExists(..)));

    //TODO: These should definetly be considered in different pointcut files or this one
    pointcut desktopExecuteMethods() :
            (call(* java.awt.Desktop.browse(..)) ||
                    call(* java.awt.Desktop.browseFileDirectory(..)) ||
                    call(* java.awt.Desktop.edit(..)) ||
                    call(* java.awt.Desktop.mail(..)) ||
                    call(* java.awt.Desktop.mail(..)) ||
                    call(* java.awt.Desktop.open(..)) ||
                    call(* java.awt.Desktop.openHelpViewer(..)) ||
                    call(* java.awt.Desktop.print(..)) ||
                    call(* java.awt.Desktop.setOpenFileHandler(..)) ||
                    call(* java.awt.Desktop.setOpenURIHandler(..)));
}