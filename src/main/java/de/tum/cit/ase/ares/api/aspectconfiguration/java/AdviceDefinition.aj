package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.JoinPoint;

public aspect AdviceDefinition {

    private boolean handleAroundAdvice(JoinPoint thisJoinPoint, String operationType) {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && isOperationAllowed(interaction, operationType));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    private boolean isOperationAllowed(FileSystemInteraction interaction, String operationType) {
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

    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    Object around() : PointcutDefinitions.unixToolkitLoadGtkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.xDesktopPeerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.xRobotPeerLoadNativeLibrariesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.xTaskbarPeerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.gifImageDecoderParseImageMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.jpegImageDecoderReadImageMethod() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.cupsPrinterInitIDsMethod() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.consoleEchoAndIsttyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileInputStreamReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileInputStreamSkipMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileOutputStreamWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileOutputStreamCloseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileOutputStreamGetChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileDescriptorSyncMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileDescriptorCloseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileCleanableCleanupMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.randomAccessFileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.randomAccessFileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.randomAccessFileSeekLengthMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.randomAccessFileOpenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.nativeImageBufferMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.nativeLibrariesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.cdsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.epollMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileChannelImplReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileChannelImplWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileDispatcherImplReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileDispatcherImplWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileDispatcherImplExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileKeyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.inheritedChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.linuxNativeDispatcherMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixCopyFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherChmodMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherChownMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherCloseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherClosedirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherDupMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherExistsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFchmodMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFchownMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFdopendirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFgetxattrMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFlistxattrMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFremovexattrMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFsetxattrMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFstatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFstatatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFutimensMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherFutimesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherGetcwdMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherGetlinelenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherLchownMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherLstatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherLutimesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherMkdirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherMknodMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherOpenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherOpenatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherOpendirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherReaddirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherReadlinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherRealpathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherRenameMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherRenameatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherRewindMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherRmdirMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherStatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherStat1Methods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherStatvfsMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherSymlinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherUnlinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherUnlinkatMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherUtimesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixNativeDispatcherWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.documentHandlerParseMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.templatesImplReadObjectMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.xPathFactoryImplInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.desktopMoveToTrashMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.desktopPrintMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.desktopSetPrintFileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fontCheckFontFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.inputEventCanAccessSystemClipboardMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.introspectorSetBeanInfoSearchPathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.propertyEditorManagerSetEditorSearchPathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileInputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileOutputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.objectInputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.objectOutputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.randomAccessFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlSetURLStreamHandlerFactoryMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlConnectionSetFileNameMapMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlStreamHandlerProviderCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileTreeWalkerGetAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.tempFileHelperCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileSystemProviderCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileTypeDetectorCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.logStreamSetDefaultStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.driverManagerSetLogStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.driverManagerSetLogWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.zipFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.streamPrintServiceFactoryRunMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.xPathFactoryFinderCreateClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.jrtFileSystemProviderCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.builtinClassLoaderFindClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.builtinClassLoaderFindResourceMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlClassPathJarLoaderCheckJarMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.urlClassPathCheckMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.moduleReferencesExplodedModuleReaderInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.systemModuleFindersSystemModuleReaderCheckPermissionMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.requestPublishersFilePublisherCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.responseBodyHandlersFileDownloadBodyHandlerCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.responseBodyHandlersPathBodyHandlerCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.responseSubscribersPathSubscriberCreateMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.writeableUserPathDoPrivilegedIOMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.readWriteSelectableChannelMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.jlinkInternalMainRunMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.dataTransfererCastToFilesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.imageConsumerQueueInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.inputStreamImageSourceAddConsumerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.shellFolderManagerCheckFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.shellFolderManagerCheckFilesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileFontGetPublicFileNameMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileServerHandlerInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.mimeTableSaveAsPropertiesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.httpURLConnectionCheckURLFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.jarFileFactoryGetCachedJarFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.serverSocketChannelImplFinishAcceptMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixAsynchronousServerSocketChannelImplFinishAcceptMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.abstractUserDefinedFileAttributeViewCheckAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixChannelFactoryOpenMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileAttributeViewsPosixCheckReadExtendedMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileAttributeViewsPosixCheckWriteExtendedMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystem1IteratorMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemFileStoreIteratorReadNextMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemCopyMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemGetFileStoresMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemMoveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderCheckAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderCreateLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderCreateSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderGetFileStoreMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderReadSymbolicLinkMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixPathCheckDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixPathCheckReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixPathCheckWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixPathToAbsolutePathMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamBasicFileAttributeViewImplCheckWriteAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamBasicFileAttributeViewImplReadAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamPosixFileAttributeViewImplCheckWriteAndUserAccessMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamPosixFileAttributeViewImplReadAttributesMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamImplDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamMoveMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamNewDirectoryStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewListMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewSizeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.printJob2DThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.rasterPrinterJobThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.serviceDialogPrintServicePanelThrowPrintToFileMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.registryImplInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.configFileSpiEngineRefreshMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.pathReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.pathWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.pathExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.pathDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

    Object around() : PointcutDefinitions.fileSystemDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            return proceed();
        }
        throwSecurityException(thisJoinPoint);
        return null;
    }

}