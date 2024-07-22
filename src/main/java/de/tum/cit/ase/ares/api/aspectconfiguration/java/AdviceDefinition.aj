package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public aspect AdviceDefinition {

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
            throw new SecurityException("Write operation blocked by AspectJ for path: " + path + "." + "Files.write called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        } else {
            System.out.println("Files.write called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Granted");
        }

        if (args.length > 1 && args[1] instanceof List) {
            List<String> lines = (List<String>) args[1];  // Assuming the second argument is the list of lines to write
            System.out.println("Files.write called with path: " + path + " and lines: " + lines);
        } else if (args.length > 1 && args[1] instanceof byte[]) {
            byte[] bytes = (byte[]) args[1];  // Assuming the second argument is the byte array to write
            System.out.println("Files.write called with path: " + path + " and bytes: " + Arrays.toString(bytes));
        } else {
            System.out.println("Files.write called with path: " + path + " and other arguments: " + Arrays.toString(args));
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
            throw new SecurityException("Read operation blocked by AspectJ for path: " + path + "." + "Files.read called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        } else {
            System.out.println("Files.read called with path: " + path + " in " + thisJoinPoint.getSourceLocation() + " - Access Granted");
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