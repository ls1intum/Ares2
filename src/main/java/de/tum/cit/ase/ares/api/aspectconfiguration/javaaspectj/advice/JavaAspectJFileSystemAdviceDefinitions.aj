package de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.advice;

import de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.JavaAspectJConfigurationSettings;
import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public aspect JavaAspectJFileSystemAdviceDefinitions {

    // This method handles the security check for file system interactions by validating if the requested operation type is allowed for the file in context.
    private boolean handleAroundAdvice(JoinPoint thisJoinPoint, String operationType) {
        if (JavaAspectJConfigurationSettings.getAllowedFileSystemInteractions() == null) {
            return false;
        }

        boolean isNotAllowed = JavaAspectJConfigurationSettings.getAllowedFileSystemInteractions().stream()
                .anyMatch(interaction -> !isOperationAllowed(interaction, operationType, thisJoinPoint) || !checkAllowedPaths(interaction, thisJoinPoint));

        if (isNotAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return false;
    }

    // This method checks if the given operation type is allowed for the file system interaction.
    private boolean isOperationAllowed(FileSystemInteraction interaction, String operationType, JoinPoint thisJoinPoint) {
        return switch (operationType.toLowerCase()) {
            case "read" -> interaction.studentsAreAllowedToReadAllFiles();
            case "write" -> interaction.studentsAreAllowedToOverwriteAllFiles();
            case "execute" -> interaction.studentsAreAllowedToExecuteAllFiles();
            case "delete" -> interaction.studentsAreAllowedToDeleteAllFiles();
            default -> throw new IllegalArgumentException("Invalid operation type: " + operationType);
        };
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

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileTypeDetectorProbeContentTypeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }


    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.pathReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.pathWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.defaultFileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.pointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

}