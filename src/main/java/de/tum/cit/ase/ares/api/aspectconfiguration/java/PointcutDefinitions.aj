package de.tum.cit.ase.ares.api.aspectconfiguration.java;

public aspect PointcutDefinitions {

        // Pointcut for FileInputStream constructors
        pointcut fileInputStreamConstructorMethods() :
                (execution(java.io.FileInputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileOutputStream constructors
        pointcut fileOutputStreamConstructorMethods() :
                (execution(java.io.FileOutputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileReader constructors
        pointcut fileReaderConstructorMethods() :
                (execution(java.io.FileReader.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileWriter constructors
        pointcut fileWriterConstructorMethods() :
                (execution(java.io.FileWriter.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterOutputStream constructor
        pointcut filterOutputStreamConstructorMethods() :
                (execution(java.io.FilterOutputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputStream constructor
        pointcut inputStreamConstructorMethods() :
                (execution(java.io.InputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputFilter$Config constructor
        pointcut objectInputFilterConfigConstructorMethods() :
                (execution(java.io.ObjectInputFilter$Config.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream$GetField constructor
        pointcut objectInputStreamGetFieldConstructorMethods() :
                (execution(java.io.ObjectInputStream$GetField.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectOutputStream$PutField constructor
        pointcut objectOutputStreamPutFieldConstructorMethods() :
                (execution(java.io.ObjectOutputStream$PutField.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectStreamClass constructors
        pointcut objectStreamClassConstructorMethods() :
                (execution(java.io.ObjectStreamClass.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectStreamField constructors
        pointcut objectStreamFieldConstructorMethods() :
                (execution(java.io.ObjectStreamField.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for OutputStream constructor
        pointcut outputStreamConstructorMethods() :
                (execution(java.io.OutputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintStream constructors
        pointcut printStreamConstructorMethods() :
                (execution(java.io.PrintStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintWriter constructors
        pointcut printWriterConstructorMethods() :
                (execution(java.io.PrintWriter.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RandomAccessFile constructors
        pointcut randomAccessFileConstructorMethods() :
                (execution(java.io.RandomAccessFile.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Reader constructors
        pointcut readerConstructorMethods() :
                (execution(java.io.Reader.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for StreamTokenizer constructor
        pointcut streamTokenizerConstructorMethods() :
                (execution(java.io.StreamTokenizer.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Writer constructors
        pointcut writerConstructorMethods() :
                (execution(java.io.Writer.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Buffer constructors
        pointcut bufferConstructorMethods() :
                (execution(java.nio.Buffer.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ByteOrder constructor
        pointcut byteOrderConstructorMethods() :
                (execution(java.nio.ByteOrder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousChannelGroup constructor
        pointcut asynchronousChannelGroupConstructorMethods() :
                (execution(java.nio.channels.AsynchronousChannelGroup.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousFileChannel constructor
        pointcut asynchronousFileChannelConstructorMethods() :
                (execution(java.nio.channels.AsynchronousFileChannel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousServerSocketChannel constructor
        pointcut asynchronousServerSocketChannelConstructorMethods() :
                (execution(java.nio.channels.AsynchronousServerSocketChannel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousSocketChannel constructor
        pointcut asynchronousSocketChannelConstructorMethods() :
                (execution(java.nio.channels.AsynchronousSocketChannel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Channels constructor
        pointcut channelsConstructorMethods() :
                (execution(java.nio.channels.Channels.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileChannel$MapMode constructor
        pointcut fileChannelMapModeConstructorMethods() :
                (execution(java.nio.channels.FileChannel$MapMode.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileLock constructors
        pointcut fileLockConstructorMethods() :
                (execution(java.nio.channels.FileLock.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MembershipKey constructor
        pointcut membershipKeyConstructorMethods() :
                (execution(java.nio.channels.MembershipKey.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Pipe constructor
        pointcut pipeConstructorMethods() :
                (execution(java.nio.channels.Pipe.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SelectionKey constructor
        pointcut selectionKeyConstructorMethods() :
                (execution(java.nio.channels.SelectionKey.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Selector constructor
        pointcut selectorConstructorMethods() :
                (execution(java.nio.channels.Selector.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AbstractInterruptibleChannel constructors
        pointcut abstractInterruptibleChannelConstructorMethods() :
                (execution(java.nio.channels.spi.AbstractInterruptibleChannel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AbstractSelectableChannel constructors
        pointcut abstractSelectableChannelConstructorMethods() :
                (execution(java.nio.channels.spi.AbstractSelectableChannel.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AsynchronousChannelProvider constructor
        pointcut asynchronousChannelProviderConstructorMethods() :
                (execution(java.nio.channels.spi.AsynchronousChannelProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SelectorProvider constructor
        pointcut selectorProviderConstructorMethods() :
                (execution(java.nio.channels.spi.SelectorProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Charset constructors
        pointcut charsetConstructorMethods() :
                (execution(java.nio.charset.Charset.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CharsetDecoder constructor
        pointcut charsetDecoderConstructorMethods() :
                (execution(java.nio.charset.CharsetDecoder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CharsetEncoder constructor
        pointcut charsetEncoderConstructorMethods() :
                (execution(java.nio.charset.CharsetEncoder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CoderResult constructor
        pointcut coderResultConstructorMethods() :
                (execution(java.nio.charset.CoderResult.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CodingErrorAction constructor
        pointcut codingErrorActionConstructorMethods() :
                (execution(java.nio.charset.CodingErrorAction.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for StandardCharsets constructor
        pointcut standardCharsetsConstructorMethods() :
                (execution(java.nio.charset.StandardCharsets.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for CharsetProvider constructor
        pointcut charsetProviderConstructorMethods() :
                (execution(java.nio.charset.spi.CharsetProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileStore constructor
        pointcut fileStoreConstructorMethods() :
                (execution(java.nio.file.FileStore.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystem constructor
        pointcut fileSystemConstructorMethods() :
                (execution(java.nio.file.FileSystem.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystems constructor
        pointcut fileSystemsConstructorMethods() :
                (execution(java.nio.file.FileSystems.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Files constructor
        pointcut filesConstructorMethods() :
                (execution(java.nio.file.Files.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Paths constructor
        pointcut pathsConstructorMethods() :
                (execution(java.nio.file.Paths.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SimpleFileVisitor constructor
        pointcut simpleFileVisitorConstructorMethods() :
                (execution(java.nio.file.SimpleFileVisitor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for StandardWatchEventKinds constructor
        pointcut standardWatchEventKindsConstructorMethods() :
                (execution(java.nio.file.StandardWatchEventKinds.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AclEntry$Builder constructor
        pointcut aclEntryBuilderConstructorMethods() :
                (execution(java.nio.file.attribute.AclEntry$Builder.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AclEntry constructor
        pointcut aclEntryConstructorMethods() :
                (execution(java.nio.file.attribute.AclEntry.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileTime constructor
        pointcut fileTimeConstructorMethods() :
                (execution(java.nio.file.attribute.FileTime.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PosixFilePermissions constructor
        pointcut posixFilePermissionsConstructorMethods() :
                (execution(java.nio.file.attribute.PosixFilePermissions.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UserPrincipalLookupService constructor
        pointcut userPrincipalLookupServiceConstructorMethods() :
                (execution(java.nio.file.attribute.UserPrincipalLookupService.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystemProvider constructor
        pointcut fileSystemProviderConstructorMethods() :
                (execution(java.nio.file.spi.FileSystemProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileTypeDetector constructor
        pointcut fileTypeDetectorConstructorMethods() :
                (execution(java.nio.file.spi.FileTypeDetector.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DatagramSocketAdaptor constructor
        pointcut datagramSocketAdaptorConstructorMethods() :
                (execution(sun.nio.ch.DatagramSocketAdaptor.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DefaultAsynchronousChannelProvider constructor
        pointcut defaultAsynchronousChannelProviderConstructorMethods() :
                (execution(sun.nio.ch.DefaultAsynchronousChannelProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DefaultSelectorProvider constructor
        pointcut defaultSelectorProviderConstructorMethods() :
                (execution(sun.nio.ch.DefaultSelectorProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileChannelImpl constructor
        pointcut fileChannelImplConstructorMethods() :
                (execution(sun.nio.ch.FileChannelImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileKey constructor
        pointcut fileKeyConstructorMethods() :
                (execution(sun.nio.ch.FileKey.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IOStatus constructor
        pointcut ioStatusConstructorMethods() :
                (execution(sun.nio.ch.IOStatus.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IOUtil constructor
        pointcut ioUtilConstructorMethods() :
                (execution(sun.nio.ch.IOUtil.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for NativeThread constructor
        pointcut nativeThreadConstructorMethods() :
                (execution(sun.nio.ch.NativeThread.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Net constructor
        pointcut netConstructorMethods() :
                (execution(sun.nio.ch.Net.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for NioSocketImpl constructor
        pointcut nioSocketImplConstructorMethods() :
                (execution(sun.nio.ch.NioSocketImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Poller constructor
        pointcut pollerConstructorMethods() :
                (execution(sun.nio.ch.Poller.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Streams constructor
        pointcut streamsConstructorMethods() :
                (execution(sun.nio.ch.Streams.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ThreadPool constructor
        pointcut threadPoolConstructorMethods() :
                (execution(sun.nio.ch.ThreadPool.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Util constructor
        pointcut utilConstructorMethods() :
                (execution(sun.nio.ch.Util.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ResultContainer constructor
        pointcut resultContainerConstructorMethods() :
                (execution(sun.nio.ch.sctp.ResultContainer.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SctpChannelImpl constructors
        pointcut sctpChannelImplConstructorMethods() :
                (execution(sun.nio.ch.sctp.SctpChannelImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SctpMultiChannelImpl constructors
        pointcut sctpMultiChannelImplConstructorMethods() :
                (execution(sun.nio.ch.sctp.SctpMultiChannelImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SctpNet constructor
        pointcut sctpNetConstructorMethods() :
                (execution(sun.nio.ch.sctp.SctpNet.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SctpServerChannelImpl constructors
        pointcut sctpServerChannelImplConstructorMethods() :
                (execution(sun.nio.ch.sctp.SctpServerChannelImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SctpStdSocketOption constructor
        pointcut sctpStdSocketOptionConstructorMethods() :
                (execution(sun.nio.ch.sctp.SctpStdSocketOption.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DefaultFileSystemProvider constructor
        pointcut defaultFileSystemProviderConstructorMethods() :
                (execution(sun.nio.fs.DefaultFileSystemProvider.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DefaultFileTypeDetector constructor
        pointcut defaultFileTypeDetectorConstructorMethods() :
                (execution(sun.nio.fs.DefaultFileTypeDetector.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ExtendedOptions$InternalOption constructor
        pointcut extendedOptionsInternalOptionConstructorMethods() :
                (execution(sun.nio.fs.ExtendedOptions$InternalOption.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ExtendedOptions constructor
        pointcut extendedOptionsConstructorMethods() :
                (execution(sun.nio.fs.ExtendedOptions.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Globs constructor
        pointcut globsConstructorMethods() :
                (execution(sun.nio.fs.Globs.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixUserPrincipals constructor
        pointcut unixUserPrincipalsConstructorMethods() :
                (execution(sun.nio.fs.UnixUserPrincipals.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File method calls
        pointcut fileMethodCalls() :
                (execution(* java.io.File.canExecute(..)) ||
                        execution(* java.io.File.canRead(..)) ||
                        execution(* java.io.File.canWrite(..)) ||
                        execution(* java.io.File.createNewFile(..)) ||
                        execution(* java.io.File.createTempFile(..)) ||
                        execution(* java.io.File.delete(..)) ||
                        execution(* java.io.File.deleteOnExit(..)) ||
                        execution(* java.io.File.exists(..)) ||
                        execution(* java.io.File.getFreeSpace(..)) ||
                        execution(* java.io.File.getTotalSpace(..)) ||
                        execution(* java.io.File.getUsableSpace(..)) ||
                        execution(* java.io.File.isDirectory(..)) ||
                        execution(* java.io.File.isFile(..)) ||
                        execution(* java.io.File.isHidden(..)) ||
                        execution(* java.io.File.lastModified(..)) ||
                        execution(* java.io.File.length(..)) ||
                        execution(* java.io.File.list(..)) ||
                        execution(* java.io.File.listFiles(..)) ||
                        execution(* java.io.File.listRoots(..)) ||
                        execution(* java.io.File.mkdir(..)) ||
                        execution(* java.io.File.mkdirs(..)) ||
                        execution(* java.io.File.renameTo(..)) ||
                        execution(* java.io.File.setExecutable(..)) ||
                        execution(* java.io.File.setLastModified(..)) ||
                        execution(* java.io.File.setReadOnly(..)) ||
                        execution(* java.io.File.setReadable(..)) ||
                        execution(* java.io.File.setWritable(..)) ||
                        execution(* java.io.File.toURI(..)) ||
                        execution(* java.io.File.toURL(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileDescriptor.closeAll methods
        pointcut fileDescriptorCloseAllMethods() :
                (execution(* java.io.FileDescriptor.closeAll(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FilterInputStream.read method
        pointcut filterInputStreamReadMethod() :
                (execution(* java.io.FilterInputStream.read(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PushbackInputStream methods
        pointcut pushbackInputStreamMethods() :
                (execution(* java.io.PushbackInputStream.read(..)) ||
                        execution(* java.io.PushbackInputStream.skip(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Files.asUncheckedRunnable method
        pointcut filesAsUncheckedRunnableMethod() :
                (execution(* java.nio.file.Files.asUncheckedRunnable(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Files.write method with Charset
        pointcut filesWriteWithCharsetMethod() :
                (execution(* java.nio.file.Files.write(..)) && args(java.nio.file.Path, java.lang.Iterable, java.nio.charset.Charset, ..)) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileChannelImpl.implCloseChannel method
        pointcut fileChannelImplImplCloseChannelMethod() :
                (execution(* sun.nio.ch.FileChannelImpl.implCloseChannel(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for IOUtil.releaseScopes method
        pointcut ioUtilReleaseScopesMethod() :
                (execution(* sun.nio.ch.IOUtil.releaseScopes(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixFileSystemProvider methods
        pointcut unixFileSystemProviderMethods() :
                (execution(* sun.nio.fs.UnixFileSystemProvider.checkAccess(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.createDirectory(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.createLink(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.createSymbolicLink(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.exists(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.getFileStore(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.implDelete(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.isHidden(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.isSameFile(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.newDirectoryStream(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.readAttributesIfExists(..)) ||
                        execution(* sun.nio.fs.UnixFileSystemProvider.readSymbolicLink(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputFilter$Config.<clinit>
        pointcut objectInputFilterConfigClinit() :
                (execution(* java.io.ObjectInputFilter$Config.*(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream.<clinit>
        pointcut objectInputStreamClinit() :
                (execution(* java.io.ObjectInputStream.*(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystemProvider.<clinit>
        pointcut fileSystemProviderClinit() :
                (execution(* java.nio.file.spi.FileSystemProvider.*(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DocumentHandler methods
        pointcut documentHandlerMethods() :
                (execution(* com.sun.beans.decoder.DocumentHandler.parse(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TemplatesImpl methods
        pointcut templatesImplMethods() :
                (execution(* com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.readObject(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XPathFactoryImpl constructors
        pointcut xPathFactoryImplConstructorMethods() :
                (execution(com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Desktop methods
        pointcut desktopMethods() :
                (execution(* java.awt.Desktop.moveToTrash(..)) ||
                        execution(* java.awt.Desktop.print(..)) ||
                        execution(* java.awt.Desktop.setPrintFileHandler(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Font methods
        pointcut fontMethods() :
                (execution(* java.awt.Font.checkFontFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputEvent methods
        pointcut inputEventMethods() :
                (execution(* java.awt.event.InputEvent.canAccessSystemClipboard(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Introspector methods
        pointcut introspectorMethods() :
                (execution(* java.beans.Introspector.setBeanInfoSearchPath(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PropertyEditorManager methods
        pointcut propertyEditorManagerMethods() :
                (execution(* java.beans.PropertyEditorManager.setEditorSearchPath(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File$TempDirectory methods
        pointcut fileTempDirectoryMethods() :
                (execution(* java.io.File$TempDirectory.generateFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for File methods
        pointcut fileMethods() :
                (execution(* java.io.File.canExecute(..)) ||
                        execution(* java.io.File.canRead(..)) ||
                        execution(* java.io.File.canWrite(..)) ||
                        execution(* java.io.File.createNewFile(..)) ||
                        execution(* java.io.File.createTempFile(..)) ||
                        execution(* java.io.File.delete(..)) ||
                        execution(* java.io.File.deleteOnExit(..)) ||
                        execution(* java.io.File.exists(..)) ||
                        execution(* java.io.File.getFreeSpace(..)) ||
                        execution(* java.io.File.getTotalSpace(..)) ||
                        execution(* java.io.File.getUsableSpace(..)) ||
                        execution(* java.io.File.isDirectory(..)) ||
                        execution(* java.io.File.isFile(..)) ||
                        execution(* java.io.File.isHidden(..)) ||
                        execution(* java.io.File.lastModified(..)) ||
                        execution(* java.io.File.length(..)) ||
                        execution(* java.io.File.mkdir(..)) ||
                        execution(* java.io.File.normalizedList(..)) ||
                        execution(* java.io.File.renameTo(..)) ||
                        execution(* java.io.File.setExecutable(..)) ||
                        execution(* java.io.File.setLastModified(..)) ||
                        execution(* java.io.File.setReadOnly(..)) ||
                        execution(* java.io.File.setReadable(..)) ||
                        execution(* java.io.File.setWritable(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputFilter$Config methods
        pointcut objectInputFilterConfigMethods() :
                (execution(* java.io.ObjectInputFilter$Config.setSerialFilter(..)) ||
                        execution(* java.io.ObjectInputFilter$Config.setSerialFilterFactory(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream methods
        pointcut objectInputStreamMethods() :
                (execution(* java.io.ObjectInputStream.enableResolveObject(..)) ||
                        execution(* java.io.ObjectInputStream.setObjectInputFilter(..)) ||
                        execution(* java.io.ObjectInputStream.verifySubclass(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectOutputStream methods
        pointcut objectOutputStreamMethods() :
                (execution(* java.io.ObjectOutputStream.enableReplaceObject(..)) ||
                        execution(* java.io.ObjectOutputStream.verifySubclass(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectInputStream constructors
        pointcut objectInputStreamConstructorMethods() :
                (execution(java.io.ObjectInputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectOutputStream constructors
        pointcut objectOutputStreamConstructorMethods() :
                (execution(java.io.ObjectOutputStream.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);



        // Pointcut for ObjectStreamClass methods
        pointcut objectStreamClassMethods() :
                (execution(* java.io.ObjectStreamClass.forClass(..)) ||
                        execution(* java.io.ObjectStreamClass.getProtectionDomains(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ObjectStreamField methods
        pointcut objectStreamFieldMethods() :
                (execution(* java.io.ObjectStreamField.getType(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixFileSystem methods
        pointcut unixFileSystemMethods() :
                (execution(* java.io.UnixFileSystem.listRoots(..)) ||
                        execution(* java.io.UnixFileSystem.resolve(..)) || execution(* sun.nio.fs.UnixFileSystem.copy(..)) ||
                        execution(* sun.nio.fs.UnixFileSystem.getFileStores(..)) ||
                        execution(* sun.nio.fs.UnixFileSystem.move(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URL constructors
        pointcut urlConstructorMethods() :
                (execution(java.net.URL.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URL methods
        pointcut urlMethodExecutions() :
                (execution(* java.net.URL.setURLStreamHandlerFactory(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URLConnection methods
        pointcut urlConnectionMethods() :
                (execution(* java.net.URLConnection.setFileNameMap(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URLStreamHandlerProvider methods
        pointcut urlStreamHandlerProviderMethods() :
                (execution(* java.net.spi.URLStreamHandlerProvider.checkPermission(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileTreeWalker methods
        pointcut fileTreeWalkerMethods() :
                (execution(* java.nio.file.FileTreeWalker.getAttributes(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for TempFileHelper methods
        pointcut tempFileHelperMethods() :
                (execution(* java.nio.file.TempFileHelper.create(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileSystemProvider methods
        pointcut fileSystemProviderMethods() :
                (execution(* java.nio.file.spi.FileSystemProvider.checkPermission(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileTypeDetector methods
        pointcut fileTypeDetectorMethods() :
                (execution(* java.nio.file.spi.FileTypeDetector.checkPermission(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for LogStream methods
        pointcut logStreamMethods() :
                (execution(* java.rmi.server.LogStream.setDefaultStream(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DriverManager methods
        pointcut driverManagerMethods() :
                (execution(* java.sql.DriverManager.setLogStream(..)) ||
                        execution(* java.sql.DriverManager.setLogWriter(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ZipFile constructors
        pointcut zipFileConstructorMethods() :
                (execution(java.util.zip.ZipFile.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);


        // Pointcut for StreamPrintServiceFactory methods
        pointcut streamPrintServiceFactoryMethods() :
                (execution(* javax.print.StreamPrintServiceFactory$1.run(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for XPathFactoryFinder methods
        pointcut xPathFactoryFinderMethods() :
                (execution(* javax.xml.xpath.XPathFactoryFinder.createClass(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for JrtFileSystemProvider methods
        pointcut jrtFileSystemProviderMethods() :
                (execution(* jdk.internal.jrtfs.JrtFileSystemProvider.checkPermission(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for BuiltinClassLoader methods
        pointcut builtinClassLoaderMethods() :
                (execution(* jdk.internal.loader.BuiltinClassLoader.findClassOnClassPathOrNull(..)) ||
                        execution(* jdk.internal.loader.BuiltinClassLoader.findResourceAsStream(..)) ||
                        execution(* jdk.internal.loader.BuiltinClassLoader.findResourceOnClassPath(..)) ||
                        execution(* jdk.internal.loader.BuiltinClassLoader.findResourcesOnClassPath(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for URLClassPath methods
        pointcut urlClassPathMethods() :
                (execution(* jdk.internal.loader.URLClassPath$JarLoader.checkJar(..)) ||
                        execution(* jdk.internal.loader.URLClassPath.check(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ModuleReferences$ExplodedModuleReader constructor
        pointcut moduleReferencesExplodedModuleReaderConstructorMethods() :
                (execution(jdk.internal.module.ModuleReferences$ExplodedModuleReader.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for SystemModuleFinders$SystemModuleReader methods
        pointcut systemModuleFindersSystemModuleReaderMethods() :
                (execution(* jdk.internal.module.SystemModuleFinders$SystemModuleReader.checkPermissionToConnect(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RequestPublishers$FilePublisher methods
        pointcut requestPublishersFilePublisherMethods() :
                (execution(* jdk.internal.net.http.RequestPublishers$FilePublisher.create(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ResponseBodyHandlers methods
        pointcut responseBodyHandlersMethods() :
                (execution(* jdk.internal.net.http.ResponseBodyHandlers$FileDownloadBodyHandler.create(..)) ||
                        execution(* jdk.internal.net.http.ResponseBodyHandlers$PathBodyHandler.create(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ResponseSubscribers methods
        pointcut responseSubscribersMethods() :
                (execution(* jdk.internal.net.http.ResponseSubscribers$PathSubscriber.create(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for WriteableUserPath methods
        pointcut writeableUserPathMethods() :
                (execution(* jdk.jfr.internal.WriteableUserPath.doPrivilegedIO(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Channels methods
        pointcut channelsMethods() :
                (execution(* jdk.nio.Channels.readWriteSelectableChannel(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for Main methods
        pointcut mainMethods() :
                (execution(* jdk.tools.jlink.internal.Main.run(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for DataTransferer methods
        pointcut dataTransfererMethods() :
                (execution(* sun.awt.datatransfer.DataTransferer.castToFiles(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileImageSource constructor
        pointcut fileImageSourceConstructorMethods() :
                (execution(sun.awt.image.FileImageSource.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ImageConsumerQueue constructor
        pointcut imageConsumerQueueConstructorMethods() :
                (execution(sun.awt.image.ImageConsumerQueue.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for InputStreamImageSource methods
        pointcut inputStreamImageSourceMethods() :
                (execution(* sun.awt.image.InputStreamImageSource.addConsumer(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ShellFolderManager methods
        pointcut shellFolderManagerMethods() :
                (execution(* sun.awt.shell.ShellFolderManager.checkFile(..)) ||
                        execution(* sun.awt.shell.ShellFolderManager.checkFiles(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileFont methods
        pointcut fileFontMethods() :
                (execution(* sun.font.FileFont.getPublicFileName(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FileServerHandler constructor
        pointcut fileServerHandlerConstructorMethods() :
                (execution(sun.net.httpserver.simpleserver.FileServerHandler.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for MimeTable methods
        pointcut mimeTableMethods() :
                (execution(* sun.net.www.MimeTable.saveAsProperties(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for HttpURLConnection methods
        pointcut httpURLConnectionMethods() :
                (execution(* sun.net.www.protocol.http.HttpURLConnection.checkURLFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for JarFileFactory methods
        pointcut jarFileFactoryMethods() :
                (execution(* sun.net.www.protocol.jar.JarFileFactory.getCachedJarFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServerSocketChannelImpl methods
        pointcut serverSocketChannelImplMethods() :
                (execution(* sun.nio.ch.ServerSocketChannelImpl.finishAccept(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixAsynchronousServerSocketChannelImpl methods
        pointcut unixAsynchronousServerSocketChannelImplMethods() :
                (execution(* sun.nio.ch.UnixAsynchronousServerSocketChannelImpl.finishAccept(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for AbstractUserDefinedFileAttributeView methods
        pointcut abstractUserDefinedFileAttributeViewMethods() :
                (execution(* sun.nio.fs.AbstractUserDefinedFileAttributeView.checkAccess(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixChannelFactory methods
        pointcut unixChannelFactoryMethods() :
                (execution(* sun.nio.fs.UnixChannelFactory.open(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixFileAttributeViews$Posix methods
        pointcut unixFileAttributeViewsPosixMethods() :
                (execution(* sun.nio.fs.UnixFileAttributeViews$Posix.checkReadExtended(..)) ||
                        execution(* sun.nio.fs.UnixFileAttributeViews$Posix.checkWriteExtended(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixFileSystem$1 methods
        pointcut unixFileSystem1Methods() :
                (execution(* sun.nio.fs.UnixFileSystem$1.iterator(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixFileSystem$FileStoreIterator methods
        pointcut unixFileSystemFileStoreIteratorMethods() :
                (execution(* sun.nio.fs.UnixFileSystem$FileStoreIterator.readNext(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixPath methods
        pointcut unixPathMethods() :
                (execution(* sun.nio.fs.UnixPath.checkDelete(..)) ||
                        execution(* sun.nio.fs.UnixPath.checkRead(..)) ||
                        execution(* sun.nio.fs.UnixPath.checkWrite(..)) ||
                        execution(* sun.nio.fs.UnixPath.toAbsolutePath(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixSecureDirectoryStream$BasicFileAttributeViewImpl methods
        pointcut unixSecureDirectoryStreamBasicFileAttributeViewImplMethods() :
                (execution(* sun.nio.fs.UnixSecureDirectoryStream$BasicFileAttributeViewImpl.checkWriteAccess(..)) ||
                        execution(* sun.nio.fs.UnixSecureDirectoryStream$BasicFileAttributeViewImpl.readAttributes(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixSecureDirectoryStream$PosixFileAttributeViewImpl methods
        pointcut unixSecureDirectoryStreamPosixFileAttributeViewImplMethods() :
                (execution(* sun.nio.fs.UnixSecureDirectoryStream$PosixFileAttributeViewImpl.checkWriteAndUserAccess(..)) ||
                        execution(* sun.nio.fs.UnixSecureDirectoryStream$PosixFileAttributeViewImpl.readAttributes(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixSecureDirectoryStream methods
        pointcut unixSecureDirectoryStreamMethods() :
                (execution(* sun.nio.fs.UnixSecureDirectoryStream.implDelete(..)) ||
                        execution(* sun.nio.fs.UnixSecureDirectoryStream.move(..)) ||
                        execution(* sun.nio.fs.UnixSecureDirectoryStream.newDirectoryStream(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for UnixUserDefinedFileAttributeView methods
        pointcut unixUserDefinedFileAttributeViewMethods() :
                (execution(* sun.nio.fs.UnixUserDefinedFileAttributeView.delete(..)) ||
                        execution(* sun.nio.fs.UnixUserDefinedFileAttributeView.list(..)) ||
                        execution(* sun.nio.fs.UnixUserDefinedFileAttributeView.read(..)) ||
                        execution(* sun.nio.fs.UnixUserDefinedFileAttributeView.size(..)) ||
                        execution(* sun.nio.fs.UnixUserDefinedFileAttributeView.write(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for PrintJob2D methods
        pointcut printJob2DMethods() :
                (execution(* sun.print.PrintJob2D.throwPrintToFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RasterPrinterJob methods
        pointcut rasterPrinterJobMethods() :
                (execution(* sun.print.RasterPrinterJob.throwPrintToFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ServiceDialog$PrintServicePanel methods
        pointcut serviceDialogPrintServicePanelMethods() :
                (execution(* sun.print.ServiceDialog$PrintServicePanel.throwPrintToFile(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for RegistryImpl constructors
        pointcut registryImplConstructorMethods() :
                (execution(sun.rmi.registry.RegistryImpl.new(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for ConfigFile$Spi methods
        pointcut configFileSpiMethods() :
                (execution(* sun.security.provider.ConfigFile$Spi.engineRefresh(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);


}