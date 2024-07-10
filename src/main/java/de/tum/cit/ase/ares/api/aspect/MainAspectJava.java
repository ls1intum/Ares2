package de.tum.cit.ase.ares.api.aspect;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Aspect
public class MainAspectJava {

    private static List<FileSystemInteraction> allowedFileSystemInteractions = Collections.emptyList();

    @Pointcut("execution(* de.tum.cit.ase.ares.api.aspectJ.main..*.*(..))")
    public void method() {}

    @Before("method()")
    public void beforeMethod() {
        System.out.println("Before execution of a method");
    }

    // Pointcut for Main.getPath method
    @Pointcut(value = "execution(java.nio.file.Path de.tum.cit.ase.ares.api.aspectJ.main.Main.getPath(..)) && args(first, more)")
    public void getPathMethod(String first, String... more) {}

    // Pointcut for Files.write method
    @Pointcut("call(* java.nio.file.Files.write(..))")
    public void filesWriteMethod() {}

    // Before advice to capture the parameters passed to Main.getPath
    @Around(value = "getPathMethod(first, more)", argNames = "joinPoint,first,more")
    public Object aroundGetPath(ProceedingJoinPoint joinPoint, String first, String... more) throws Throwable {
        System.out.println("Before execution of: " + joinPoint.getSignature() + " with first: " + first + " and more: " + Arrays.toString(more));
        return joinPoint.proceed();
    }

    // Around advice to capture the path used in Files.write and check permissions
    @Around(value = "filesWriteMethod()", argNames = "joinPoint")
    public Object aroundFilesWrite(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Path path = (Path) args[0];  // Assuming the first argument is the Path

        // Check if the path is allowed and has write permission
        boolean isAllowed = allowedFileSystemInteractions.stream()
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

        return joinPoint.proceed();
    }

    // Method to set the security policy
    public static synchronized void setSecurityPolicy(List<FileSystemInteraction> iAllowTheFollowingFileSystemInteractionsForTheStudents, Class<?> caller) {
        // Check the caller class to ensure only the allowed class can set the policy
        if (!caller.getName().equals("de.tum.cit.ase.ares.api.internal.TestGuardUtils")) {
            throw new SecurityException("Unauthorized access to set security policy");
        }
        allowedFileSystemInteractions = iAllowTheFollowingFileSystemInteractionsForTheStudents;
    }
}