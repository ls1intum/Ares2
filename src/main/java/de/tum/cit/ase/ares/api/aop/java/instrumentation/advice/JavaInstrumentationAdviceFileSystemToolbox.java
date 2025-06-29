package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Array;
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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    /**
     * List of call stack classes to ignore during file system checks.
     *
     * <p>Description: Contains class name prefixes that should be skipped when analyzing the call stack
     * to avoid false positives from test harness or localization utilities.
     */
    @Nonnull
    private static final List<String> FILE_SYSTEM_IGNORE_CALLSTACK = List.of(
            "java.lang.ClassLoader",
            "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension",
            "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension"
    );

    /**
     * Map of methods with attribute index exceptions for file system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during file system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.io.File.delete", IgnoreValues.allExcept(1)),
            Map.entry("java.io.File.deleteOnExit", IgnoreValues.allExcept(1))
    );

    /**
     * Map of methods with parameter index exceptions for file system ignore logic.
     *
     * <p>Description: Specifies for certain methods which parameter index should be exempted
     * from ignore rules during file system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();
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
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceFileSystemToolbox is a utility class and should not be instantiated."
        );
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Retrieves the value of a specified static field from the settings class.
     *
     * <p>Description: Uses reflection to access a static field in JavaAOPTestCaseSettings,
     * allowing retrieval of security-related configuration values for instrumentation and tests.
     *
     * @param fieldName the name of the field to retrieve
     * @param <T> the type of the field's value
     * @return the value of the specified field
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T getValueFromSettings(@Nonnull String fieldName) {
        try {
            // Take bootloader as class loader in order to get the JavaAOPTestCaseSettings class at bootloader time for instrumentation
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null), "adviceSettingsClass must not be null");
            @Nonnull Field field = Objects.requireNonNull(adviceSettingsClass.getDeclaredField(Objects.requireNonNull(fieldName, "fieldName must not be null")), "field must not be null");
            field.setAccessible(true);
            @Nullable T value = (T) field.get(null);
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
     * Sets the value of a specified static field in the settings class.
     *
     * <p>Description: Uses reflection to modify a static field in JavaAOPTestCaseSettings,
     * allowing updates to security-related configuration values for instrumentation and tests.
     *
     * @param fieldName the name of the field to modify
     * @param newValue the new value to assign to the field
     * @param <T> the type of the field's value
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static <T> void setValueToSettings(@Nonnull String fieldName, @Nullable T newValue) {
        try {
            // Take bootloader as class loader in order to get the JavaAOPTestCaseSettings class at bootloader time for instrumentation
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null), "adviceSettingsClass must not be null");
            @Nonnull Field field = Objects.requireNonNull(adviceSettingsClass.getDeclaredField(Objects.requireNonNull(fieldName, "fieldName must not be null")), "field must not be null");
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
     * Decrements the value at a specified index in an integer array setting.
     *
     * <p>Description: Retrieves an integer array from settings, decrements the value
     * at the given position, and updates the array back to the settings class.
     *
     * @param settingsArray the name of the array field in settings
     * @param position the index position of the value to decrement
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static void decrementSettingsArrayValue(@Nonnull String settingsArray, int position) {
        @Nullable int[] array = getValueFromSettings(settingsArray);
        if (array != null && position >= 0 && position < array.length) {
            @Nonnull int[] clone = array.clone();
            clone[position]--;
            setValueToSettings(settingsArray, clone);
        }
    }

    /**
     * Retrieves a localized message based on a key and optional arguments.
     *
     * <p>Description: Attempts to fetch a localized string from the Messages class using reflection.
     * Falls back to the key if localization fails.
     *
     * @param key the localization key identifying the message
     * @param args optional arguments to format the localized message
     * @return the localized message string, or the key itself if localization fails
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    public static String localize(@Nonnull String key, @Nullable Object... args) {
        try {
            @Nonnull Class<?> messagesClass = Class.forName(
                    "de.tum.cit.ase.ares.api.localization.Messages",
                    true,
                    Thread.currentThread().getContextClassLoader()
            );
            @Nonnull Method localized = messagesClass.getDeclaredMethod(
                    "localized", String.class, Object[].class
            );
            @Nullable Object result = localized.invoke(null, key, args);
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
     * Determines if a call stack element is in the allow list.
     *
     * <p>Description: Checks whether the class name of the provided stack trace element
     * starts with any of the allowed class name prefixes.
     *
     * @param allowedClasses the array of allowed class name prefixes
     * @param elementToCheck the stack trace element to check
     * @return true if the element is allowed, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean checkIfCallstackElementIsAllowed(@Nonnull String[] allowedClasses, @Nonnull StackTraceElement elementToCheck) {
        String className = elementToCheck.getClassName();
        for (@Nonnull String allowedClass : allowedClasses) {
            if (className.startsWith(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the current call stack for violations of restricted packages.
     *
     * <p>Description: Examines the stack trace to find the first element whose class name
     * starts with the restricted package but is not in the allowed classes list,
     * skipping any classes in the ignore list.
     *
     * @param restrictedPackage the prefix of restricted package names
     * @param allowedClasses the array of allowed class name prefixes
     * @return the fully qualified method name that violates criteria, or null if none
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nullable
    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (@Nonnull StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            boolean ignoreFound = false;
            for (@Nonnull String allowedClass : FILE_SYSTEM_IGNORE_CALLSTACK) {
                if (className.startsWith(allowedClass)) {
                    ignoreFound = true;
                    break;
                }
            }
            if (ignoreFound) {
                break;
            }
            if (className.startsWith(restrictedPackage) && !checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                return className + "." + element.getMethodName();
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Filter variables">

    /**
     * Filters variables based on the IgnoreValues criteria.
     *
     * <p>Description: Returns a new array of variables, excluding those that match the ignore criteria.
     * If all variables are ignored, returns an empty array. If all except one variable is ignored,
     * returns an array with only that variable.
     *
     * @param variables the original array of variables
     * @param ignoreVariables criteria determining which variables to skip
     * @return a filtered array of variables
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    private static Object[] filterVariables(@Nonnull Object[] variables, @Nonnull IgnoreValues ignoreVariables) {
        @Nonnull ArrayList<Object> newVariables = new ArrayList<>(Arrays.asList(variables.clone()));
        switch (ignoreVariables.getType()) {
            // No variable is ignored
            case NONE:
                break;
            // All variables are ignored
            case ALL:
                newVariables.clear();
                break;
            // All variables except the one at the given index are ignored
            case ALL_EXCEPT:
                @Nonnull Object toKeep = newVariables.get(ignoreVariables.getIndex());
                newVariables.clear();
                newVariables.add(toKeep);
                break;
            case NONE_EXCEPT:
                newVariables.remove(ignoreVariables.getIndex());
                break;
        }
        return newVariables.toArray();
    }
    //</editor-fold>

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks if a Path is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPaths not null or if the given path does not match one of the allowedPatterns.
     *
     * @since 2.0.0
     * @author Markus
     * @param path         the Path to test
     * @param allowedPaths whitelist of allowed path strings
     * @return true if path is forbidden; false otherwise
     */
    private static boolean checkIfPathIsForbidden(@Nullable Path path, @Nullable String[] allowedPaths) {
        if(path == null) {
            return false;
        }
        if(allowedPaths == null) {
            return true;
        }
        try {
            @Nonnull Path realPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            for (@Nonnull String allowed : allowedPaths) {
                try {
                    Path allowedPath = variableToPath(allowed);
                    if (allowedPath != null && realPath.startsWith(allowedPath)) {
                        return false;
                    }
                } catch (InvalidPathException ignored) {
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /**
     * Transforms variable values into a normalized absolute path.
     *
     * <p>Description: Converts the provided variable (Path, String, or File)
     * into an absolute normalized Path for security checks, validating existence.
     *
     * @param variableValue the variable to transform into a Path
     * @return the normalized absolute Path
     * @throws InvalidPathException if transformation fails
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nullable
    private static Path variableToPath(@Nullable Object variableValue) {
        try {
            if (variableValue == null) {
                return null;
            } else if (variableValue instanceof Path) {
                return ((Path) variableValue).normalize().toAbsolutePath();
            } else if (variableValue instanceof String) {
                Path absolutePath = Path.of((String) variableValue).normalize().toAbsolutePath();
                if (Files.exists(absolutePath)) {
                    return absolutePath;
                } else {
                    return null;
                }
            } else if (variableValue instanceof File) {
                return Path.of(((File) variableValue).toURI()).normalize().toAbsolutePath();
            } else {
                return null;
            }
        } catch (InvalidPathException e) {
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Violation analysis">
    /**
     * Analyzes a variable to determine if it violates allowed paths.
     *
     * <p>Description: Recursively checks if the variable or its elements (if an array or List)
     * are in violation of the allowed paths. Returns true if any element is forbidden.
     *
     * @since 2.0.0
     * @author Markus
     * @param observedVariable      the variable to analyze
     * @param allowedPaths  whitelist of allowed path strings; if null, all paths are considered allowed
     * @return true if a violation is found, false otherwise
     */
    private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedPaths) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return false;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                if (analyseViolation(element, allowedPaths)) {
                    return true;
                }
            }
            return false;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                if (analyseViolation(element, allowedPaths)) {
                    return true;
                }
            }
            return false;
        } else {
            try {
                Path observedPath = variableToPath(observedVariable);
                return checkIfPathIsForbidden(observedPath, allowedPaths);
            }
            catch (InvalidPathException ignored) {
                return false;
            }
        }
    }

    /**
     * Extracts and returns the first violating path string from an array or list variable.
     *
     * <p>Description: Iterates through the variable’s elements (array or List), converts each to a Path if possible,
     * and returns the string of the first path that does not satisfy the allowedPaths whitelist.
     *
     * @since 2.0.0
     * @author Markus
     * @param observedVariable     the array or List to inspect
     * @param allowedPaths whitelist of allowed path strings
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedPaths) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return null;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                String violationPath = extractViolationPath(element, allowedPaths);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                String violationPath = extractViolationPath(element, allowedPaths);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else {
            try {
                Path observedPath = variableToPath(observedVariable);
                if (checkIfPathIsForbidden(observedPath, allowedPaths) && observedPath != null) {
                    return observedPath.toString();
                }
            } catch (InvalidPathException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Checks an array of observedVariables against allowed file system paths.
     *
     * <p>Description: Iterates through the filtered observedVariables (excluding those matching ignoreVariables). For each
     * non-null variable, if it is an array or a List (excluding byte[]/Byte[]), each element is converted to a Path
     * and tested against allowedPaths. Otherwise, the variable itself is converted to a Path and tested. The first
     * violating path found is returned.
     *
     * @since 2.0.0
     * @author Markus
     * @param observedVariables      array of values to validate
     * @param allowedPaths   whitelist of allowed path strings; if null, all paths are considered allowed
     * @param ignoreVariables criteria determining which observedVariables to skip
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] observedVariables,
            @Nullable String[] allowedPaths,
            @Nonnull IgnoreValues ignoreVariables
    ) {
        for (@Nullable Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
            if (analyseViolation(observedVariable, allowedPaths)) {
                return extractViolationPath(observedVariable, allowedPaths);
            }
        }
        return null;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Validates a file system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (read, overwrite, execute, delete)
     * complies with allowed paths and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the file system action being performed
     * @param declaringTypeName the fully qualified class name of the caller
     * @param methodName the name of the method invoked
     * @param methodSignature the method signature descriptor
     * @param attributes optional method attributes
     * @param parameters optional method parameters
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static void checkFileSystemInteraction(
            @Nonnull String action,
            @Nonnull String declaringTypeName,
            @Nonnull String methodName,
            @Nonnull String methodSignature,
            @Nullable Object[] attributes,
            @Nullable Object[] parameters
    ) {
        //<editor-fold desc="Get information from settings">
        @Nullable final String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        @Nullable final String restrictedPackage = getValueFromSettings("restrictedPackage");
        @Nullable final String[] allowedClasses = getValueFromSettings("allowedListedClasses");

        @Nullable final String[] allowedPaths = getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "overwrite" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new SecurityException("Unknown action: " + action);
                }
        );
        //</editor-fold>
        //<editor-fold desc="Get information from attributes">
        @Nonnull final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String fileSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (fileSystemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String pathIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
        if (pathIllegallyInteractedThroughParameter != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", fileSystemMethodToCheck, action, pathIllegallyInteractedThroughParameter, fullMethodSignature));
        }
        //</editor-fold>
        //<editor-fold desc="Check attributes">
        @Nullable String pathIllegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
        if (pathIllegallyInteractedThroughAttribute != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", fileSystemMethodToCheck, action, pathIllegallyInteractedThroughAttribute, fullMethodSignature));
        }
        //</editor-fold>
    }
    //</editor-fold>

    //</editor-fold>
}