package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Utility class for the Java instrumentation advice.
 * <p>
 * This class provides methods to interact with the JavaSecurityTestCaseSettings class and to check
 * file system interactions against established security policies. The goal is to ensure that no
 * unauthorized file system operations (e.g., read, write, execute, delete) are allowed during execution.
 * <p>
 * The class includes methods to access fields from JavaSecurityTestCaseSettings, verify call stack and
 * variable criteria, and determine whether certain file system operations are permitted. This helps
 * enforce security policies at runtime and block unauthorized file interactions.
 */
public class JavaInstrumentationAdviceToolbox {
    //<editor-fold desc="Constructor">

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JavaInstrumentationAdviceToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceToolbox is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Get the value of a field from the JavaSecurityTestCaseSettings class.
     * This method dynamically accesses a field in the JavaSecurityTestCaseSettings class
     * to retrieve security-related configuration values needed for file system interaction checks.
     *
     * @param fieldName The name of the field to retrieve the value from.
     * @return The value of the field.
     * @throws SecurityException If the field cannot be accessed due to linkage errors, class not found,
     *                           or illegal access issues.
     */
    private static Object getValueFromSettings(String fieldName) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloader time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
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
     * Check if the provided call stack element is allowed.
     * This method verifies whether the class in the call stack element belongs to the list of allowed
     * classes, ensuring that only authorized classes are permitted to perform certain file system operations.
     *
     * @param allowedClasses The list of classes allowed to be present in the call stack.
     * @param elementToCheck The call stack element to check.
     * @return True if the call stack element is allowed, false otherwise.
     */
    private static boolean checkIfCallstackElementIsAllowed(String[] allowedClasses, StackTraceElement elementToCheck) {
        for (String allowedClass : allowedClasses) {
            if (elementToCheck.getClassName().startsWith(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the call stack violates the specified criteria.
     * This method examines the current call stack to determine if any element belongs to a restricted package.
     * If such an element is found, and it is not in the allowed classes list, the violating call stack element
     * is returned.
     *
     * @param restrictedPackage The package that is restricted in the call stack.
     * @param allowedClasses    The list of classes that are allowed to be present in the call stack.
     * @return The call stack element that violates the criteria, or null if no violation occurred.
     */
    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.toString().startsWith(restrictedPackage)) {
                // TODO: Check if the method is allowed to access the file system, this happened in the Demo with the OutputTester currently not working
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return element.toString();
                }
            }
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
     * @param pathToCheck  The path that needs to be checked against the allowed paths.
     * @return True if the path is allowed, false otherwise.
     */
    private static boolean checkIfPathIsAllowed(String[] allowedPaths, Path pathToCheck) {
        Path absoluteNormalisedPathToCheck = pathToCheck.toAbsolutePath().normalize();
        for (String allowedPath : allowedPaths) {
            if (absoluteNormalisedPathToCheck.startsWith(variableToPath(allowedPath))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the variable criteria is violated.
     *
     * @param variables    The variables to check.
     * @param allowedPaths The paths that are allowed to be accessed.
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
     * @param action            The file system action being performed (e.g., read, write, execute, delete).
     * @param declaringTypeName The name of the class declaring the method.
     * @param methodName        The name of the method being invoked.
     * @param methodSignature   The signature of the method.
     * @param attributes        The attributes of the method (if any).
     * @param parameters        The parameters of the method (if any).
     * @throws SecurityException If the file system interaction is found to be unauthorized.
     */
    public static void checkFileSystemInteraction(
            String action,
            String declaringTypeName,
            String methodName,
            String methodSignature,
            Object[] attributes,
            Object[] parameters
    ) {
        String aopMode = (String) getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        String restrictedPackage = (String) getValueFromSettings("restrictedPackage");
        String[] allowedClasses = (String[]) getValueFromSettings("allowedListedClasses");
        String[] allowedPaths = (String[]) getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "overwrite" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
        );
        final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        String illegallyReadingMethod = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            if (illegallyReadPath == null) {
                illegallyReadPath = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths);
            }
            if (illegallyReadPath != null) {
                throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution):" + illegallyReadingMethod + " tried to illegally " + action + " from " + illegallyReadPath + " via " + fullMethodSignature + "but was blocked by Ares.");
            }
        }
    }
    //</editor-fold>
    //</editor-fold>
}