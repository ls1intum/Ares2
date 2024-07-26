package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public aspect AdviceDefinition {

    Object around() : PointcutDefinitions.colorEditorMethods() {
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

    Object around() : PointcutDefinitions.fontEditorMethods() {
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

}