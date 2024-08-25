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
        if (JavaAspectConfigurationLists.allowedFileSystemInteractions == null) {
            return true;
        }

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> isOperationAllowed(interaction, operationType, thisJoinPoint) && checkAllowedPaths(interaction, thisJoinPoint));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    // This method checks if the given operation type is allowed for the file system interaction.
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

    // This method checks if the file paths involved in the join point are allowed according to the file system interaction rules.
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

    // This method throws a SecurityException indicating that the join point operation could not proceed.
    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    before(): FileSystemPointcutDefinitions.randomAccessFileExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileDeleteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileInputStreamInitMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileOutputStreamInitMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.objectStreamClassMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileTypeDetectorProbeContentTypeMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.filesReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.filesWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.filesExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }


    before(): FileSystemPointcutDefinitions.pathReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.pathWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.pathExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.pathDeleteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileChannelExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileChannelReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileChannelWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileWriterMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileHandlerMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.midiSystemMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemsReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemsExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.defaultFileSystemExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): FileSystemPointcutDefinitions.desktopExecuteMethods() {
        if (!handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

}