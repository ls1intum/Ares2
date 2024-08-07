package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public aspect FileSystemAdviceDefinition {

    // This method handles the security check for file system interactions by validating if the requested operation type is allowed for the file in context.
    private boolean handleAroundAdvice(JoinPoint thisJoinPoint, String operationType) {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> isOperationAllowed(interaction, operationType, thisJoinPoint) && checkAllowedPaths(interaction, thisJoinPoint));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    private boolean isOperationAllowed(FileSystemInteraction interaction, String operationType, JoinPoint thisJoinPoint) {
        switch (operationType.toLowerCase()) {
            case "read":
                return interaction.studentsAreAllowedToReadAllFiles();
            case "write":
                return interaction.studentsAreAllowedToOverwriteAllFiles();
            case "execute":
                return interaction.studentsAreAllowedToExecuteAllFiles();
            case "delete":
                return interaction.studentsAreAllowedToDeleteAllFiles();
            default:
                throw new IllegalArgumentException("Invalid operation type: " + operationType);
        }
    }

    private static boolean checkAllowedPaths(FileSystemInteraction interaction, JoinPoint thisJoinPoint) {
        return Arrays
                .stream(thisJoinPoint.getArgs())
                .map(arg -> {
                    switch (arg) {
                        case Path p -> {
                            return p;
                        }
                        case File p -> {
                            return Paths.get(p.getPath());
                        }
                        case String p -> {
                            try {
                                return Paths.get(p);
                            } catch (InvalidPathException e) {
                                return null;
                            }
                        }
                        default -> {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .allMatch(path -> {
                    Path argumentPath = path.toAbsolutePath().normalize();
                    Path interactionPath = interaction.onThisPathAndAllPathsBelow().toAbsolutePath().normalize();
                    return argumentPath.startsWith(interactionPath);
                });
    }

    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    Object around(): FileSystemPointcutDefinitions.unixToolkitLoadGtkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.xDesktopPeerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.xRobotPeerLoadNativeLibrariesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.xTaskbarPeerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.gifImageDecoderParseImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.jpegImageDecoderReadImageMethod() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.cupsPrinterInitIDsMethod() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileInputStreamCloseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileOutputStreamCloseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileOutputStreamGetChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileDescriptorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileCleanableCleanupMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.randomAccessFileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.randomAccessFileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.randomAccessFileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.nativeImageBufferMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.epollMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileChannelImplReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileChannelImplWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileDispatcherImplReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileDispatcherImplWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileDispatcherImplExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileKeyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.inheritedChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.linuxNativeDispatcherMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixCopyFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixNativeDispatcherReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixNativeDispatcherWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixNativeDispatcherExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixNativeDispatcherDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.documentHandlerParseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.templatesImplReadObjectMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.desktopMoveToTrashMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.desktopPrintMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.desktopSetPrintFileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.inputEventCanAccessSystemClipboardMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.introspectorSetBeanInfoSearchPathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.propertyEditorManagerSetEditorSearchPathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileTempDirectoryGenerateFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileInputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileOutputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.objectInputFilterConfigMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.objectInputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.objectOutputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileTreeWalkerGetAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.tempFileHelperCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemProviderCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileTypeDetectorCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.zipFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.streamPrintServiceFactoryRunMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.xPathFactoryFinderCreateClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.readWriteSelectableChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.dataTransfererCastToFilesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.imageConsumerQueueInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.inputStreamImageSourceAddConsumerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.shellFolderManagerCheckFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.shellFolderManagerCheckFilesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileFontGetPublicFileNameMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixChannelFactoryOpenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileAttributeViewsPosixCheckReadExtendedMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileAttributeViewsPosixCheckWriteExtendedMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystem1IteratorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemFileStoreIteratorReadNextMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemCopyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemGetFileStoresMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemMoveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemProviderCheckAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemProviderCreateLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemProviderCreateSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemProviderGetFileStoreMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixFileSystemProviderReadSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixPathCheckDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixPathCheckReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixPathCheckWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixPathToAbsolutePathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamBasicFileAttributeViewImplCheckWriteAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamBasicFileAttributeViewImplReadAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamPosixFileAttributeViewImplCheckWriteAndUserAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamPosixFileAttributeViewImplReadAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamImplDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamMoveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixSecureDirectoryStreamNewDirectoryStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixUserDefinedFileAttributeViewDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixUserDefinedFileAttributeViewListMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixUserDefinedFileAttributeViewReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixUserDefinedFileAttributeViewSizeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.unixUserDefinedFileAttributeViewWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.printJob2DThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.rasterPrinterJobThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.serviceDialogPrintServicePanelThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.filesReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.filesWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.filesExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.filesDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }


    Object around(): FileSystemPointcutDefinitions.pathReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.pathWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.pathExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.pathDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsFileSystemProviderReadSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.desktopSetOpenFileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.desktopExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fontCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsPathCheckDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.win32PrintJobGetAttributeValuesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.hotSpotDiagnosticDumpHeapMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.processBuilderStartMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.processImplInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsFileSystemProviderCheckAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsPathCheckReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsPathRegisterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.windowsFileSystemGetRootDirectoriesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.jarFileFactoryGetCachedJarFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileServerHandlerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.win32ShellFolderManager2CheckFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.win32ShellFolder2GetFileSystemPathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.win32ShellFolder2ListFilesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.shellFolderManagerCheckFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.zipFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.winNTFileSystemAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.winNTFileSystemResolveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileChannelExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.midiSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemsReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemsExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.defaultFileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around(): FileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

}