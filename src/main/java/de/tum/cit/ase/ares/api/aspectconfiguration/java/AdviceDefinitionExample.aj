package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import java.nio.file.Path;

public aspect AdviceDefinitionExample {

    // Around advice to capture the path used in Files.write and check permissions
    Object around() : PointcutDefinitions.filesWriteMethod() {
        Object[] args = thisJoinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        // Check if the file name is allowed and has write permission
        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called" + " in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    // Around advice to capture the path used in Files.readAllBytes and check permissions
    Object around() : PointcutDefinitions.filesReadMethod() {
        Object[] args = thisJoinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        // Check if the file name is allowed and has read permission
        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToReadAllFiles());

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + "Called" + " in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return proceed();
    }

    /*

    // Around advice to capture file deletion and check permissions
    Object around() : PointcutDefinitions.filesDeleteMethod() {
        Object[] args = thisJoinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        // Check if the file name is allowed and has delete permission
        boolean isAllowed = FileSystemInteractionList.getAllowedFileSystemInteractions().stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && interaction.studentsAreAllowedToDeleteAllFiles());

        if (!isAllowed) {
            System.out.println("Files.delete called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
            throw new SecurityException("Delete operation blocked by AspectJ for path: " + path);
        } else {
            System.out.println("Files.delete called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Granted");
        }

        return proceed();
    }

    // Around advice to capture method executions and check permissions
    Object around() : PointcutDefinitions.executionMethod() {
        // You can add additional checks or log statements here
        System.out.println("Execution of: " + thisJoinPoint.getSignature() + " in " + thisJoinPoint.getSourceLocation());

        return proceed();
    }

     */

}