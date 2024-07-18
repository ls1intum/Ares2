package de.tum.cit.ase.ares.api.aspect;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.ProceedingJoinPoint;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public aspect MainAspectJava {

    // Around advice to capture the parameters passed to Main.getPath
    Object around() : PointcutDefinitions.getPathMethodCall() {
        Object[] args = thisJoinPoint.getArgs();
        String first = (String) args[0];
        Object[] more = Arrays.copyOfRange(args, 1, args.length, Object[].class);

        System.out.println("Before execution of: " + thisJoinPoint.getSignature() + " with first: " + first + " and more: " + Arrays.toString(more));
        System.out.println("All arguments: ");
        for (Object arg : args) {
            System.out.println(arg);
        }
        return proceed();
    }

    // Around advice to capture the path used in Files.write and check permissions
    Object around() : PointcutDefinitions.filesWriteMethod() {
        Object[] args = thisJoinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        // Check if the path is allowed and has write permission
        boolean isAllowed = FileSystemInteractionList.getAllowedFileSystemInteractions().stream()
                .anyMatch(interaction -> path.startsWith(interaction.onThisPathAndAllPathsBelow())
                        && interaction.studentsAreAllowedToOverwriteAllFiles());

        if (!isAllowed) {
            System.out.println("Files.write called with path: " + path + " - Access Denied");
            throw new SecurityException("Write operation blocked by AspectJ for path: " + path);
        } else {
            System.out.println("Files.write called with path: " + path + " - Access Granted");
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

        // Check if the path is allowed and has read permission
        boolean isAllowed = FileSystemInteractionList.getAllowedFileSystemInteractions().stream()
                .anyMatch(interaction -> path.startsWith(interaction.onThisPathAndAllPathsBelow())
                        && interaction.studentsAreAllowedToReadAllFiles());

        if (!isAllowed) {
            System.out.println("Files.read called with path: " + path + " - Access Denied");
            throw new SecurityException("Read operation blocked by AspectJ for path: " + path);
        } else {
            System.out.println("Files.read called with path: " + path + " - Access Granted");
        }

        return proceed();
    }

    /*
    // Around advice to capture file deletion and check permissions
    Object around() : PointcutDefinitions.filesDeleteMethod() {
        Object[] args = thisJoinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        // Check if the path is allowed and has delete permission
        boolean isAllowed = FileSystemInteractionList.getAllowedFileSystemInteractions().stream()
                .anyMatch(interaction -> path.startsWith(interaction.onThisPathAndAllPathsBelow())
                        && interaction.studentsAreAllowedToDeleteAllFiles());

        if (!isAllowed) {
            System.out.println("Files.delete called with path: " + path + " - Access Denied");
            throw new SecurityException("Delete operation blocked by AspectJ for path: " + path);
        } else {
            System.out.println("Files.delete called with path: " + path + " - Access Granted");
        }

        return proceed(args);
    }

     */
}