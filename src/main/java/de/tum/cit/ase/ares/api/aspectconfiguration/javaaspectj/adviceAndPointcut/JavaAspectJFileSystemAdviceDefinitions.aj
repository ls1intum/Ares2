package de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut;

import de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.JavaAspectJConfigurationSettings;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
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
    private boolean isOperationAllowed(FilePermission interaction, String operationType, JoinPoint thisJoinPoint) {
        return switch (operationType.toLowerCase()) {
            case "read" -> interaction.readAllFiles();
            case "write" -> interaction.overwriteAllFiles();
            case "execute" -> interaction.executeAllFiles();
            case "delete" -> interaction.deleteAllFiles();
            default -> throw new IllegalArgumentException("Invalid operation type: " + operationType);
        };
    }

    // This method checks if the file paths involved in the join point are allowed according to the file system interaction rules.
    private static boolean checkAllowedPaths(FilePermission interaction, JoinPoint thisJoinPoint) {
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
                    Path interactionPath = Path.of(interaction.onThisPathAndAllPathsBelow()).toAbsolutePath().normalize();
                    return argumentPath.startsWith(interactionPath);
                });
    }

    // This method throws a SecurityException indicating that the join point operation could not proceed.
    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileTypeDetectorProbeContentTypeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }


    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.pathReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.pathWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.defaultFileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.adviceAndPointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

}