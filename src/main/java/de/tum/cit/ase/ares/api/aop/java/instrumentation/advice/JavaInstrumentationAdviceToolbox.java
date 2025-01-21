package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.InvalidPathException;

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
    // Skip the OutputTester and InputTester classes, as they intercept the output and input for System.out and System.in
    // Therefore, they cause false positives.
    // Also, X11FontManager needs to be set when using AWT therefore we have to allow it
    private static final List<String> fileSystemIgnoreCallstack = List.of("de.tum.cit.ase.ares.api.io.OutputTester", "de.tum.cit.ase.ares.api.io.InputTester", "sun.awt.X11FontManager", "de.tum.cit.ase.ares.api.localization.Messages");
    private static final List<String> fileSystemIgnoreAttributes = List.of("java.io.File.delete");
    private static final List<String> fileSystemIgnoreParameter = List.of();

    //<editor-fold desc="Constructor">

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    protected JavaInstrumentationAdviceToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceToolbox is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Retrieves the value of a specified static field from the {@code JavaSecurityTestCaseSettings} class.
     * <p>
     * This method uses reflection to dynamically access a field in {@code JavaSecurityTestCaseSettings},
     * allowing retrieval of security-related configuration values used for instrumentation and aspectJ tests.
     * </p>
     *
     * @param fieldName The name of the field to retrieve.
     * @param <T>       The expected type of the field's value.
     * @return The value of the specified field.
     * @throws SecurityException If the field cannot be accessed due to various issues such as:
     * <ul>
     *   <li>Class not found</li>
     *   <li>No such field</li>
     *   <li>Illegal access</li>
     *   <li>Linkage errors</li>
     *   <li>Null pointer exceptions</li>
     *   <li>Inaccessible object exceptions</li>
     *   <li>Illegal argument exceptions</li>
     * </ul>
     */
    protected static <T> T getValueFromSettings(String fieldName) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloader time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
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
     * Sets the value of a specified static field in the {@code JavaSecurityTestCaseSettings} class.
     * <p>
     * This method uses reflection to dynamically modify a field in {@code JavaSecurityTestCaseSettings},
     * allowing updates to security-related configuration values used for instrumentation and AspectJ tests.
     * </p>
     *
     * @param fieldName The name of the field to modify.
     * @param newValue  The new value to assign to the field.
     * @param <T>       The expected type of the field's value.
     * @throws SecurityException If the field cannot be accessed or modified due to:
     * <ul>
     *   <li>Class not found</li>
     *   <li>No such field</li>
     *   <li>Illegal access</li>
     *   <li>Linkage errors</li>
     *   <li>Null pointer exceptions</li>
     *   <li>Inaccessible object exceptions</li>
     *   <li>Illegal argument exceptions</li>
     * </ul>
     */
    protected static <T> void setValueToSettings(String fieldName, T newValue) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloader time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
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
     * Decrements the value at a specified position in a settings array stored in the {@code JavaSecurityTestCaseSettings} class.
     * <p>
     * This method retrieves an integer array from {@code JavaSecurityTestCaseSettings}, decrements the value at the given
     * index, and updates the array back to the settings class.
     * </p>
     *
     * @param settingsArray The name of the array field in {@code JavaSecurityTestCaseSettings}.
     * @param position      The index position of the value to decrement.
     * @throws SecurityException If retrieving or modifying the array fails due to:
     * <ul>
     *   <li>Class not found</li>
     *   <li>No such field</li>
     *   <li>Illegal access</li>
     *   <li>Linkage errors</li>
     *   <li>Null pointer exceptions</li>
     *   <li>Inaccessible object exceptions</li>
     *   <li>Illegal argument exceptions</li>
     * </ul>
     * @throws ArrayIndexOutOfBoundsException If the provided position is out of bounds.
     */
    protected static void decrementSettingsArrayValue(String settingsArray, int position) {
        int[] newSettingsArray = ((int[]) getValueFromSettings(settingsArray)).clone();
        newSettingsArray[position]--;
        setValueToSettings(settingsArray, newSettingsArray);
    }

    /**
     * Retrieves a localized message based on a given key and optional arguments.
     * <p>
     * This method attempts to fetch a localized string from the {@code Messages} class using reflection.
     * If localization fails due to missing classes or methods, it returns the provided key as a fallback.
     * </p>
     *
     * @param key  The localization key identifying the message.
     * @param args Optional arguments to format the localized message.
     * @return The localized message string, or the key itself if localization fails.
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
     * Check if the provided call stack element is allowed.
     * This method verifies whether the class in the call stack element belongs to the list of allowed
     * classes, ensuring that only authorized classes are permitted to perform certain file system operations.
     *
     * @param allowedClasses The list of classes allowed to be present in the call stack.
     * @param elementToCheck The call stack element to check.
     * @return True if the call stack element is allowed, false otherwise.
     */
    protected static boolean checkIfCallstackElementIsAllowed(String[] allowedClasses, StackTraceElement elementToCheck) {
        String className = elementToCheck.getClassName();
        for (String allowedClass : allowedClasses) {
            if (className.startsWith(allowedClass)) {
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
    protected static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.startsWith(restrictedPackage)) {
                if (fileSystemIgnoreCallstack.contains(className)) {
                    return null;
                }
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return className;
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
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        } else if (variableValue instanceof Path path) {
            try {
                return path.normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(path.toString(), localize("security.advice.transform.path.exception"));
            }
        } else if (variableValue instanceof String string) {
            try {
                if (Files.exists(Path.of(string).normalize().toAbsolutePath())) {
                    return Path.of(string).normalize().toAbsolutePath();
                } else {
                    throw new InvalidPathException(string, localize("security.advice.transform.path.exception"));
                }
            } catch (InvalidPathException e) {
                throw new InvalidPathException(string, localize("security.advice.transform.path.exception"));
            }
        } else if (variableValue instanceof File file) {
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
        String illegallyReadingMethod = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = null;
            if (!fileSystemIgnoreParameter.contains(fullMethodSignature + "." + methodName)) {
                illegallyReadPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            }
            if (illegallyReadPath == null && !fileSystemIgnoreAttributes.contains(fullMethodSignature + "." + methodName)) {
                illegallyReadPath = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths);
            }
            if (illegallyReadPath != null) {
                throw new SecurityException(localize("security.advice.illegal.method.execution", illegallyReadingMethod, action, illegallyReadPath, fullMethodSignature));
            }
        }
    }
    //</editor-fold>
    //</editor-fold>
}