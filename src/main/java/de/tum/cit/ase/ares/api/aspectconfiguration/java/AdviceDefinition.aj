package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import org.aspectj.lang.JoinPoint;

public aspect AdviceDefinition {

    private boolean handleAroundAdvice(JoinPoint thisJoinPoint) {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    Object around() : PointcutDefinitions.fileInputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileReaderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileWriterConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.filterOutputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.inputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamGetFieldConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectOutputStreamPutFieldConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectStreamClassConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectStreamFieldConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.outputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.printStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.printWriterConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.randomAccessFileConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.readerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.streamTokenizerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.writerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.bufferConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.byteOrderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.asynchronousChannelGroupConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.asynchronousFileChannelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.asynchronousServerSocketChannelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.asynchronousSocketChannelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.channelsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileChannelMapModeConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileLockConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.membershipKeyConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.pipeConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.selectionKeyConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.selectorConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.abstractInterruptibleChannelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.abstractSelectableChannelConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.asynchronousChannelProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.selectorProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.charsetConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.charsetDecoderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.charsetEncoderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.coderResultConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.codingErrorActionConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.standardCharsetsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.charsetProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileStoreConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileSystemConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileSystemsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.filesConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.pathsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.simpleFileVisitorConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.standardWatchEventKindsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.aclEntryBuilderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.aclEntryConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileTimeConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.posixFilePermissionsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.userPrincipalLookupServiceConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileSystemProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileTypeDetectorConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.datagramSocketAdaptorConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.defaultAsynchronousChannelProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.defaultSelectorProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileChannelImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileKeyConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.ioStatusConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.ioUtilConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.nativeThreadConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.netConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.nioSocketImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.pollerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.streamsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.threadPoolConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.utilConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.resultContainerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.sctpChannelImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.sctpMultiChannelImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.sctpNetConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.sctpServerChannelImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.sctpStdSocketOptionConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.defaultFileSystemProviderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.defaultFileTypeDetectorConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.extendedOptionsInternalOptionConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.extendedOptionsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.globsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixUserPrincipalsConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileMethodCalls() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileDescriptorCloseAllMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.filterInputStreamReadMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.pushbackInputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.filesAsUncheckedRunnableMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.filesWriteWithCharsetMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileChannelImplImplCloseChannelMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.ioUtilReleaseScopesMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigClinit() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamClinit() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileSystemProviderClinit() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.documentHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.templatesImplMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.xPathFactoryImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.desktopMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fontMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.inputEventMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.introspectorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.propertyEditorManagerMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileTempDirectoryMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileInputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileOutputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectOutputStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectInputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectOutputStreamConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.objectStreamFieldMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.randomAccessFileConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.urlConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.urlMethodExecutions() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.urlConnectionMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.urlStreamHandlerProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileTreeWalkerMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.tempFileHelperMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileSystemProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileTypeDetectorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.logStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.driverManagerMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.zipFileConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.streamPrintServiceFactoryMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.xPathFactoryFinderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.jrtFileSystemProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.builtinClassLoaderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.urlClassPathMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.moduleReferencesExplodedModuleReaderConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.systemModuleFindersSystemModuleReaderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.requestPublishersFilePublisherMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.responseBodyHandlersMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.responseSubscribersMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.writeableUserPathMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.channelsMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.mainMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.dataTransfererMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileImageSourceConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.imageConsumerQueueConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.inputStreamImageSourceMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.shellFolderManagerMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileFontMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.fileServerHandlerConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.mimeTableMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.httpURLConnectionMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.jarFileFactoryMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.serverSocketChannelImplMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixAsynchronousServerSocketChannelImplMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.abstractUserDefinedFileAttributeViewMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixChannelFactoryMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileAttributeViewsPosixMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystem1Methods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystemFileStoreIteratorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixPathMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamBasicFileAttributeViewImplMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamPosixFileAttributeViewImplMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixSecureDirectoryStreamMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.unixUserDefinedFileAttributeViewMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.printJob2DMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.rasterPrinterJobMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.serviceDialogPrintServicePanelMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.registryImplConstructorMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    Object around() : PointcutDefinitions.configFileSpiMethods() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

}