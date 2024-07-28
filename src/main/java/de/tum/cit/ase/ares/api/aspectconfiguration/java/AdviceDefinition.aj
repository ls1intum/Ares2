package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.aspectj.lang.JoinPoint;

public aspect AdviceDefinition {

    private boolean handleAroundAdvice(JoinPoint thisJoinPoint, String operationType) {
        String fileName = thisJoinPoint.getSourceLocation().getFileName();

        boolean isAllowed = JavaAspectConfigurationLists.allowedFileSystemInteractions.stream()
                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)
                        && isOperationAllowed(interaction, operationType));

        if (!isAllowed) {
            throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " operation blocked by AspectJ." + " Called in " + thisJoinPoint.getSourceLocation() + " - Access Denied");
        }

        return true;
    }

    private boolean isOperationAllowed(FileSystemInteraction interaction, String operationType) {
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

    private void throwSecurityException(JoinPoint thisJoinPoint) {
        throw new SecurityException(thisJoinPoint.getSignature().toLongString() + " was not able to proceed.");
    }

    // Advice for System.getSecurityManager()
    before(): execution(public static java.lang.SecurityManager System.getSecurityManager()) {
        System.out.println("Intercepted call to System.getSecurityManager()");
        // You can add additional logic here, such as logging or throwing an exception
    }

    // Advice for file read operations
    before(): call(* java.nio.file.Files.read*(..)) || call(* java.io.FileInputStream.read*(..) || call(* java.io.Files.readString(..)) {
        System.out.println("Intercepted file read operation");
        throw new SecurityException("File read operation blocked by AspectJ");
        // Add logic to handle the read operation, such as blocking or logging
    }

}