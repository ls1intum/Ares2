package %s.ares.api.aop.java.aspectj.adviceandpointcut;

import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public aspect JavaAspectJFileSystemAdviceDefinitions {

    //<editor-fold desc="Tool methods">

    /**
     * Get the value of a field from the JavaTestCaseSettings class.
     * This method retrieves the value of a field from the JavaTestCaseSettings class.
     * The field is specified by its name, and the value is returned as an Object.
     *
     * @param fieldName The name of the field to retrieve from the JavaTestCaseSettings class.
     * @return The value of the specified field.
     * @throws SecurityException If the field cannot be accessed or does not exist.
     */
    private static Object getValueFromSettings(String fieldName) {
        try {
            // Take standard class loader as class loader in order to get the JavaTestCaseSettings class at compile time for aspectj
            Class<?> adviceSettingsClass = Class.forName("%s.ares.api.aop.java.JavaTestCaseSettings");
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            field.setAccessible(false);
            return value;
        } catch (LinkageError e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Linkage error while accessing field '" + fieldName + "' in AdviceSettings", e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not find 'JavaTestCaseSettings' class to access field '" + fieldName + "'", e);
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
        if (variableValue == null) {
            throw new InvalidPathException("null", "Cannot transform to path");
        } else if (variableValue instanceof Path) {
            Path path = (Path) variableValue;
            try {
                return path.normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(path.toString(), "Cannot transform to path");
            }
        } else if (variableValue instanceof String) {
            String string = (String) variableValue;
            try {
                return Path.of(string).normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(string, "Cannot transform to path");
            }
        } else if (variableValue instanceof File) {
            File file = (File) variableValue;
            try {
                return Path.of(file.toURI()).normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(file.toString(), "Cannot transform to path");
            }
        } else {
            throw new InvalidPathException(variableValue.toString(), "Cannot transform to path");
        }
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
        Path absoluteNormalisedPathToCheck = pathToCheck.toAbsolutePath().normalize();
        for (String allowedPath : allowedPaths) {
            if (absoluteNormalisedPathToCheck.startsWith( variableToPath(allowedPath))) {
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
        String aopMode = (String) getValueFromSettings("aopMode");
        if(aopMode == null || !aopMode.equals("ASPECTJ")) {
            return;
        }
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
            %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.bufferedReaderInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.scannerInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReaderInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.lineNumberReaderInitMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
            %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.printWriterInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.bufferedWriterInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.outputStreamWriterInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.dataOutputStreamInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectOutputStreamInitMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.printStreamInitMethods() {
        checkFileSystemInteraction("write", thisJoinPoint);
    }

    before():
            %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        checkFileSystemInteraction("execute", thisJoinPoint);
    }

    before():
            %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    %s.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction("delete", thisJoinPoint);
    }

}