package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

public aspect JavaAspectJFileSystemPointcutDefinitions {

    pointcut fileReadMethods():
            (call(* java.io.File.normalizedList(..)) ||
                    call(* java.io.RandomAccessFile.read(..)));

    pointcut fileWriteMethods():
            (call(java.io.RandomAccessFile.new(..)) ||
                    call(java.io.BufferedOutputStream.new(..)) ||
                    call(java.io.DataOutputStream.new(..)) ||
                    call(java.io.ObjectOutputStream.new(..)) ||
                    call(java.util.zip.GZIPOutputStream.new(..)) ||
                    call(java.util.zip.InflaterOutputStream.new(..)) ||
                    call(* java.io.File.setExecutable(..)) ||
                    call(* java.io.File.setLastModified(..)) ||
                    call(* java.io.File.setReadOnly(..)) ||
                    call(* java.io.File.setReadable(..)) ||
                    call(* java.io.File.setWritable(..)) ||
                    call(* java.io.OutputStream.write(..)) ||
                    call(* java.io.BufferedOutputStream.write(..)) ||
                    call(* java.io.DataOutputStream.writeUTF(..)) ||
                    call(* java.io.ObjectOutputStream.writeObject(..)) ||
                    call(* java.io.RandomAccessFile.writeBoolean(..)) ||
                    call(* java.io.RandomAccessFile.writeByte(..)) ||
                    call(* java.io.RandomAccessFile.writeBytes(..)) ||
                    call(* java.io.RandomAccessFile.writeChar(..)) ||
                    call(* java.io.RandomAccessFile.writeChars(..)) ||
                    call(* java.io.RandomAccessFile.writeDouble(..)) ||
                    call(* java.io.RandomAccessFile.writeFloat(..)) ||
                    call(* java.io.RandomAccessFile.writeInt(..)) ||
                    call(* java.io.RandomAccessFile.writeLong(..)) ||
                    call(* java.io.RandomAccessFile.writeShort(..)) ||
                    call(* java.io.RandomAccessFile.writeUTF(..)) ||
                    call(* java.nio.channels.FileChannel.write(..)) ||
                    call(* java.nio.file.Files.write(..)) ||
                    call(* java.nio.file.Files.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files.newOutputStream(..)) ||
                    call(* java.nio.file.attribute.UserDefinedFileAttributeView.write(..)) ||
                    call(* com.sun.management.HotSpotDiagnosticMXBean.dumpHeap(..)) ||
                    call(* jdk.jfr.Recording.dump(..)) ||
                    call(* javax.imageio.ImageIO.write(..)) ||
                    call(* javax.print.DocPrintJob.print(..)) ||
                    call(* javax.sound.sampled.AudioSystem.write(..)) ||
                    call(* javax.xml.bind.Marshaller.marshal(..)) ||
                    call(* javax.xml.transform.Transformer.transform(..)) ||
                    call(* java.io.FileOutputStream.write(..)) ||
                    call(* java.io.RandomAccessFile.write(..)));

    pointcut fileCreateMethods():
            (call(* java.io.File.createNewFile(..)) ||
                    call(* java.io.File.createTempFile(..)) ||
                    call(* java.io.File.mkdir(..)) ||
                    call(* java.io.File.mkdirs(..)));

    pointcut fileExecuteMethods():
            (call(* java.lang.ProcessBuilder.start(..)) ||
                    call(* java.lang.Runtime.exec(..)) ||
                    call(* org.eclipse.swt.program.Program.launch(..)) ||
                    call(* com.apple.eio.FileManager.openURL(..)) ||
                    call(* com.apple.eio.FileManager.runApplication(..)));

    pointcut fileDeleteMethods():
            (call(* java.awt.Desktop.moveToTrash(..)) ||
                    call(* java.io.File.delete(..)) ||
                    call(* java.io.File.deleteOnExit(..)));

    pointcut fileInputStreamInitMethods(): call(java.io.FileInputStream.new(..));

    pointcut fileOutputStreamInitMethods(): call(java.io.FileOutputStream.new(..));

    pointcut objectStreamClassMethods(): if(false);

    pointcut randomAccessFileInitMethods(): call(java.io.RandomAccessFile.new(..));

    pointcut fileTypeDetectorProbeContentTypeMethods(): if(false);

    pointcut fileImageSourceInitMethods(): call(sun.awt.image.FileImageSource.new(..));

    pointcut imageConsumerQueueInitMethods(): call(sun.awt.image.ImageConsumerQueue.new(..));

    pointcut filesReadMethods():
            (call(* java.awt.Toolkit.createImage(..)) ||
                    call(* java.awt.Toolkit.getImage(..)) ||
                    call(* java.awt.image.PixelGrabber.grabPixels(..)) ||
                    call(java.io.BufferedInputStream.new(..)) ||
                    call(* java.io.BufferedInputStream.read(..)) ||
                    call(java.io.DataInputStream.new(..)) ||
                    call(java.io.ObjectInputStream.new(..)) ||
                    call(* java.io.DataInput.read(..)) ||
                    call(* java.io.DataInput.readBoolean(..)) ||
                    call(* java.io.DataInput.readByte(..)) ||
                    call(* java.io.DataInput.readChar(..)) ||
                    call(* java.io.DataInput.readDouble(..)) ||
                    call(* java.io.DataInput.readFloat(..)) ||
                    call(* java.io.DataInput.readFully(..)) ||
                    call(* java.io.DataInput.readInt(..)) ||
                    call(* java.io.DataInput.readLine(..)) ||
                    call(* java.io.DataInput.readLong(..)) ||
                    call(* java.io.DataInput.readShort(..)) ||
                    call(* java.io.DataInput.readUTF(..)) ||
                    call(* java.io.DataInput.readUnsignedByte(..)) ||
                    call(* java.io.DataInput.readUnsignedShort(..)) ||
                    call(* java.io.DataInputStream.read(..)) ||
                    call(* java.io.DataInputStream.readFully(..)) ||
                    call(* java.io.DataInputStream.readUTF(..)) ||
                    call(* java.io.InputStream.read(..)) ||
                    call(* java.io.ObjectInput.readObject(..)) ||
                    call(* java.io.ObjectInput.read(..)) ||
                    call(* java.io.ObjectInputStream.readObject(..)) ||
                    call(* java.io.ObjectInputStream.read(..)) ||
                    call(* java.io.Reader.read(..)) ||
                    call(* java.io.InputStreamReader.read(..)) ||
                    call(* java.lang.ClassLoader.getResourceAsStream(..)) ||
                    call(* java.net.JarURLConnection.getInputStream(..)) ||
                    call(* java.nio.file.Files.find(..)) ||
                    call(* java.nio.file.Files.list(..)) ||
                    call(* java.nio.file.Files.walk(..)) ||
                    call(* java.nio.file.Files.walkFileTree(..)) ||
                    call(* java.nio.file.Files.lines(..)) ||
                    call(* java.nio.file.Files.newBufferedReader(..)) ||
                    call(* java.nio.file.Files.newByteChannel(..)) ||
                    call(* java.nio.file.Files.newDirectoryStream(..)) ||
                    call(* java.nio.file.Files.newInputStream(..)) ||
                    call(* java.nio.file.Files.readAllBytes(..)) ||
                    call(* java.nio.file.Files.readAllLines(..)) ||
                    call(* java.nio.file.Files.readString(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newAsynchronousFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newByteChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newDirectoryStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newInputStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newWatchService(..)) ||
                    call(* javax.imageio.ImageIO.createImageInputStream(..)) ||
                    call(* javax.imageio.ImageIO.getImageReaders(..)) ||
                    call(* javax.imageio.ImageIO.read(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSoundbank(..)) ||
                    call(* javax.sound.sampled.AudioSystem.getAudioInputStream(..)) ||
                    call(* javax.xml.parsers.DocumentBuilder.parse(..)));

    pointcut filesWriteMethods():
            (call(* java.nio.file.Files.write(..)) ||
                    call(* java.nio.file.Files.writeString(..)) ||
                    call(* java.nio.file.Files.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files.newByteChannel(..)) ||
                    call(* java.nio.file.Files.newOutputStream(..)) ||
                    call(* java.nio.file.Files.copy(..)) ||
                    call(* java.nio.file.Files.move(..)) ||
                    call(* java.nio.file.Files.setAttribute(..)) ||
                    call(* java.nio.file.Files.setLastModifiedTime(..)) ||
                    call(* java.nio.file.Files.setOwner(..)) ||
                    call(* java.nio.file.Files.setPosixFilePermissions(..)));

    pointcut filesCreateMethods():
            (call(* java.nio.file.Files.createDirectory(..)) ||
                    call(* java.nio.file.Files.createDirectories(..)) ||
                    call(* java.nio.file.Files.createFile(..)) ||
                    call(* java.nio.file.Files.createLink(..)) ||
                    call(* java.nio.file.Files.createSymbolicLink(..)) ||
                    call(* java.nio.file.Files.createTempDirectory(..)) ||
                    call(* java.nio.file.Files.createTempFile(..)));

    pointcut filesExecuteMethods(): if(false);

    pointcut filesDeleteMethods():
            (call(* java.nio.file.Files.delete(..)) ||
                    call(* java.nio.file.Files.deleteIfExists(..)));

    pointcut fileSystemReadMethods(): if(false);

    pointcut fileSystemWriteMethods():
            (call(* java.nio.file.FileSystem.newWatchService(..)) ||
                    call(* java.nio.file.FileSystem.close(..)));

    pointcut fileSystemExecuteMethods(): if(false);

    pointcut fileChannelExecuteMethods(): if(false);

    pointcut fileChannelReadMethods():
            (call(* java.nio.channels.AsynchronousFileChannel.read(..)) ||
                    call(* java.nio.channels.FileChannel.map(..)) ||
                    call(* java.nio.channels.FileChannel.read(..)));

    pointcut fileChannelCreateMethods():
            call(* java.nio.channels.FileChannel.open(..));

    pointcut fileChannelWriteMethods():
            (call(* java.nio.channels.AsynchronousFileChannel.write(..)) ||
                    call(* java.nio.channels.AsynchronousFileChannel.truncate(..)) ||
                    call(* java.nio.channels.FileChannel.map(..)) ||
                    call(* java.nio.channels.FileChannel.write(..)) ||
                    call(* java.nio.channels.FileChannel.truncate(..)));


    pointcut writerMethods():
             (call(java.io.Writer.new(..)) ||
                    call(* java.io.Writer.append(..)) ||
                    call(* java.io.Writer.write(..)));

    pointcut fileWriterMethods():
            (call(java.io.FileWriter.new(..)) ||
                    call(* java.io.FileWriter.append(..)) ||
                    call(* java.io.FileWriter.write(..)));

    pointcut bufferedWriterMethods():
            call(java.io.BufferedWriter.new(..)) ||
            call(* java.io.BufferedWriter.append(..)) ||
            call(* java.io.BufferedWriter.write(..));

    pointcut fileHandlerMethods():
            (call(java.util.logging.FileHandler.new(..)) ||
                    call(* java.util.logging.FileHandler.publish(..)));

    pointcut midiSystemMethods():
            call(* javax.sound.midi.MidiSystem.getSoundbank(..));

    pointcut fileSystemsReadMethods(): if(false);

    pointcut defaultFileSystemExecuteMethods():
            call(* java.io.DefaultFileSystem.getFileSystem(..));

    pointcut fileSystemProviderReadMethods():
            (call(* java.nio.file.spi.FileSystemProvider.newAsynchronousFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newByteChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newDirectoryStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newInputStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newWatchService(..)));

    pointcut fileSystemProviderCreateMethods():
            (call(* java.nio.file.spi.FileSystemProvider.createDirectory(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.createLink(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.createSymbolicLink(..)));

    pointcut fileSystemProviderWriteMethods():
            (call(* java.nio.file.spi.FileSystemProvider.copy(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.move(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newAsynchronousFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newByteChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.newOutputStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.setAttribute(..)));

    pointcut fileSystemProviderExecuteMethods(): if(false);

    pointcut fileSystemProviderDeleteMethods():
            (call(* java.nio.file.spi.FileSystemProvider.delete(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider.deleteIfExists(..)));

    pointcut bufferedReaderInitMethods(): call(java.io.BufferedReader.new(..));

    pointcut scannerInitMethods(): call(java.util.Scanner.new(java.io.File));

    pointcut fileReaderInitMethods(): call(java.io.FileReader.new(..));

    pointcut printWriterInitMethods(): call(java.io.PrintWriter.new(..));

    pointcut desktopExecuteMethods(): if(false);
}
