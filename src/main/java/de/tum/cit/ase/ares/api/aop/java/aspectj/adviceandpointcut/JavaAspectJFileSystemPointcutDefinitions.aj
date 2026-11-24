package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection") public aspect JavaAspectJFileSystemPointcutDefinitions {

    pointcut fileReadMethods():
            (call(* java.io.File+.normalizedList(..)) ||
                    call(* java.io.File+.list(..)) ||
                    call(* java.io.File+.listFiles(..)) ||
                    call(* java.io.File+.listRoots(..)) ||
                    call(* java.io.RandomAccessFile+.read(..)) ||
                    // FileSystemView lists inside the JDK, which is not woven, so the
                    // call itself is checked against the directory it is given.
                    call(* javax.swing.filechooser.FileSystemView+.getFiles(..)) ||
                    call(* javax.swing.filechooser.FileSystemView+.getChild(..)) ||
                    call(* javax.swing.filechooser.FileSystemView+.isParent(..)));

    pointcut fileWriteMethods():
            (call(java.io.RandomAccessFile+.new(..)) ||
                    call(java.io.BufferedOutputStream+.new(..)) ||
                    call(java.io.PrintStream+.new(..)) ||
                    call(java.util.logging.FileHandler+.new(..)) ||
                    call(java.util.zip.GZIPOutputStream+.new(..)) ||
                    call(java.util.zip.InflaterOutputStream+.new(..)) ||
                    call(java.util.zip.ZipOutputStream+.new(..)) ||
                    call(java.util.jar.JarOutputStream+.new(..)) ||
                    call(java.util.Formatter+.new(..)) ||
                    call(* java.io.File+.setExecutable(..)) ||
                    call(* java.io.File+.setLastModified(..)) ||
                    call(* java.io.File+.setReadOnly(..)) ||
                    call(* java.io.File+.setReadable(..)) ||
                    call(* java.io.File+.setWritable(..)) ||
                    call(* java.io.File+.renameTo(..)) ||
                    call(* java.io.DataOutput+.writeBoolean(..)) ||
                    call(* java.io.DataOutput+.writeByte(..)) ||
                    call(* java.io.DataOutput+.writeBytes(..)) ||
                    call(* java.io.DataOutput+.writeChar(..)) ||
                    call(* java.io.DataOutput+.writeChars(..)) ||
                    call(* java.io.DataOutput+.writeDouble(..)) ||
                    call(* java.io.DataOutput+.writeFloat(..)) ||
                    call(* java.io.DataOutput+.writeInt(..)) ||
                    call(* java.io.DataOutput+.writeLong(..)) ||
                    call(* java.io.DataOutput+.writeShort(..)) ||
                    call(* java.io.DataOutput+.writeUTF(..)) ||
                    call(* java.nio.channels.FileChannel+.write(..)) ||
                    call(* java.nio.file.Files+.write(..)) ||
                    call(* java.nio.file.Files+.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files+.newOutputStream(..)) ||
                    call(* java.nio.file.attribute.UserDefinedFileAttributeView+.write(..)) ||
                    call(* javax.imageio.ImageIO+.write(..)) ||
                    call(* javax.imageio.ImageIO+.createImageOutputStream(..)) ||
                    call(* javax.print.DocPrintJob+.print(..)) ||
                    call(* javax.sound.sampled.AudioSystem+.write(..)) ||
                    call(* javax.xml.transform.Transformer+.transform(..)) ||
                    call(* javax.xml.bind.Marshaller+.marshal(..)) ||
                    call(* java.util.zip.ZipOutputStream+.putNextEntry(..)) ||
                    call(* java.util.zip.ZipOutputStream+.closeEntry(..)) ||
                    call(* java.util.jar.JarOutputStream+.putNextEntry(..)) ||
                    call(* java.util.jar.JarOutputStream+.closeEntry(..)) ||
                    call(* java.util.Properties+.store(..)) ||
                    call(* java.util.Properties+.storeToXML(..)) ||
                    // java.util.prefs.Preferences writes to a backing store on disk through
                    // no student-opened stream, so it carries no path argument, exactly like
                    // Properties.store above. The file system advice allows it only where the
                    // overwrite allow-list contains "*".
                    call(* java.util.prefs.Preferences+.put(..)) ||
                    call(* java.util.prefs.Preferences+.putInt(..)) ||
                    call(* java.util.prefs.Preferences+.putLong(..)) ||
                    call(* java.util.prefs.Preferences+.putBoolean(..)) ||
                    call(* java.util.prefs.Preferences+.putFloat(..)) ||
                    call(* java.util.prefs.Preferences+.putDouble(..)) ||
                    call(* java.util.prefs.Preferences+.putByteArray(..)) ||
                    call(* java.util.prefs.Preferences+.remove(..)) ||
                    call(* java.util.prefs.Preferences+.clear(..)) ||
                    call(* java.util.prefs.Preferences+.flush(..)) ||
                    call(* java.util.prefs.Preferences+.sync(..)) ||
                    call(* java.util.prefs.Preferences+.importPreferences(..))
                    );

    pointcut fileCreateMethods():
            (call(* java.io.File+.createNewFile(..)) ||
                    call(* java.io.File+.createTempFile(..)) ||
                    call(* java.io.File+.mkdir(..)) ||
                    call(* java.io.File+.mkdirs(..)) ||
                    call(java.io.FileOutputStream+.new(..)) ||
                    call(java.io.BufferedOutputStream+.new(..)) ||
                    call(java.io.FileWriter+.new(..)) ||
                    call(java.io.PrintWriter+.new(..)) ||
                    call(java.io.RandomAccessFile+.new(..)) ||
                    call(* javax.swing.filechooser.FileSystemView+.createNewFolder(..)) ||
                    // Preferences.node(path) and the userNodeForPackage/systemNodeForPackage
                    // factories create the node and any missing ancestors in the backing
                    // store, like File.mkdir, so they are creates. importPreferences creates
                    // every missing node its XML names, so it is a create as well as the
                    // overwrite it is registered as above. userRoot and systemRoot can create
                    // the store's directory and its modification file on first access, so they
                    // are creates as well as the reads they are registered as below.
                    call(* java.util.prefs.Preferences+.node(..)) ||
                    call(* java.util.prefs.Preferences+.userNodeForPackage(..)) ||
                    call(* java.util.prefs.Preferences+.systemNodeForPackage(..)) ||
                    call(* java.util.prefs.Preferences+.importPreferences(..)) ||
                    call(* java.util.prefs.Preferences+.userRoot(..)) ||
                    call(* java.util.prefs.Preferences+.systemRoot(..)));

    // Note: ProcessBuilder.start, startPipeline, and Runtime.exec are handled by the Command System
    // in Byte Buddy mode, as they execute commands rather than individual files.
    // Desktop.open/edit/print/browse/browseFileDirectory launch an external application
    // to act on the file or URI, so they are classified as an execute (launch) operation
    // rather than a file read, matching the instrumentation backend.
    pointcut fileExecuteMethods():
            (call(* java.lang.Runtime+.load(..)) ||
                    call(* java.lang.Runtime+.loadLibrary(..)) ||
                    call(* java.lang.System+.load(..)) ||
                    call(* java.lang.System+.loadLibrary(..)) ||
                    call(* java.awt.Desktop+.open(..)) ||
                    call(* java.awt.Desktop+.edit(..)) ||
                    call(* java.awt.Desktop+.print(..)) ||
                    call(* java.awt.Desktop+.browse(..)) ||
                    call(* java.awt.Desktop+.browseFileDirectory(..)));

    pointcut fileDeleteMethods():
            (call(* java.awt.Desktop+.moveToTrash(..)) ||
                    call(* java.io.File+.delete(..)) ||
                    call(* java.io.File+.deleteOnExit(..)) ||
                    call(* org.apache.commons.io.FileUtils+.forceDelete(..)) ||
                    // Preferences.removeNode removes the node from the backing store, so it is
                    // a delete and is checked against the delete allow-list, not overwrite.
                    call(* java.util.prefs.Preferences+.removeNode(..)));

    pointcut fileInputStreamInitMethods(): call(java.io.FileInputStream+.new(..));

    pointcut fileOutputStreamInitMethods(): call(java.io.FileOutputStream+.new(..));

    pointcut objectStreamClassMethods(): if(false);

    pointcut randomAccessFileInitMethods(): call(java.io.RandomAccessFile+.new(..));

    pointcut fileTypeDetectorProbeContentTypeMethods(): if(false);

    pointcut fileImageSourceInitMethods(): if(false);

    pointcut imageConsumerQueueInitMethods(): if(false);

    // Instrumentation rewrites application archive calls to JavaInstrumentationArchiveCallSite,
    // which checks them itself; that check is inactive under an AspectJ policy. Exclude the
    // wrapper here: the agent loads it on the boot class loader, which sees AspectJ only when it
    // is on the boot class path, so a woven copy stops the agent from starting.
    pointcut filesReadMethods():
            (call(* java.awt.Toolkit+.createImage(..)) ||
                    call(* java.awt.Toolkit+.getImage(..)) ||

                    call(* java.awt.Font+.createFont(..)) ||
                    call(* java.awt.Font+.createFonts(..)) ||
                    call(java.io.BufferedInputStream+.new(..)) ||
                    call(* java.io.DataInput+.read(..)) ||
                    call(* java.io.DataInput+.readBoolean(..)) ||
                    call(* java.io.DataInput+.readByte(..)) ||
                    call(* java.io.DataInput+.readChar(..)) ||
                    call(* java.io.DataInput+.readDouble(..)) ||
                    call(* java.io.DataInput+.readFloat(..)) ||
                    call(* java.io.DataInput+.readFully(..)) ||
                    call(* java.io.DataInput+.readInt(..)) ||
                    call(* java.io.DataInput+.readLine(..)) ||
                    call(* java.io.DataInput+.readLong(..)) ||
                    call(* java.io.DataInput+.readShort(..)) ||
                    call(* java.io.DataInput+.readUTF(..)) ||
                    call(* java.io.DataInput+.readUnsignedByte(..)) ||
                    call(* java.io.DataInput+.readUnsignedShort(..)) ||
                    call(java.io.Reader+.new(..)) ||
                    call(* java.lang.Class.getResourceAsStream(..)) ||
                    call(* java.lang.ClassLoader+.getResourceAsStream(..)) ||
                    call(* java.nio.file.Files+.find(..)) ||
                    call(* java.nio.file.Files+.list(..)) ||
                    call(* java.nio.file.Files+.mismatch(..)) ||
                    // Also registered in filesWriteMethods() below: copy's source path must be
                    // checked as READ, not OVERWRITE (I-114/TD-063) - the advice's
                    // checkCopyOrTransferSpecialCase splits each firing to the correct parameter.
                    call(* java.nio.file.Files+.copy(..)) ||
                    // Also registered in filesWriteMethods() and filesDeleteMethods() below:
                    // move reads and deletes its source and writes its target, and
                    // checkCopyOrTransferSpecialCase checks each firing against its own side.
                    call(* java.nio.file.Files+.move(..)) ||
                    call(* java.nio.file.Files+.walk(..)) ||
                    call(* java.nio.file.Files+.walkFileTree(..)) ||
                    call(* java.nio.file.Files+.lines(..)) ||
                    call(* java.nio.file.Files+.newBufferedReader(..)) ||
                    call(* java.nio.file.Files+.newByteChannel(..)) ||
                    call(* java.nio.file.Files+.newDirectoryStream(..)) ||
                    call(* java.nio.file.Files+.newInputStream(..)) ||
                    call(* java.nio.file.Files+.readAllBytes(..)) ||
                    call(* java.nio.file.Files+.readAllLines(..)) ||
                    call(* java.nio.file.Files+.readString(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newDirectoryStream(..)) ||
                    call(* javax.imageio.ImageIO+.createImageInputStream(..)) ||
                    call(* javax.imageio.ImageIO+.getImageReaders(..)) ||
                    call(* javax.imageio.ImageIO+.read(..)) ||
                    call(javax.imageio.stream.FileCacheImageInputStream+.new(..)) ||
                    call(javax.imageio.stream.FileImageInputStream+.new(..)) ||
                    call(* javax.sound.midi.MidiSystem+.getSoundbank(..)) ||
                    call(* javax.sound.sampled.AudioSystem+.getAudioInputStream(..)) ||
                    call(* javax.xml.parsers.DocumentBuilder+.parse(..)) ||
                    call(* javax.xml.parsers.SAXParser+.parse(..)) ||
                    call(* javax.xml.bind.Unmarshaller+.unmarshal(..)) ||
                    call(java.util.zip.ZipFile+.new(..)) ||
                    call(* java.util.zip.ZipFile+.entries(..)) ||
                    call(* java.util.zip.ZipFile+.getInputStream(..)) ||
                    call(java.util.zip.ZipInputStream+.new(..)) ||
                    call(* java.util.zip.ZipInputStream+.getNextEntry(..)) ||
                    call(java.util.zip.GZIPInputStream+.new(..)) ||
                    call(java.util.jar.JarFile+.new(..)) ||
                    call(* java.util.jar.JarFile+.entries(..)) ||
                    call(* java.util.jar.JarFile+.getInputStream(..)) ||
                    call(java.util.jar.JarInputStream+.new(..)) ||
                    call(* java.util.jar.JarInputStream+.getNextJarEntry(..)) ||
                    call(* java.util.Properties+.load(..)) ||
                    call(* java.util.Properties+.loadFromXML(..)) ||
                    // java.util.prefs.Preferences reads from a backing store on disk without a
                    // path argument, like Properties.load above. The accessor methods
                    // (userRoot, systemRoot, ...) are the only way to reach the store, so they
                    // are classified as reads. The file system advice allows them only where
                    // the read allow-list contains "*".
                    call(* java.util.prefs.Preferences+.get(..)) ||
                    call(* java.util.prefs.Preferences+.getInt(..)) ||
                    call(* java.util.prefs.Preferences+.getLong(..)) ||
                    call(* java.util.prefs.Preferences+.getBoolean(..)) ||
                    call(* java.util.prefs.Preferences+.getFloat(..)) ||
                    call(* java.util.prefs.Preferences+.getDouble(..)) ||
                    call(* java.util.prefs.Preferences+.getByteArray(..)) ||
                    call(* java.util.prefs.Preferences+.keys(..)) ||
                    call(* java.util.prefs.Preferences+.childrenNames(..)) ||
                    call(* java.util.prefs.Preferences+.nodeExists(..)) ||
                    call(* java.util.prefs.Preferences+.exportNode(..)) ||
                    call(* java.util.prefs.Preferences+.exportSubtree(..)) ||
                    call(* java.util.prefs.Preferences+.userRoot(..)) ||
                    call(* java.util.prefs.Preferences+.systemRoot(..)))
            && !within(de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationArchiveCallSite);

    // Note: Files.newByteChannel is intentionally NOT included here.
    // It is already in filesReadMethods, and the actual operation (read/write/create)
    // is determined by the OpenOptions passed to it. The deriveActionChecks() method handles
    // the semantic classification based on the options. Methods like Files.readString() and
    // Files.readAllLines() internally call newByteChannel() with default READ-only options.
    // Note: Files.move is included here, in filesReadMethods and in filesDeleteMethods
    // because it reads and deletes the source and writes the target. Files.copy does not delete the
    // source, but IS also registered in filesReadMethods above: its source path must be
    // checked as READ, not OVERWRITE (I-114/TD-063) - see checkCopyOrTransferSpecialCase.
    pointcut filesWriteMethods():
            (call(* java.nio.file.Files+.write(..)) ||
                    call(* java.nio.file.Files+.writeString(..)) ||
                    call(* java.nio.file.Files+.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files+.newOutputStream(..)) ||
                    call(* java.nio.file.Files+.copy(..)) ||
                    call(* java.nio.file.Files+.move(..)) ||
                    call(* java.nio.file.Files+.setAttribute(..)) ||
                    call(* java.nio.file.Files+.setLastModifiedTime(..)) ||
                    call(* java.nio.file.Files+.setOwner(..)) ||
                    call(* java.nio.file.Files+.setPosixFilePermissions(..)));

    // Note: Files.newByteChannel is intentionally NOT included here.
    // It is already in filesReadMethods, and the actual operation is determined
    // by the OpenOptions. The deriveActionChecks() method handles semantic classification.
    // Methods like Files.readString() and Files.readAllLines() internally call
    // newByteChannel() with default READ-only options.
    // Note: Files.write and Files.writeString are NOT included here because they default to
    // TRUNCATE_EXISTING behaviour. When called with CREATE or CREATE_NEW options, the
    // deriveActionChecks method will detect these options and properly classify as "create".
    pointcut filesCreateMethods():
            (call(* java.nio.file.Files+.createDirectory(..)) ||
                    call(* java.nio.file.Files+.createDirectories(..)) ||
                    call(* java.nio.file.Files+.createFile(..)) ||
                    call(* java.nio.file.Files+.createLink(..)) ||
                    call(* java.nio.file.Files+.createSymbolicLink(..)) ||
                    call(* java.nio.file.Files+.createTempDirectory(..)) ||
                    call(* java.nio.file.Files+.createTempFile(..)) ||
                    call(* java.nio.file.Files+.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files+.newOutputStream(..)));

    pointcut filesExecuteMethods(): if(false);

    pointcut filesDeleteMethods():
            (call(* java.nio.file.Files+.delete(..)) ||
                    call(* java.nio.file.Files+.deleteIfExists(..)) ||
                    call(* java.nio.file.Files+.move(..)) ||
                    call(* java.nio.file.SecureDirectoryStream+.deleteFile(..)) ||
                    call(* java.nio.file.SecureDirectoryStream+.deleteDirectory(..)));

    pointcut fileSystemReadMethods(): if(false);

    pointcut fileSystemWriteMethods(): if(false);

    pointcut fileSystemExecuteMethods(): if(false);

    pointcut fileChannelExecuteMethods(): if(false);

    // Note: FileChannel.transferTo is also registered in fileChannelWriteMethods below:
    // the receiver (the channel being read FROM) must be checked as READ - see
    // checkCopyOrTransferSpecialCase, which splits each firing to the correct side.
    pointcut fileChannelReadMethods():
            (call(* java.nio.channels.AsynchronousFileChannel+.open(..)) ||
                    call(* java.nio.channels.AsynchronousFileChannel+.read(..)) ||
                    call(* java.nio.channels.FileChannel+.map(..)) ||
                    call(* java.nio.channels.FileChannel+.open(..)) ||
                    call(* java.nio.channels.FileChannel+.transferTo(..)) ||
                    call(* java.nio.channels.SeekableByteChannel+.read(..)));

    // Note: FileChannel.open and AsynchronousFileChannel.open are intentionally NOT included here.
    // These methods are already in fileChannelReadMethods, and the actual operation (read/write/create)
    // is determined by the OpenOptions passed to them. The deriveActionChecks() method handles
    // the semantic classification based on the options. Subsequent write()/truncate() calls
    // will be caught by fileChannelWriteMethods if needed.
    pointcut fileChannelCreateMethods(): if(false);

    // Note: FileChannel.open and AsynchronousFileChannel.open are intentionally NOT included here.
    // These methods are already in fileChannelReadMethods, and the actual operation is determined
    // by the OpenOptions. The deriveActionChecks() method handles semantic classification.
    // Note: FileChannel.map is intentionally NOT included here. It's in fileChannelReadMethods,
    // and the actual operation is determined by the MapMode parameter. MapMode.READ_ONLY does
    // not modify the file. The deriveActionChecks() method handles semantic classification.
    // Note: FileChannel.transferFrom was previously not intercepted by either backend at
    // all (I-114/TD-063). Its receiver (the channel being written TO) is checked as
    // OVERWRITE here; unlike transferTo it is not also registered in
    // fileChannelReadMethods, since its src argument is itself a channel and is not
    // resolvable to a checkable path with the existing machinery - see
    // checkCopyOrTransferSpecialCase's class-level Javadoc.
    pointcut fileChannelWriteMethods():
            (call(* java.nio.channels.AsynchronousFileChannel+.write(..)) ||
                    call(* java.nio.channels.AsynchronousFileChannel+.truncate(..)) ||
                    call(* java.nio.channels.FileChannel+.write(..)) ||
                    call(* java.nio.channels.FileChannel+.truncate(..)) ||
                    call(* java.nio.channels.FileChannel+.transferTo(..)) ||
                    call(* java.nio.channels.FileChannel+.transferFrom(..)));


    pointcut writerMethods(): (call(java.io.Writer+.new(..)));

    pointcut fileHandlerMethods():
            (call(java.util.logging.FileHandler+.new(..)) ||
                    call(* java.util.logging.FileHandler+.publish(..)) ||
                    call(* java.util.logging.FileHandler+.close(..)));

    pointcut midiSystemMethods():
            call(* javax.sound.midi.MidiSystem+.getSoundbank(..));

    pointcut fileSystemsReadMethods(): if(false);

    pointcut defaultFileSystemExecuteMethods(): if(false);

    // The provider methods that open a file are read here. Their options decide
    // whether they read, write or create, as for Files.newByteChannel. copy and move
    // are also registered below; checkCopyOrTransferSpecialCase splits them by side.
    pointcut fileSystemProviderReadMethods():
            (call(* java.nio.file.spi.FileSystemProvider+.newDirectoryStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newInputStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newByteChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newAsynchronousFileChannel(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.copy(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.move(..)));

    pointcut fileSystemProviderCreateMethods():
            call(* java.nio.file.spi.FileSystemProvider+.createDirectory(..));

    pointcut fileSystemProviderWriteMethods():
            (call(* java.nio.file.spi.FileSystemProvider+.copy(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.move(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.newOutputStream(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.setAttribute(..)));

    pointcut fileSystemProviderExecuteMethods(): if(false);

    pointcut fileSystemProviderDeleteMethods():
            (call(* java.nio.file.spi.FileSystemProvider+.delete(..)) ||
                    call(* java.nio.file.spi.FileSystemProvider+.move(..)));

    pointcut processBuilderRedirectReadMethods():
            (call(* java.lang.ProcessBuilder.redirectInput(java.io.File)) ||
                    call(* java.lang.ProcessBuilder.Redirect.from(java.io.File)));

    pointcut processBuilderRedirectWriteMethods():
            (call(* java.lang.ProcessBuilder.redirectOutput(java.io.File)) ||
                    call(* java.lang.ProcessBuilder.redirectError(java.io.File)) ||
                    call(* java.lang.ProcessBuilder.Redirect.to(java.io.File)) ||
                    call(* java.lang.ProcessBuilder.Redirect.appendTo(java.io.File)));

    pointcut scannerInitMethods():
            (call(java.util.Scanner+.new(java.io.File, ..)) ||
                    call(java.util.Scanner+.new(java.nio.file.Path, ..)));

    pointcut desktopExecuteMethods():
            (call(* java.awt.Desktop+.open(..)) ||
                    call(* java.awt.Desktop+.edit(..)) ||
                    call(* java.awt.Desktop+.print(..)) ||
                    call(* java.awt.Desktop+.browse(..)) ||
                    call(* java.awt.Desktop+.browseFileDirectory(..)));
}
