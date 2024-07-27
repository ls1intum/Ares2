package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import org.aspectj.lang.JoinPoint;

import java.nio.file.Path;

public aspect AdviceDefinitionExample {
    /*
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

    // Around advice to capture the path used in Files.write and check permissions
    Object around() : PointcutDefinitions.filesWriteMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

    // Around advice to capture the path used in Files.readAllBytes and check permissions
    Object around() : PointcutDefinitions.filesReadMethod() {
        if (handleAroundAdvice(thisJoinPoint)) {
            return proceed();
        } else {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
        }
    }

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
