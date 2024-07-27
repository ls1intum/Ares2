package de.tum.cit.ase.ares.api.aspectconfiguration.java;

public aspect AdviceDefinition { // TODO: Can we outsource the common functionality in a separate function?

    Object around() : PointcutDefinitions.fileInputStreamConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileReaderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileWriterConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.filterOutputStreamConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.inputStreamConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectInputStreamGetFieldConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectOutputStreamPutFieldConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectStreamClassConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectStreamFieldConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.outputStreamConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.printStreamConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.printWriterConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.randomAccessFileConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.readerConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.streamTokenizerConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.writerConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.bufferConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.byteOrderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.asynchronousChannelGroupConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.asynchronousFileChannelConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.asynchronousServerSocketChannelConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.asynchronousSocketChannelConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.channelsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileChannelMapModeConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileLockConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.membershipKeyConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.pipeConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.selectionKeyConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.selectorConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.abstractInterruptibleChannelConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.abstractSelectableChannelConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.asynchronousChannelProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.selectorProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.charsetConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.charsetDecoderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.charsetEncoderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.coderResultConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.codingErrorActionConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.standardCharsetsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.charsetProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
        }

    Object around() : PointcutDefinitions.fileStoreConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileSystemConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileSystemsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.filesConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.pathsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.simpleFileVisitorConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.standardWatchEventKindsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.aclEntryBuilderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.aclEntryConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileTimeConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blockedby AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.posixFilePermissionsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.userPrincipalLookupServiceConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileSystemProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileTypeDetectorConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.datagramSocketAdaptorConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.defaultAsynchronousChannelProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.defaultSelectorProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileChannelImplConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileKeyConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.ioStatusConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.ioUtilConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.nativeThreadConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.netConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.nioSocketImplConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.pollerConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.streamsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.threadPoolConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.utilConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.resultContainerConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.sctpChannelImplConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.sctpMultiChannelImplConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.sctpNetConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.sctpServerChannelImplConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.sctpStdSocketOptionConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.defaultFileSystemProviderConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.defaultFileTypeDetectorConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.extendedOptionsInternalOptionConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.extendedOptionsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.globsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.unixUserPrincipalsConstructorMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileMethodCalls() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && (interaction.studentsAreAllowedToOverwriteAllFiles() ||
                        interaction.studentsAreAllowedToReadAllFiles() ||
                        interaction.studentsAreAllowedToDeleteAllFiles()));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileDescriptorCloseAllMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.filterInputStreamReadMethod() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToReadAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.pushbackInputStreamMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToReadAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.filesAsUncheckedRunnableMethod() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.filesWriteWithCharsetMethod() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileChannelImplImplCloseChannelMethod() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.ioUtilReleaseScopesMethod() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.unixFileSystemProviderMethods() {
        Object[] args = thisJoinPoint.getArgs();
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && (interaction.studentsAreAllowedToOverwriteAllFiles() ||
                        interaction.studentsAreAllowedToReadAllFiles() ||
                        interaction.studentsAreAllowedToDeleteAllFiles()));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectInputFilterConfigClinit() {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.objectInputStreamClinit() {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    Object around() : PointcutDefinitions.fileSystemProviderClinit() {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

}