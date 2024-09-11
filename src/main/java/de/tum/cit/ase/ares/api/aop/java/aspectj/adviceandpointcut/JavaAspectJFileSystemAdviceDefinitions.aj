package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public aspect JavaAspectJFileSystemAdviceDefinitions {

    //<editor-fold desc="Tool methods">
    /**
     * Get the value of a field from the JavaSecurityTestCaseSettings class.
     * This method retrieves the value of a field from the JavaSecurityTestCaseSettings class.
     * The field is specified by its name, and the value is returned as an Object.
     *
     * @param fieldName The name of the field to retrieve from the JavaSecurityTestCaseSettings class.
     * @return The value of the specified field.
     * @throws SecurityException If the field cannot be accessed or does not exist.
     */
    private static Object getValueFromSettings(String fieldName) {
        try {
            // Take standard class loader as class loader in order to get the JavaSecurityTestCaseSettings class at compile time for aspectj
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings");
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            field.setAccessible(false);
            return value;
        } catch (LinkageError e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Linkage error while accessing field '" + fieldName + "' in AdviceSettings", e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not find 'JavaSecurityTestCaseSettings' class to access field '" + fieldName + "'", e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' not found in AdviceSettings", e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Null pointer exception while accessing field '" + fieldName + "' in AdviceSettings", e);
        } catch (IllegalAccessException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' is not accessible in AdviceSettings", e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' is inaccessible in AdviceSettings", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Callstack criteria methods">

    /**
     * Check if the call stack violates the specified criteria.
     * This method examines the current call stack to determine if any element belongs to a restricted package.
     * If such an element is found, and it is not in the allowed classes list, the violating call stack element
     * is returned.
     *
     * @param restrictedPackage The package that is restricted in the call stack.
     * @param allowedClasses The list of classes that are allowed to be present in the call stack.
     * @param readingMethod The method that is currently being executed.
     * @return The call stack element that violates the criteria, or null if no violation occurred.
     */
    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses, String readingMethod) {
        if (readingMethod.startsWith(restrictedPackage)) {
            for (String allowedClass : allowedClasses) {
                if (readingMethod.startsWith(allowedClass)) {
                    return null;
                }
            }
            return readingMethod;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Variable criteria methods">

    /**
     * Transform a variable value into a normalized absolute path.
     * This method converts the provided variable (e.g., Path, String, or File) into an absolute
     * normalized path. This path is used to check whether the file system interaction is permitted
     * according to security policies.
     *
     * @param variableValue The variable value to transform into a path. Supported types are Path, String, or File.
     * @return The normalized absolute path of the variable value.
     * @throws InvalidPathException If the variable cannot be transformed into a valid path.
     */
    private static Path variableToPath(Object variableValue) {
        return switch (variableValue) {
            case null -> throw new InvalidPathException("null", "Cannot transform to path");
            case Path path -> {
                try {
                    yield path.normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(path.toString(), "Cannot transform to path");
                }
            }
            case String string -> {
                try {
                    yield Path.of(string).normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(string, "Cannot transform to path");
                }
            }
            case File file -> {
                try {
                    yield Path.of(file.toURI()).normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(file.toString(), "Cannot transform to path");
                }
            }
            default -> throw new InvalidPathException(variableValue.toString(), "Cannot transform to path");
        };
    }

    /**
     * Check if the provided path is allowed according to security policies.
     * This method compares the given path with the list of allowed paths to determine whether the path
     * is permitted for access or modification based on the defined security rules.
     *
     * @param allowedPaths The list of allowed paths that can be accessed or modified.
     * @param pathToCheck The path that needs to be checked against the allowed paths.
     * @return True if the path is allowed, false otherwise.
     */
    private static boolean checkIfPathIsAllowed(String[] allowedPaths, Path pathToCheck) {
        for (String allowedPath : allowedPaths) {
            if (variableToPath(allowedPath).toString().equals(pathToCheck.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the variable criteria is violated.
     *
     * @param variables     The variables to check.
     * @param allowedPaths  The paths that are allowed to be accessed.
     * @return The path that violates the criteria, null if no violation occurred.
     */
    private static String checkIfVariableCriteriaIsViolated(Object[] variables, String[] allowedPaths) {
        for (Object variable : variables) {
            try {
                Path resultingPath = variableToPath(variable);
                if (!checkIfPathIsAllowed(allowedPaths, resultingPath)) {
                    return resultingPath.toString();
                }
            } catch (InvalidPathException ignored) {
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Check if the file system interaction is allowed according to security policies.
     * This method verifies that the specified file system action (read, write, execute, delete) complies
     * with the allowed paths and call stack criteria. If any violation is detected, a SecurityException is thrown.
     * It checks if the action is restricted based on the method call, attributes, and parameters. If a method
     * violates the file system security rules, the action is blocked.
     *
     * @param action The file system action being performed (e.g., read, write, execute, delete).
     * @param thisJoinPoint The current join point of the method being executed.
     * @throws SecurityException If the file system interaction is found to be unauthorized.
     */
    private void checkFileSystemInteraction(
            String action,
            JoinPoint thisJoinPoint
    ) {
        String restrictedPackage = (String) getValueFromSettings("restrictedPackage");
        String[] allowedClasses = (String[]) getValueFromSettings("allowedListedClasses");
        String[] allowedPaths = (String[]) getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "write" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
        );
        Object[] parameters = thisJoinPoint.getArgs();
        // TODO Sarp: delete this statement, this is a workaround since the YAML reader doesn't work properly
        if (restrictedPackage == null) {
            restrictedPackage = "de.tum.cit.ase";
        }
        final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        String illegallyReadingMethod = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, thisJoinPoint.getSourceLocation().getWithinType().getName());
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            if (illegallyReadPath != null) {
                throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution):" + illegallyReadingMethod + " tried to illegally " + action + " from " + illegallyReadPath + " via " + fullMethodSignature + "but was blocked by Ares.");
            }
        }
    }
    //</editor-fold>
    //</editor-fold>

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        checkFileSystemInteraction("write", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        checkFileSystemInteraction("execute", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction("delete", thisJoinPoint);
    }

}