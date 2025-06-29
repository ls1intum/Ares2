package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.file.InvalidPathException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;

import org.aspectj.lang.JoinPoint;
//</editor-fold>

public aspect JavaAspectJThreadSystemAdviceDefinitions {

    //<editor-fold desc="Constants">
    /**
     * List of call stack classes to ignore during thread system checks.
     *
     * <p>Description: Contains class name prefixes that should be skipped when analyzing the call stack
     * to avoid false positives from test harness or localization utilities.
     */
    @Nonnull
    private static final List<String> THREAD_SYSTEM_IGNORE_CALLSTACK = List.of(
            "java.lang.ClassLoader",
            "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension",
            "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension"
    );

    /**
     * Map of methods with attribute index exceptions for thread system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during thread system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> THREAD_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries();

    /**
     * Map of methods with parameter index exceptions for thread system ignore logic.
     *
     * <p>Description: Specifies for certain methods which parameter index should be exempted
     * from ignore rules during thread system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> THREAD_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();
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
            // Take standard class loader as class loader in order to get the JavaAOPTestCaseSettings class at compile time for aspectj
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
            // Take standard class loader as class loader in order to get the JavaAOPTestCaseSettings class at compile time for aspectj
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"), "adviceSettingsClass must not be null");
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

    //<editor-fold desc="Thread system methods">

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
            for (@Nonnull String allowedClass : THREAD_SYSTEM_IGNORE_CALLSTACK) {
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
     * Checks whether the thread limit for the class at the specified index is still available.
     * If the count at that index is greater than zero, this method decrements it and returns
     * <code>false</code> (indicating it was allowed). If the count is zero or below, it returns
     * <code>true</code> (indicating the class is disallowed because the quota is exhausted).
     *
     * @param threadNumberAllowedToBeCreated An array of permissible thread counts, parallel to
     *                                       the class array that determines which classes can create threads.
     * @param index                          The index corresponding to the class being checked.
     * @return <code>true</code> if no more threads can be created (disallowed), or
     *         <code>false</code> if the class is allowed to create another thread and the count was decremented.
     */
    private static boolean handleFoundClassIsForbidden(@Nullable int[] threadNumberAllowedToBeCreated, int index) {
        if (threadNumberAllowedToBeCreated == null) {
            return true;
        }
        boolean threadDisallowed = threadNumberAllowedToBeCreated[index] <= 0;
        if (!threadDisallowed) {
            decrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
        }
        return threadDisallowed;
    }

    /**
     * Checks if a class name is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPaths not null or if the given path does not match one of the allowedPatterns.
     *
     * @since 2.0.0
     * @author Markus
     * @param actualClassname the class name of the thread being requested
     * @param threadClassAllowedToBeCreated the thread classes that are allowed to be created
     * @param threadNumberAllowedToBeCreated the number of threads allowed to be created
     * @return true if path is forbidden; false otherwise
     */
    private static boolean checkIfThreadIsForbidden(@Nullable String actualClassname, @Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated) {
        if (actualClassname == null) {
            return false;
        }
        if (threadClassAllowedToBeCreated == null || threadNumberAllowedToBeCreated == null) {
            return true;
        }
        int starIndex = -1;
        for (int i = 0; i < threadClassAllowedToBeCreated.length; i++) {
            String allowedClassName = threadClassAllowedToBeCreated[i];
            if ("*".equals(allowedClassName)) {
                starIndex = i;
            }

            // Handle special case for Lambda-Expression
            if ("Lambda-Expression".equals(allowedClassName) && "Lambda-Expression".equals(actualClassname)) {
                return handleFoundClassIsForbidden(threadNumberAllowedToBeCreated, i);
            }

            // Skip Class.forName if either is Lambda-Expression (they don't match)
            if ("Lambda-Expression".equals(allowedClassName) || "Lambda-Expression".equals(actualClassname)) {
                continue;
            }

            try {
                Class<?> allowedClass = Class.forName(allowedClassName, true, ClassLoader.getSystemClassLoader());
                Class<?> actualClass = Class.forName(actualClassname, true, ClassLoader.getSystemClassLoader());
                if (allowedClass.isAssignableFrom(actualClass)) {
                    return handleFoundClassIsForbidden(threadNumberAllowedToBeCreated, i);
                }
            } catch (ClassNotFoundException | IllegalStateException | NullPointerException ignored) {
            }
        }
        if (starIndex != -1) {
            return handleFoundClassIsForbidden(threadNumberAllowedToBeCreated, starIndex);
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /**
     * Checks if a variable is a lambda expression.
     *
     * <p>Description: Determines if the provided variable is a lambda expression by checking
     * if its class is synthetic and has a writeReplace() method that returns a SerializedLambda.
     *
     * @param variableClass the class of the variable to check
     * @return true if the variable is a lambda expression, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean isReallyLambda(@Nonnull Class<?> variableClass) {
        // Step 1: check if the class is synthetic
        if (!variableClass.isSynthetic()) {
            return false;
        }
        String className = variableClass.getName();

        // Check for common lambda patterns
        return className.contains("$$Lambda") ||
                className.contains("$Lambda$") ||
                className.matches(".*\\$\\$Lambda\\$.*");
    }

    /**
     * Checks if a variable is an instance of Thread.FieldHolder using reflection.
     *
     * <p>Description: Uses reflection to safely check if the variable is an instance
     * of Thread.FieldHolder, avoiding direct instanceof checks that might fail in
     * instrumentation contexts.
     *
     * @param variableValue the variable to check
     * @return true if the variable is a Thread.FieldHolder instance, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean isThreadFieldHolder(@Nullable Object variableValue) {
        if (variableValue == null) {
            return false;
        }

        try {
            @Nonnull Class<?> variableClass = variableValue.getClass();
            @Nonnull String className = variableClass.getName();

            // Check if the class name matches Thread.FieldHolder pattern
            return className.equals("java.lang.Thread$FieldHolder") ||
                    className.endsWith("$FieldHolder") && className.startsWith("java.lang.Thread");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the task field value from a Thread.FieldHolder instance using reflection.
     *
     * <p>Description: Uses reflection to safely access the task field from a
     * Thread.FieldHolder instance, handling potential access restrictions.
     *
     * @param threadFieldHolder the Thread.FieldHolder instance
     * @return the value of the task field as a String
     * @throws InvalidPathException if extraction fails
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    private static String getTaskFromThreadFieldHolder(@Nonnull Object threadFieldHolder) {
        try {
            @Nonnull Class<?> fieldHolderClass = threadFieldHolder.getClass();
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
            Field taskField = fieldHolderClass.getDeclaredField("task");
            long offset = (Long) objectFieldOffsetMethod.invoke(unsafe, taskField);
            Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
            @Nullable Object taskValue = getObjectMethod.invoke(unsafe, threadFieldHolder, offset);
            if (taskValue == null) {
                throw new InvalidPathException(threadFieldHolder.toString(),
                        localize("security.advice.transform.path.exception"));
            }
            @Nonnull Class<?> taskClass = taskValue.getClass();
            return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException |
                 InaccessibleObjectException e) {
            throw new InvalidPathException(threadFieldHolder.toString(),
                    localize("security.advice.transform.path.exception"));
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /** Converts a variable value to its class name.
     *
     * <p>Description: If the variable is null, throws an InvalidPathException.
     * If the variable is a lambda expression, returns "Lambda-Expression".
     * Otherwise, returns the class name of the variable.
     *
     */
    @Nonnull
    private static String variableToClassname(@Nullable Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        } else if (isThreadFieldHolder(variableValue)) {
            return getTaskFromThreadFieldHolder(variableValue);
        } else if (variableValue instanceof Runnable || variableValue instanceof Callable<?> || variableValue instanceof ForkJoinTask<?> || variableValue instanceof CompletableFuture<?> || variableValue instanceof Supplier<?> || variableValue instanceof Function<?, ?> || variableValue instanceof BiFunction<?, ?, ?> || variableValue instanceof CompletionStage<?>) {
            @Nonnull Class<?> variableClass = variableValue.getClass();
            return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
        } else {
            throw new InvalidPathException(variableValue.toString(), localize("security.advice.transform.path.exception"));
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
     * @param threadClassAllowedToBeCreated whitelist of allowed thread classes
     * @param threadNumberAllowedToBeCreated the number of threads allowed to be created
     * @return true if a violation is found, false otherwise
     */
    private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return false;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                if (analyseViolation(element, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated)) {
                    return true;
                }
            }
            return false;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                if (analyseViolation(element, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated)) {
                    return true;
                }
            }
            return false;
        } else {
            try {
                String observedClassname = variableToClassname(observedVariable);
                return checkIfThreadIsForbidden(observedClassname, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            } catch (InvalidPathException ignored) {
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
     * @param threadClassAllowedToBeCreated the thread classes that are allowed to be created
     * @param threadNumberAllowedToBeCreated the number of threads allowed to be created
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return null;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                String violationPath = extractViolationPath(element, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                String violationPath = extractViolationPath(element, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else {
            try {
                String observedClassname = variableToClassname(observedVariable);
                if (checkIfThreadIsForbidden(observedClassname, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated)) {
                    return observedClassname;
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
     * @param threadClassAllowedToBeCreated whitelist of allowed thread classes
     * @param threadNumberAllowedToBeCreated the number of threads allowed to be created
     * @param ignoreVariables criteria determining which observedVariables to skip
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] observedVariables,
            @Nullable String[] threadClassAllowedToBeCreated,
            @Nullable int[] threadNumberAllowedToBeCreated,
            @Nonnull IgnoreValues ignoreVariables
    ) {
        for (@Nullable Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
            if (analyseViolation(observedVariable, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated)) {
                return extractViolationPath(observedVariable, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            }
        }
        return null;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Validates a thread system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (create)
     * complies with allowed threads and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the thread system action being performed
     * @param thisJoinPoint the join point representing the method call
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void checkThreadSystemInteraction(
            @Nonnull String action,
            @Nonnull JoinPoint thisJoinPoint
    ) {
        //<editor-fold desc="Get information from settings">
        @Nullable final String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("ASPECTJ")) {
            return;
        }
        @Nullable final String restrictedPackage = getValueFromSettings("restrictedPackage");
        @Nullable final String[] allowedClasses = getValueFromSettings("allowedListedClasses");
        @Nullable String[] threadClassAllowedToBeCreated = getValueFromSettings("threadClassAllowedToBeCreated");
        int threadClassAllowedToBeCreatedSize = threadClassAllowedToBeCreated == null ? 0 : threadClassAllowedToBeCreated.length;
        @Nullable int[] threadNumberAllowedToBeCreated = getValueFromSettings("threadNumberAllowedToBeCreated");
        int threadNumberAllowedToBeCreatedSize = threadNumberAllowedToBeCreated == null ? 0 : threadNumberAllowedToBeCreated.length;
        if (threadNumberAllowedToBeCreatedSize != threadClassAllowedToBeCreatedSize) {
            throw new SecurityException(localize("security.advice.thread.allowed.size", threadNumberAllowedToBeCreatedSize, threadClassAllowedToBeCreatedSize));
        }
        //</editor-fold>
        //<editor-fold desc="Get information from attributes">
        @Nonnull Object[] parameters = thisJoinPoint.getArgs();
        @Nonnull final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String threadSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (threadSystemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String threadIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated, THREAD_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(fullMethodSignature, IgnoreValues.NONE));
        if (threadIllegallyInteractedThroughParameter != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", threadSystemMethodToCheck, action, threadIllegallyInteractedThroughParameter, fullMethodSignature));
        }
        //</editor-fold>
    }
    //</editor-fold>

    //</editor-fold>

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJThreadSystemPointcutDefinitions.threadCreateMethodsWithParameters() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJThreadSystemPointcutDefinitions.threadCreateMethodsWithoutParameters() {
        checkThreadSystemInteraction("create", thisJoinPoint);
    }

}