package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">
import java.io.File;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
//</editor-fold>

/**
 * Utility class for Java instrumentation file system security advice.
 *
 * <p>Description: Provides static methods to enforce file system security policies at runtime
 * by checking file system interactions (read, write, execute, delete) against allowed paths,
 * call stack criteria, and variable criteria. Uses reflection to interact with test case settings
 * and localization utilities. Designed to prevent unauthorized file system operations during
 * Java application execution, especially in test and instrumentation scenarios.
 *
 * <p>Design Rationale: Centralizes file system security checks for Java instrumentation advice,
 * ensuring consistent enforcement of security policies. Uses static utility methods and a private
 * constructor to prevent instantiation. Reflection is used to decouple the toolbox from direct
 * dependencies on settings and localization classes, supporting flexible and dynamic test setups.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaInstrumentationAdviceFileSystemToolbox {

    //<editor-fold desc="Constants">
    // Skip the OutputTester and InputTester classes, as they intercept the output and input for System.out and System.in
    // Therefore, they cause false positives.
    // Also, X11FontManager needs to be set when using AWT therefore we have to allow it
    /**
     * List of call stack classes to ignore during file system checks.
     *
     * <p>Description: Contains class signatures that are excluded from file system security checks
     * for file operations to avoid false positives.
     */
    private static final List<String> FILE_SYSTEM_IGNORE_CALLSTACK = List.of(
            "java.lang.ClassLoader",
            "sun.awt.X11FontManager",
            "de.tum.cit.ase.ares.api.io.OutputTester",
            "de.tum.cit.ase.ares.api.io.InputTester",
            "de.tum.cit.ase.ares.api.localization.Messages"
    );
    private static final Map<String, Integer> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.io.File.delete", 1)
    );
    private static final Map<String, Integer> FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * <p>Description: Throws a SecurityException if instantiation is attempted, enforcing the
     * utility class pattern.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private JavaInstrumentationAdviceFileSystemToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceFileSystemToolbox is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Retrieves the value of a specified static field from the JavaSecurityTestCaseSettings class.
     *
     * <p>Description: Uses reflection to access a static field in JavaSecurityTestCaseSettings,
     * allowing retrieval of security-related configuration values for instrumentation and tests.
     *
     * @param fieldName The name of the field to retrieve.
     * @param <T> The expected type of the field's value.
     * @return The value of the specified field.
     * @throws SecurityException If the field cannot be accessed due to various issues.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @SuppressWarnings("unchecked")
    private static <T> T getValueFromSettings(String fieldName) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloader time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            T value = (T) field.get(null);
            field.setAccessible(false);
            return value;

        } catch (LinkageError e) {
            throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
        } catch (NullPointerException e) {
            throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
        }
    }

    /**
     * Sets the value of a specified static field in the JavaSecurityTestCaseSettings class.
     *
     * <p>Description: Uses reflection to modify a static field in JavaSecurityTestCaseSettings,
     * allowing updates to security-related configuration values for instrumentation and tests.
     *
     * @param fieldName The name of the field to modify.
     * @param newValue The new value to assign to the field.
     * @param <T> The expected type of the field's value.
     * @throws SecurityException If the field cannot be accessed or modified.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static <T> void setValueToSettings(String fieldName, T newValue) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloader time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, newValue);
            field.setAccessible(false);
        } catch (LinkageError e) {
            throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
        } catch (NullPointerException e) {
            throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
        }
    }

    /**
     * Decrements the value at a specified position in a settings array.
     *
     * <p>Description: Retrieves an integer array from JavaSecurityTestCaseSettings, decrements the value
     * at the given index, and updates the array back to the settings class.
     *
     * @param settingsArray The name of the array field in JavaSecurityTestCaseSettings.
     * @param position The index position of the value to decrement.
     * @throws SecurityException If retrieving or modifying the array fails.
     * @throws ArrayIndexOutOfBoundsException If the provided position is out of bounds.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static void decrementSettingsArrayValue(String settingsArray, int position) {
        int[] newSettingsArray = ((int[]) getValueFromSettings(settingsArray)).clone();
        newSettingsArray[position]--;
        setValueToSettings(settingsArray, newSettingsArray);
    }

    /**
     * Retrieves a localized message based on a given key and optional arguments.
     *
     * <p>Description: Attempts to fetch a localized string from the Messages class using reflection.
     * If localization fails, returns the provided key as a fallback.
     *
     * @param key The localization key identifying the message.
     * @param args Optional arguments to format the localized message.
     * @return The localized message string, or the key itself if localization fails.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static String localize(String key, Object... args) {
        try {
            Class<?> messagesClass = Class.forName("de.tum.cit.ase.ares.api.localization.Messages", true, Thread.currentThread().getContextClassLoader());
            Method localized = messagesClass.getDeclaredMethod("localized", String.class, Object[].class);
            Object result = localized.invoke(null, key, args);
            if (result instanceof String str) {
                return str;
            } else {
                throw new IllegalStateException("Method does not return a String");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            // Fallback: Return the key if localization fails
            return key;
        }
    }
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Callstack criteria methods">

    /**
     * Checks if the provided call stack element is allowed.
     *
     * <p>Description: Verifies whether the class in the call stack element belongs to the list of allowed
     * classes, ensuring only authorized classes are permitted to perform certain file system operations.
     *
     * @param allowedClasses The list of classes allowed to be present in the call stack.
     * @param elementToCheck The call stack element to check.
     * @return True if the call stack element is allowed, false otherwise.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean checkIfCallstackElementIsAllowed(String[] allowedClasses, StackTraceElement elementToCheck) {
        String className = elementToCheck.getClassName();
        for (String allowedClass : allowedClasses) {
            if (className.startsWith(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the call stack violates the specified criteria.
     *
     * <p>Description: Examines the current call stack to determine if any element belongs to a restricted
     * package and is not in the allowed classes list. Returns the violating call stack element if found.
     *
     * @param restrictedPackage The package that is restricted in the call stack.
     * @param allowedClasses The list of classes that are allowed to be present in the call stack.
     * @return The call stack element that violates the criteria, or null if no violation occurred.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            boolean ignoreFound = false;
            for(String allowedClass : FILE_SYSTEM_IGNORE_CALLSTACK) {
                if (className.startsWith(allowedClass)) {
                    ignoreFound = true;
                    break;
                }
            }
            if(ignoreFound) {
                break;
            }
            if (className.startsWith(restrictedPackage)) {
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return className + "." +element.getMethodName();
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Variable criteria methods">

    /**
     * Transforms a variable value into a normalized absolute path.
     *
     * <p>Description: Converts the provided variable (Path, String, or File) into an absolute
     * normalized path for file system security checks.
     *
     * @param variableValue The variable value to transform into a path.
     * @return The normalized absolute path of the variable value.
     * @throws InvalidPathException If the variable cannot be transformed into a valid path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static Path variableToPath(Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        } else if (variableValue instanceof Path) {
            Path path = (Path) variableValue;
            try {
                return path.normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(path.toString(), localize("security.advice.transform.path.exception"));
            }
        } else if (variableValue instanceof String) {
            String string = (String) variableValue;
            try {
                Path absolutePath = Path.of(string).normalize().toAbsolutePath();
                if (Files.exists(absolutePath)) {
                    return absolutePath;
                } else {
                    throw new InvalidPathException(string, localize("security.advice.transform.path.exception"));
                }
            } catch (InvalidPathException e) {
                throw new InvalidPathException(string, localize("security.advice.transform.path.exception"));
            }
        } else if (variableValue instanceof File) {
            File file = (File) variableValue;
            try {
                return Path.of(file.toURI()).normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(file.toString(), localize("security.advice.transform.path.exception"));
            }
        } else {
            throw new InvalidPathException(variableValue.toString(), localize("security.advice.transform.path.exception"));
        }
    }

    /**
     * Checks if the provided path is allowed according to security policies.
     *
     * <p>Description: Compares the given path with the list of allowed paths to determine whether
     * the path is permitted for access or modification.
     *
     * @param allowedPaths The list of allowed paths that can be accessed or modified.
     * @param pathToCheck The path that needs to be checked against the allowed paths.
     * @return True if the path is allowed, false otherwise.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean checkIfPathIsAllowed(String[] allowedPaths, Path pathToCheck) {
        Path absoluteNormalisedPathToCheck;
        try {
            absoluteNormalisedPathToCheck = pathToCheck.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            return false;
        }
        for (String allowedPath : allowedPaths) {
            if (absoluteNormalisedPathToCheck.startsWith(variableToPath(allowedPath))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the variable criteria is violated.
     *
     * <p>Description: Iterates over the provided variables, transforms them to paths, and checks
     * if any path is not allowed according to the security policies.
     *
     * @param variables The variables to check.
     * @param allowedPaths The paths that are allowed to be accessed.
     * @return The path that violates the criteria, null if no violation occurred.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static String checkIfVariableCriteriaIsViolated(Object[] variables, String[] allowedPaths, int ignoreVariablesExcept) {
        ArrayList<Object> newVariables = new ArrayList<>(Arrays.asList(variables.clone()));
        switch (ignoreVariablesExcept) {
            // No variable is ignored
            case -1:
                break;
            // All variables are ignored
            case -2:
                newVariables.clear();
                break;
            // All variables except the one at the given index are ignored
            default:
                Object toKeep = newVariables.get(ignoreVariablesExcept);
                newVariables.clear();
                newVariables.add(toKeep);
                break;
        }
        for (Object variable : newVariables) {
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
     * Checks if the file system interaction is allowed according to security policies.
     *
     * <p>Description: Verifies that the specified file system action (read, write, execute, delete)
     * complies with the allowed paths and call stack criteria. Throws a SecurityException if any
     * violation is detected.
     *
     * @param action The file system action being performed (e.g., read, write, execute, delete).
     * @param declaringTypeName The name of the class declaring the method.
     * @param methodName The name of the method being invoked.
     * @param methodSignature The signature of the method.
     * @param attributes The attributes of the method (if any).
     * @param parameters The parameters of the method (if any).
     * @throws SecurityException If the file system interaction is found to be unauthorized.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static void checkFileSystemInteraction(
            String action,
            String declaringTypeName,
            String methodName,
            String methodSignature,
            Object[] attributes,
            Object[] parameters
    ) {
        String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        String restrictedPackage = getValueFromSettings("restrictedPackage");
        String[] allowedClasses = getValueFromSettings("allowedListedClasses");
        String[] allowedPaths = getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "overwrite" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
        );
        final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        String fileSystemMethodToCheck = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (fileSystemMethodToCheck != null) {
            String illegallyInteractedPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, -1));
            if (illegallyInteractedPath == null) {
                illegallyInteractedPath = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, -1));
            }
            if (illegallyInteractedPath != null) {
                throw new SecurityException(localize("security.advice.illegal.method.execution", fileSystemMethodToCheck, action, illegallyInteractedPath, fullMethodSignature));
            }
        }
    }
    //</editor-fold>
    //</editor-fold>
}