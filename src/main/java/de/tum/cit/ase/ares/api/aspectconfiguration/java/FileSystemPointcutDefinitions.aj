package de.tum.cit.ase.ares.api.aspectconfiguration.java;

public aspect FileSystemPointcutDefinitions {

    pointcut unixToolkitLoadGtkMethods() :
            (call(* sun.awt.UNIXToolkit.load_gtk(..)) ||
                    call(* sun.awt.UNIXToolkit.load_gtk_icon(..)) ||
                    call(* sun.awt.UNIXToolkit.unload_gtk(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut xDesktopPeerInitMethods() :
            call(* sun.awt.X11.XDesktopPeer.init(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut xRobotPeerLoadNativeLibrariesMethods() :
            call(* sun.awt.X11.XRobotPeer.loadNativeLibraries(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut xTaskbarPeerInitMethods() :
            call(* sun.awt.X11.XTaskbarPeer.init(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut gifImageDecoderParseImageMethods() :
            call(* sun.awt.image.GifImageDecoder.parseImage(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut jpegImageDecoderReadImageMethod() :
            call(* sun.awt.image.JPEGImageDecoder.readImage(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut cupsPrinterInitIDsMethod() :
            call(* sun.print.CUPSPrinter.initIDs(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileInputStreamReadMethods() :
            (call(* java.io.FileInputStream.read(..)) ||
                    call(* java.io.FileInputStream.available(..)) ||
                    call(* java.io.FileInputStream.getFD(..)) ||
                    call(* java.io.FileInputStream.getChannel(..))) ||
                    call(* java.io.FileInputStream.skip(..)) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileInputStreamCloseMethods() :
            call(* java.io.FileInputStream.close(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileOutputStreamWriteMethods() :
            call(* java.io.FileOutputStream.write(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileOutputStreamCloseMethods() :
            call(* java.io.FileOutputStream.close(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileOutputStreamGetChannelMethods() :
            call(* java.io.FileOutputStream.getChannel(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileDescriptorMethods() :
            (call(* java.io.FileDescriptor.sync(..)) ||
                    call(* java.io.FileDescriptor.close(..)))&&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileCleanableCleanupMethods() :
            call(* java.io.FileCleanable.cleanup(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut randomAccessFileReadMethods() :
            (call(* java.io.RandomAccessFile.read(..)) ||
                    call(* java.io.RandomAccessFile.readBytes(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut randomAccessFileWriteMethods() :
            (call(* java.io.RandomAccessFile.write(..)) ||
                    call(* java.io.RandomAccessFile.writeBytes(..)) ||
                    call(* java.io.RandomAccessFile.setLength(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut randomAccessFileExecuteMethods() :
            (call(* java.io.RandomAccessFile.seek(..)) ||
                    call(* java.io.RandomAccessFile.getFilePointer(..)) ||
                    call(* java.io.RandomAccessFile.length(..)) ||
                    call(java.io.RandomAccessFile.new(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemReadMethods() :
            (call(* java.io.UnixFileSystem.canonicalize(..)) ||
                    call(* java.io.UnixFileSystem.checkAccess(..)) ||
                    call(* java.io.UnixFileSystem.getBooleanAttributes(..)) ||
                    call(* java.io.UnixFileSystem.getLastModifiedTime(..)) ||
                    call(* java.io.UnixFileSystem.getLength(..)) ||
                    call(* java.io.UnixFileSystem.getNameMax(..)) ||
                    call(* java.io.UnixFileSystem.getSpace(..)) ||
                    call(* java.io.UnixFileSystem.list(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemWriteMethods() :
            (call(* java.io.UnixFileSystem.createDirectory(..)) ||
                    call(* java.io.UnixFileSystem.createFileExclusively(..)) ||
                    call(* java.io.UnixFileSystem.setLastModifiedTime(..)) ||
                    call(* java.io.UnixFileSystem.setPermission(..)) ||
                    call(* java.io.UnixFileSystem.setReadOnly(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemDeleteMethods() :
            (call(* java.io.UnixFileSystem.delete(..)) ||
                    call(* java.io.UnixFileSystem.rename(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut nativeImageBufferMethods() :
            call(* jdk.internal.jimage.NativeImageBuffer.getNativeMap(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut epollMethods() :
            (call(* sun.nio.ch.EPoll.create(..)) ||
                    call(* sun.nio.ch.EPoll.ctl(..)) ||
                    call(* sun.nio.ch.EPoll.wait(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileChannelImplReadMethods() :
            call(* sun.nio.ch.FileChannelImpl.unmap(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileChannelImplWriteMethods() :
            call(* sun.nio.ch.FileChannelImpl.transferTo(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileDispatcherImplReadMethods() :
            (call(* sun.nio.ch.FileDispatcherImpl.pread(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.read(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.readv(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.size(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileDispatcherImplWriteMethods() :
            (call(* sun.nio.ch.FileDispatcherImpl.force(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.pwrite(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.truncate(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.write(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.writev(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileDispatcherImplExecuteMethods() :
            (call(* sun.nio.ch.FileDispatcherImpl.close(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.closeIntFD(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.dup0(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.lock(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.preClose(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.release(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.seek(..)) ||
                    call(* sun.nio.ch.FileDispatcherImpl.setDirect0(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileKeyMethods() :
            call(* sun.nio.ch.FileKey.init(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut inheritedChannelMethods() :
            call(* sun.nio.ch.InheritedChannel.open(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut linuxNativeDispatcherMethods() :
            (call(* sun.nio.fs.LinuxNativeDispatcher.endmntent(..)) ||
                    call(* sun.nio.fs.LinuxNativeDispatcher.getmntent(..)) ||
                    call(* sun.nio.fs.LinuxNativeDispatcher.setmntent(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixCopyFileMethods() :
            call(* sun.nio.fs.UnixCopyFile.transfer(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut unixNativeDispatcherReadMethods() :
            (call(* sun.nio.fs.UnixNativeDispatcher.access(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.exists(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fgetxattr(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.flistxattr(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fstat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fstatat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.getcwd(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.getlinelen(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.lstat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.read(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.readdir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.readlink(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.realpath(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.stat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.stat1(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.statvfs(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut unixNativeDispatcherWriteMethods() :
            (call(* sun.nio.fs.UnixNativeDispatcher.chmod(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.chown(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fchmod(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fchown(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fremovexattr(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fsetxattr(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.futimens(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.futimes(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.lchown(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.link(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.lutimes(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.mkdir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.mknod(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.rename(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.renameat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.rmdir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.symlink(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.utimes(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.write(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut unixNativeDispatcherExecuteMethods() :
            (call(* sun.nio.fs.UnixNativeDispatcher.close(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.closedir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.dup(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.fdopendir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.open(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.openat(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.opendir(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.rewind(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut unixNativeDispatcherDeleteMethods() :
            (call(* sun.nio.fs.UnixNativeDispatcher.unlink(..)) ||
                    call(* sun.nio.fs.UnixNativeDispatcher.unlinkat(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut documentHandlerParseMethods() : call(* com.sun.beans.decoder.DocumentHandler.parse(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut templatesImplReadObjectMethods() : call(* com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.readObject(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut desktopMoveToTrashMethods() : call(* java.awt.Desktop.moveToTrash(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut desktopPrintMethods() : call(* java.awt.Desktop.print(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut desktopSetPrintFileHandlerMethods() : call(* java.awt.Desktop.setPrintFileHandler(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut inputEventCanAccessSystemClipboardMethods() : call(* java.awt.event.InputEvent.canAccessSystemClipboard(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut introspectorSetBeanInfoSearchPathMethods() : call(* java.beans.Introspector.setBeanInfoSearchPath(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut propertyEditorManagerSetEditorSearchPathMethods() : call(* java.beans.PropertyEditorManager.setEditorSearchPath(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileTempDirectoryGenerateFileMethods() : call(* java.io.File$TempDirectory.generateFile(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

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
                    call(* java.io.File.normalizedList(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileWriteMethods() :
            (call(* java.io.File.canWrite(..)) ||
                    call(* java.io.File.createNewFile(..)) ||
                    call(* java.io.File.createTempFile(..)) ||
                    call(* java.io.File.setExecutable(..)) ||
                    call(* java.io.File.setLastModified(..)) ||
                    call(* java.io.File.setReadOnly(..)) ||
                    call(* java.io.File.setReadable(..)) ||
                    call(* java.io.File.setWritable(..)) ||
                    call(* java.io.File.mkdir(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileExecuteMethods() :
            (call(* java.io.File.canExecute(..)) ||
                    call(* java.io.File.renameTo(..))||
                    call(* java.io.File.toPath(..)) ||
                    call(* java.io.File.toURI(..))||
                    call(* java.io.File.mkdirs(..))||
                    call(* java.io.File.listFiles(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileDeleteMethods() :
            (call(* java.io.File.delete(..)) ||
                    call(* java.io.File.deleteOnExit(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);


    pointcut fileInputStreamInitMethods() : call(java.io.FileInputStream.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileOutputStreamInitMethods() : call(java.io.FileOutputStream.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut objectInputFilterConfigMethods() : (call(* java.io.ObjectInputFilter$Config.setSerialFilter(..)) || call(* java.io.ObjectInputFilter$Config.setSerialFilterFactory(..))) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut objectInputStreamMethods() : (call(java.io.ObjectInputStream.new(..)) || call(* java.io.ObjectInputStream.enableResolveObject(..)) || call(* java.io.ObjectInputStream.setObjectInputFilter(..)) || call(* java.io.ObjectInputStream.verifySubclass(..))) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut objectOutputStreamMethods() : (call(java.io.ObjectOutputStream.new(..)) || call(* java.io.ObjectOutputStream.enableReplaceObject(..)) || call(* java.io.ObjectOutputStream.verifySubclass(..))) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut objectStreamClassMethods() : (call(* java.io.ObjectStreamClass.forClass(..)) || call(* java.io.ObjectStreamClass.getProtectionDomains(..)) || call(* java.io.ObjectStreamField.getType(..))) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut randomAccessFileInitMethods() : call(java.io.RandomAccessFile.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemMethods() : (call(* java.io.UnixFileSystem.listRoots(..)) || call(* java.io.UnixFileSystem.resolve(..))) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileTreeWalkerGetAttributesMethods() : call(* java.nio.file.FileTreeWalker.getAttributes(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut tempFileHelperCreateMethods() : call(* java.nio.file.TempFileHelper.create(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileSystemProviderCheckPermissionMethods() : call(* java.nio.file.spi.FileSystemProvider.checkPermission(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileTypeDetectorCheckPermissionMethods() : call(* java.nio.file.spi.FileTypeDetector.checkPermission(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut zipFileInitMethods() : call(java.util.zip.ZipFile.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut streamPrintServiceFactoryRunMethods() : call(* javax.print.StreamPrintServiceFactory$1.run(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut xPathFactoryFinderCreateClassMethods() : call(* javax.xml.xpath.XPathFactoryFinder.createClass(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut readWriteSelectableChannelMethods() : call(* jdk.nio.Channels.readWriteSelectableChannel(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut dataTransfererCastToFilesMethods() : call(* sun.awt.datatransfer.DataTransferer.castToFiles(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileImageSourceInitMethods() : call(sun.awt.image.FileImageSource.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut imageConsumerQueueInitMethods() : call(sun.awt.image.ImageConsumerQueue.new(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut inputStreamImageSourceAddConsumerMethods() : call(* sun.awt.image.InputStreamImageSource.addConsumer(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut shellFolderManagerCheckFileMethods() : call(* sun.awt.shell.ShellFolderManager.checkFile(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut shellFolderManagerCheckFilesMethods() : call(* sun.awt.shell.ShellFolderManager.checkFiles(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileFontGetPublicFileNameMethods() : call(* sun.font.FileFont.getPublicFileName(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixChannelFactoryOpenMethods() : call(* sun.nio.fs.UnixChannelFactory.open(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileAttributeViewsPosixCheckReadExtendedMethods() : call(* sun.nio.fs.UnixFileAttributeViews$Posix.checkReadExtended(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileAttributeViewsPosixCheckWriteExtendedMethods() : call(* sun.nio.fs.UnixFileAttributeViews$Posix.checkWriteExtended(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystem1IteratorMethods() : call(* sun.nio.fs.UnixFileSystem$1.iterator(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemFileStoreIteratorReadNextMethods() : call(* sun.nio.fs.UnixFileSystem$FileStoreIterator.readNext(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemCopyMethods() : call(* sun.nio.fs.UnixFileSystem.copy(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemGetFileStoresMethods() : call(* sun.nio.fs.UnixFileSystem.getFileStores(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemMoveMethods() : call(* sun.nio.fs.UnixFileSystem.move(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemProviderCheckAccessMethods() : call(* sun.nio.fs.UnixFileSystemProvider.checkAccess(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemProviderCreateLinkMethods() : call(* sun.nio.fs.UnixFileSystemProvider.createLink(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemProviderCreateSymbolicLinkMethods() : call(* sun.nio.fs.UnixFileSystemProvider.createSymbolicLink(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemProviderGetFileStoreMethods() : call(* sun.nio.fs.UnixFileSystemProvider.getFileStore(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixFileSystemProviderReadSymbolicLinkMethods() : call(* sun.nio.fs.UnixFileSystemProvider.readSymbolicLink(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixPathCheckDeleteMethods() : call(* sun.nio.fs.UnixPath.checkDelete(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixPathCheckReadMethods() : call(* sun.nio.fs.UnixPath.checkRead(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixPathCheckWriteMethods() : call(* sun.nio.fs.UnixPath.checkWrite(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixPathToAbsolutePathMethods() : call(* sun.nio.fs.UnixPath.toAbsolutePath(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamBasicFileAttributeViewImplCheckWriteAccessMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream$BasicFileAttributeViewImpl.checkWriteAccess(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamBasicFileAttributeViewImplReadAttributesMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream$BasicFileAttributeViewImpl.readAttributes(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamPosixFileAttributeViewImplCheckWriteAndUserAccessMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream$PosixFileAttributeViewImpl.checkWriteAndUserAccess(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamPosixFileAttributeViewImplReadAttributesMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream$PosixFileAttributeViewImpl.readAttributes(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamImplDeleteMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream.implDelete(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamMoveMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream.move(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixSecureDirectoryStreamNewDirectoryStreamMethods() : call(* sun.nio.fs.UnixSecureDirectoryStream.newDirectoryStream(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixUserDefinedFileAttributeViewDeleteMethods() : call(* sun.nio.fs.UnixUserDefinedFileAttributeView.delete(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixUserDefinedFileAttributeViewListMethods() : call(* sun.nio.fs.UnixUserDefinedFileAttributeView.list(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixUserDefinedFileAttributeViewReadMethods() : call(* sun.nio.fs.UnixUserDefinedFileAttributeView.read(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixUserDefinedFileAttributeViewSizeMethods() : call(* sun.nio.fs.UnixUserDefinedFileAttributeView.size(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut unixUserDefinedFileAttributeViewWriteMethods() : call(* sun.nio.fs.UnixUserDefinedFileAttributeView.write(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut printJob2DThrowPrintToFileMethods() : call(* sun.print.PrintJob2D.throwPrintToFile(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut rasterPrinterJobThrowPrintToFileMethods() : call(* sun.print.RasterPrinterJob.throwPrintToFile(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut serviceDialogPrintServicePanelThrowPrintToFileMethods() : call(* sun.print.ServiceDialog$PrintServicePanel.throwPrintToFile(..)) &&
            !within(de.tum.cit.ase.ares.api..*);

    pointcut filesReadMethods() :
            (call(* java.nio.file.Files.readAllBytes(..)) ||
                    call(* java.nio.file.Files.readAllLines(..)) ||
                    call(* java.nio.file.Files.readString(..)) ||
                    call(* java.nio.file.Files.readAttributes(..)) ||
                    call(* java.nio.file.Files.lines(..)) ||
                    call(* java.nio.file.Files.newBufferedReader(..)) ||
                    call(* java.nio.file.Files.newInputStream(..)) ||
                    call(* java.nio.file.Files.probeContentType(..)) ||
                    call(* java.nio.file.Files.isReadable(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut filesWriteMethods() :
            (call(* java.nio.file.Files.write(..)) ||
                    call(* java.nio.file.Files.writeString(..)) ||
                    call(* java.nio.file.Files.newBufferedWriter(..)) ||
                    call(* java.nio.file.Files.newOutputStream(..)) ||
                    call(* java.nio.file.Files.setAttribute(..)) ||
                    call(* java.nio.file.Files.setLastModifiedTime(..)) ||
                    call(* java.nio.file.Files.setOwner(..)) ||
                    call(* java.nio.file.Files.setPosixFilePermissions(..)) ||
                    call(* java.nio.file.Files.isWritable(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

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
                    call(* java.nio.file.Files.isExecutable(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut filesDeleteMethods() :
            (call(* java.nio.file.Files.delete(..)) ||
                    call(* java.nio.file.Files.deleteIfExists(..)) ||
                    call(* java.nio.file.Files.isDirectory(..)) ||
                    call(* java.nio.file.Files.isRegularFile(..)) ||
                    call(* java.nio.file.Files.isSameFile(..)) ||
                    call(* java.nio.file.Files.isSymbolicLink(..)) ||
                    call(* java.nio.file.Files.notExists(..)) ||
                    call(* java.nio.file.Files.size(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

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
                    call(* java.nio.file.Path.normalize(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut pathWriteMethods() :
            (call(* java.nio.file.Path.register(..)) ||
                    call(* java.nio.file.Path.toFile(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut pathExecuteMethods() :
            (call(* java.nio.file.Path.compareTo(..)) ||
                    call(* java.nio.file.Path.endsWith(..)) ||
                    call(* java.nio.file.Path.startsWith(..)) ||
                    call(* java.nio.file.Path.equals(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut pathDeleteMethods() :
            (call(* java.nio.file.Path.deleteIfExists(..)) ||
                    call(* java.nio.file.Path.delete(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileSystemReadMethods() :
            (call(* java.nio.file.FileSystem.getFileStores(..)) ||
                    call(* java.nio.file.FileSystem.getPath(..)) ||
                    call(* java.nio.file.FileSystem.getPathMatcher(..)) ||
                    call(* java.nio.file.FileSystem.getRootDirectories(..)) ||
                    call(* java.nio.file.FileSystem.provider(..)) ||
                    call(* java.nio.file.FileSystem.supportedFileAttributeViews(..)) ||
                    call(* java.nio.file.FileSystem.isOpen(..)) ||
                    call(* java.nio.file.FileSystem.isReadOnly(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileSystemWriteMethods() :
            (call(* java.nio.file.FileSystem.newWatchService(..)) ||
                    call(* java.nio.file.FileSystem.close(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileSystemExecuteMethods() :
            (call(* java.nio.file.FileSystem.equals(..)) ||
                    call(* java.nio.file.FileSystem.hashCode(..)) ||
                    call(* java.nio.file.FileSystem.toString(..))) &&
                            !within(de.tum.cit.ase.ares.api..*);

    pointcut fileSystemDeleteMethods() :
            call(* java.nio.file.FileSystem.close(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsFileSystemProviderReadSymbolicLinkMethods() :
            call(* sun.nio.fs.WindowsFileSystemProvider.readSymbolicLink(..)) && !within(de.tum.cit.ase.ares.api..*);

    pointcut desktopSetOpenFileHandlerMethods() :
            call(* java.awt.Desktop.setOpenFileHandler(..)) && !within(de.tum.cit.ase.ares.api..*);

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
                    call(* java.awt.Desktop.setOpenURIHandler(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fontCreateMethods() :
            (call(* java.awt.Font.createFont(..)) ||
                    call(* java.awt.Font.createFonts(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsPathCheckDeleteMethods() :
            call(* sun.nio.fs.WindowsPath.checkDelete(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut win32PrintJobGetAttributeValuesMethods() :
            call(* sun.print.Win32PrintJob.getAttributeValues(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut hotSpotDiagnosticDumpHeapMethods() :
            call(* com.sun.management.internal.HotSpotDiagnostic.dumpHeap(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut processBuilderStartMethods() :
            call(* java.lang.ProcessBuilder.start(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut processImplInitMethods() :
            call(java.lang.ProcessImpl.new(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsFileSystemProviderCheckAccessMethods() :
            call(* sun.nio.fs.WindowsFileSystemProvider.checkAccess(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsPathCheckReadMethods() :
            call(* sun.nio.fs.WindowsPath.checkRead(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsPathRegisterMethods() :
            call(* sun.nio.fs.WindowsPath.register(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut windowsFileSystemGetRootDirectoriesMethods() :
            call(* sun.nio.fs.WindowsFileSystem.getRootDirectories(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut jarFileFactoryGetCachedJarFileMethods() :
            call(* sun.net.www.protocol.jar.JarFileFactory.getCachedJarFile(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileServerHandlerInitMethods() :
            call(sun.net.httpserver.simpleserver.FileServerHandler.new(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut win32ShellFolderManager2CheckFileMethods() :
            call(* sun.awt.shell.Win32ShellFolderManager2.checkFile(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut win32ShellFolder2GetFileSystemPathMethods() :
            call(* sun.awt.shell.Win32ShellFolder2.getFileSystemPath(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut win32ShellFolder2ListFilesMethods() :
            call(* sun.awt.shell.Win32ShellFolder2.listFiles(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut winNTFileSystemAccessMethods() :
            call(* java.io.WinNTFileSystem.access(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut winNTFileSystemResolveMethods() :
            call(* java.io.WinNTFileSystem.resolve(..)) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileChannelExecuteMethods() :
            (call(* java.nio.channels.FileChannel.open(..)) ||
                    call(* java.nio.channels.FileChannel.position(..)) ||
                    call(* java.nio.channels.FileChannel.tryLock(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileChannelReadMethods() :
            (call(* java.nio.channels.FileChannel.read(..)) ||
                    call(* java.nio.channels.FileChannel.size(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileChannelWriteMethods() :
            (call(* java.nio.channels.FileChannel.write(..)) ||
                    call(* java.nio.channels.FileChannel.force(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileWriterMethods() :
            (call(java.io.FileWriter.new(..)) ||
                    call(* java.io.FileWriter.append(..)) ||
                    call(* java.io.FileWriter.write(..)) ||
                    call(* java.io.FileWriter.flush(..)) ||
                    call(* java.io.FileWriter.close(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut fileHandlerMethods() :
            (call(java.util.logging.FileHandler.new(..)) ||
                    call(* java.util.logging.FileHandler.close(..)) ||
                    call(* java.util.logging.FileHandler.flush(..)) ||
                    call(* java.util.logging.FileHandler.publish(..)) ||
                    call(* java.util.logging.FileHandler.setLevel(..)) ||
                    call(* java.util.logging.FileHandler.setFormatter(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

    pointcut midiSystemMethods() :
            (call(* javax.sound.midi.MidiSystem.getMidiDevice(..)) ||
                    call(* javax.sound.midi.MidiSystem.getMidiDeviceInfo(..)) ||
                    call(* javax.sound.midi.MidiSystem.getReceiver(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSequencer(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSoundbank(..)) ||
                    call(* javax.sound.midi.MidiSystem.getSynthesizer(..)) ||
                    call(* javax.sound.midi.MidiSystem.getTransmitter(..))) &&
                    !within(de.tum.cit.ase.ares.api..*);

}