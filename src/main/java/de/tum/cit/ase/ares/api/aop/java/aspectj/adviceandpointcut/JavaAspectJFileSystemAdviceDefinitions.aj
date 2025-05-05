package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

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

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;

import org.aspectj.lang.JoinPoint;
//</editor-fold>

public aspect JavaAspectJFileSystemAdviceDefinitions {

    //<editor-fold desc="Constants">
    // Skip the OutputTester and InputTester classes, as they intercept the output and input for System.out and System.in
    // Therefore, they cause false positives.
    // Also, X11FontManager needs to be set when using AWT therefore we have to allow it
    /**
     * List of call stack classes to ignore during file system checks.
     *
     * <p>Description: Contains class name prefixes that should be skipped when analyzing the call stack
     * to avoid false positives from test harness or localization utilities.
     */
    @Nonnull
    private static final List<String> FILE_SYSTEM_IGNORE_CALLSTACK = List.of(
            "java.lang.ClassLoader",
            "sun.awt.X11FontManager",
            "de.tum.cit.ase.ares.api.io.OutputTester",
            "de.tum.cit.ase.ares.api.io.InputTester",
            "de.tum.cit.ase.ares.api.localization.Messages"
    );

    /**
     * Map of methods with attribute index exceptions for file system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during file system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.io.File.delete", IgnoreValues.allExcept(1))
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
            // Take standard class loader as class loader in order to get the JavaSecurityTestCaseSettings class at compile time for aspectj
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"), "adviceSettingsClass must not be null");
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
            // Take standard class loader as class loader in order to get the JavaSecurityTestCaseSettings class at compile time for aspectj
            @Nonnull Class<?> adviceSettingsClass =  Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"), "adviceSettingsClass must not be null");
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
    @Nonnull
    private static Path variableToPath(@Nullable Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        } else if (variableValue instanceof Path) {
            @Nonnull Path path = (Path) variableValue;
            try {
                return path.normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new InvalidPathException(path.toString(), localize("security.advice.transform.path.exception"));
            }
        } else if (variableValue instanceof String) {
            @Nonnull String string = (String) variableValue;
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
            @Nonnull File file = (File) variableValue;
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
     * Determines if a given path is allowed based on configured prefixes.
     *
     * <p>Description: Resolves the real path of the target and checks
     * whether it starts with any allowed path prefix.
     *
     * @param allowedPaths the array of allowed path prefixes
     * @param pathToCheck the Path to verify against allowed prefixes
     * @return true if access is allowed, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean checkIfPathIsAllowed(@Nonnull String[] allowedPaths, @Nonnull Path pathToCheck) {
        try {
            @Nonnull Path real = pathToCheck.toRealPath(LinkOption.NOFOLLOW_LINKS);
            for (@Nonnull String allowed : allowedPaths) {
                if (real.startsWith(variableToPath(allowed))) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static Object[] filterVariables(@Nonnull Object[] variables, IgnoreValues ignoreVariables) {
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

    /**
     * Checks an array of variables against allowed file system paths.
     *
     * <p>Description: Iterates through the filtered variables (excluding those matching ignoreVariables). For each
     * non-null variable, if it is an array or a List (excluding byte[]/Byte[]), each element is converted to a Path
     * and tested against allowedPaths. Otherwise, the variable itself is converted to a Path and tested. The first
     * violating path found is returned.
     *
     * @since 2.0.0
     * @author Markus
     * @param variables      array of values to validate
     * @param allowedPaths   whitelist of allowed path strings; if null, all paths are considered allowed
     * @param ignoreVariables criteria determining which variables to skip
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] variables,
            @Nullable String[] allowedPaths,
            IgnoreValues ignoreVariables
    ) {
        for (@Nullable Object variable : filterVariables(variables, ignoreVariables)) {
            if (variable == null || isByteArray(variable)) {
                continue;
            }

            if (variable.getClass().isArray()) {
                if (handleArrayVariable(variable, allowedPaths)) {
                    return extractViolationPath(variable, allowedPaths);
                }
            } else if (variable instanceof List<?>) {
                if (handleListVariable((List<?>) variable, allowedPaths)) {
                    return extractViolationPath(variable, allowedPaths);
                }
            } else {
                Path path = safeConvertToPath(variable);
                if (path != null && isForbidden(path, allowedPaths)) {
                    return path.toString();
                }
            }
        }
        return null;
    }

    /**
     * Determines if an object is a byte array.
     *
     * <p>Description: Returns true if the object is either a primitive byte[] or an array of Byte.
     *
     * @since 2.0.0
     * @author Markus
     * @param obj the object to test
     * @return true if obj is byte[] or Byte[], false otherwise
     */
    private static boolean isByteArray(Object obj) {
        return obj instanceof byte[] || obj instanceof Byte[];
    }

    /**
     * Processes an array variable, checking each element for path violations.
     *
     * <p>Description: Reflectively iterates through the given array, converts each element to a Path if possible,
     * and tests against allowedPaths. Stops at the first violation.
     *
     * @since 2.0.0
     * @author Markus
     * @param array        the array to inspect
     * @param allowedPaths whitelist of allowed paths
     * @return true if any element violates the allowedPaths; false otherwise
     */
    private static boolean handleArrayVariable(Object array, @Nullable String[] allowedPaths) {
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            Path path = safeConvertToPath(element);
            if (path != null && isForbidden(path, allowedPaths)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Processes a List variable, checking each element for path violations.
     *
     * <p>Description: Iterates through the list, converts each element to a Path if possible, and tests against
     * allowedPaths. Stops at the first violation encountered.
     *
     * @since 2.0.0
     * @author Markus
     * @param list         the list to inspect
     * @param allowedPaths whitelist of allowed paths
     * @return true if any element violates the allowedPaths; false otherwise
     */
    private static boolean handleListVariable(List<?> list, @Nullable String[] allowedPaths) {
        for (Object element : list) {
            Path path = safeConvertToPath(element);
            if (path != null && isForbidden(path, allowedPaths)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Safely converts an object to a Path, ignoring invalid inputs.
     *
     * <p>Description: Attempts to convert the given object to a Path via variableToPath. If the conversion
     * fails with InvalidPathException, returns null.
     *
     * @since 2.0.0
     * @author Markus
     * @param obj the object to convert
     * @return the resulting Path, or null if conversion fails
     */
    @Nullable
    private static Path safeConvertToPath(@Nullable Object obj) {
        try {
            return variableToPath(obj);
        } catch (InvalidPathException ex) {
            return null;
        }
    }

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
    private static boolean isForbidden(@Nonnull Path path, @Nullable String[] allowedPaths) {
        return allowedPaths != null && !checkIfPathIsAllowed(allowedPaths, path);
    }

    /**
     * Extracts and returns the first violating path string from an array or list variable.
     *
     * <p>Description: Iterates through the variable’s elements (array or List), converts each to a Path if possible,
     * and returns the string of the first path that does not satisfy the allowedPaths whitelist.
     *
     * @since 2.0.0
     * @author Markus
     * @param variable     the array or List to inspect
     * @param allowedPaths whitelist of allowed path strings
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(Object variable, @Nullable String[] allowedPaths) {
        if (variable.getClass().isArray()) {
            int length = Array.getLength(variable);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(variable, i);
                Path path = safeConvertToPath(element);
                if (path != null && isForbidden(path, allowedPaths)) {
                    return path.toString();
                }
            }
        } else if (variable instanceof List<?>) {
            for (Object element : (List<?>) variable) {
                Path path = safeConvertToPath(element);
                if (path != null && isForbidden(path, allowedPaths)) {
                    return path.toString();
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Validates a file system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (read, overwrite, execute,
     * delete) complies with allowed paths and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the file system action being performed
     * @param thisJoinPoint the join point of the method being executed
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private void checkFileSystemInteraction(
            String action,
            JoinPoint thisJoinPoint
    ) {
        //<editor-fold desc="Get information from settings">
        @Nullable final String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("ASPECTJ")) {
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
        @Nonnull Object[] parameters = thisJoinPoint.getArgs();
        @Nonnull final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String fileSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (fileSystemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String pathIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(fullMethodSignature, IgnoreValues.NONE));
        if (pathIllegallyInteractedThroughParameter != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", fileSystemMethodToCheck, action, pathIllegallyInteractedThroughParameter, fullMethodSignature));
        }
        //</editor-fold>
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
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.bufferedReaderInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.scannerInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReaderInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.printWriterInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() {
        checkFileSystemInteraction("overwrite", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        checkFileSystemInteraction("execute", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction("delete", thisJoinPoint);
    }

}