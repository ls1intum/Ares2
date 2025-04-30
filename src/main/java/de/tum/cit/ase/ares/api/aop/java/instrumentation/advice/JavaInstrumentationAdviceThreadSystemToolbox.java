package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.Serializable;

import java.nio.file.InvalidPathException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

public class JavaInstrumentationAdviceThreadSystemToolbox {

    //<editor-fold desc="Constants">
    /**
     * List of call stack classes to ignore during thread system checks.
     *
     * <p>Description: Contains class signatures that are excluded from thread system security checks
     * for thread operations to avoid false positives.
     */
    private final static List<String> THREAD_SYSTEM_IGNORE_CALLSTACK = List.of();
    /**
     * List of method attributes to ignore during thread system checks.
     *
     * <p>Description: Contains method signatures whose attributes are excluded from thread system security checks
     * for thread operations to avoid false positives.
     */
    private final static List<String> THREAD_SYSTEM_IGNORE_ATTRIBUTES = List.of();
    /**
     * List of method parameters to ignore during thread system checks.
     *
     * <p>Description: Contains method signatures whose parameters are excluded from thread system security checks
     * for thread operations to avoid false positives.
     */
    private final static List<String> THREAD_SYSTEM_IGNORE_PARAMETERS = List.of();
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
    private JavaInstrumentationAdviceThreadSystemToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceThreadSystemToolbox is a utility class and should not be instantiated.");
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

    //<editor-fold desc="Thread system methods">

    //<editor-fold desc="Callstack criteria methods">

    /**
     * Checks if the provided call stack element is allowed.
     *
     * <p>Description: Verifies whether the class in the call stack element belongs to the list of allowed
     * classes, ensuring only authorized classes are permitted to perform certain thread system operations.
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
            for (String allowedClass : THREAD_SYSTEM_IGNORE_CALLSTACK) {
                if (className.startsWith(allowedClass)) {
                    ignoreFound = true;
                    break;
                }
            }
            if (ignoreFound) {
                break;
            }
            if (className.startsWith(restrictedPackage)) {
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return className + "." + element.getMethodName();
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Variable criteria methods">

    private static boolean isReallyLambda(Class<?> variableClass) {
        if (!variableClass.isSynthetic()) {
            return false;
        }
        try {
            // Step 2: locate the hidden writeReplace() method
            Method writeReplace = variableClass.getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            // Step 3: ensure it returns a SerializedLambda
            Class<?> returnType = writeReplace.getReturnType();
            return SerializedLambda.class.isAssignableFrom(returnType);
        } catch (NoSuchMethodException e) {
            // If writeReplace is missing, this synthetic class is not a lambda
            return false;
        }
    }

    private static String variableToClassname(Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        }
        if (!(variableValue instanceof Runnable || variableValue instanceof Callable<?> || variableValue instanceof ForkJoinTask<?>)) {
            throw new InvalidPathException(variableValue.toString(), localize("security.advice.transform.path.exception"));
        }
        Class<?> variableClass = variableValue.getClass();
        return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
    }

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
    private static boolean handleFoundClassIsAllowed(int[] threadNumberAllowedToBeCreated, int index) {
        boolean threadDisallowed = threadNumberAllowedToBeCreated[index] <= 0;
        if (!threadDisallowed) {
            decrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
        }
        return threadDisallowed;
    }

    /**
     * Determines if the specified <code>resultingClassname</code> is permitted to create a thread.
     * This method searches for an exact match of <code>resultingClassname</code> in the
     * <code>threadClassAllowedToBeCreated</code> array. If found, it checks whether there is
     * a remaining quota at that index via {@link #handleFoundClassIsAllowed(int[], int)}.
     * <p>
     * If no exact match is found, but a wildcard (<code>"*"</code>) entry is present in
     * <code>threadClassAllowedToBeCreated</code>, the method treats it as a fallback match
     * and checks its corresponding quota.
     * <p>
     * If a quota is available, it is decremented, and this method returns <code>true</code>.
     * Otherwise, it returns <code>false</code>.
     *
     * @param threadClassAllowedToBeCreated  An array of allowed class names or a wildcard string (<code>"*"</code>).
     * @param threadNumberAllowedToBeCreated An array specifying how many threads each corresponding class may create.
     * @param resultingClassname             The class name of the thread being requested.
     * @return <code>true</code> if the specified class name (or the wildcard) still has quota left and was decremented,
     *         or <code>false</code> if the class is not allowed or its quota is exhausted.
     */
    private static boolean checkIfClassIsAllowed(String[] threadClassAllowedToBeCreated, int[] threadNumberAllowedToBeCreated, String resultingClassname) {
        // TODO Markus: Remove star operator
        int starIndex = -1;
        for (int i = 0; i < threadClassAllowedToBeCreated.length; i++) {
            String allowedClass = threadClassAllowedToBeCreated[i];
            if ("*".equals(allowedClass)) {
                starIndex = i;
            } else if (resultingClassname.equals(allowedClass)) {
                return handleFoundClassIsAllowed(threadNumberAllowedToBeCreated, i);
            }
        }
        if (starIndex != -1) {
            return handleFoundClassIsAllowed(threadNumberAllowedToBeCreated, starIndex);
        }
        return false;
    }

    /**
     * Check if the variable criteria is violated.
     *
     * @param variables    The variables to check.
     * @param threadClassAllowedToBeCreated The thread classes that are allowed to be created.
     * @param threadNumberAllowedToBeCreated The number of threads allowed to be created.
     * @return The path that violates the criteria, null if no violation occurred.
     */
    private static String checkIfVariableCriteriaIsViolated(Object[] variables, String[] threadClassAllowedToBeCreated, int[] threadNumberAllowedToBeCreated) {
        for (Object variable : variables) {
            try {
                String resultingClassname = variableToClassname(variable);
                if (!checkIfClassIsAllowed(threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated, resultingClassname)) {
                    return resultingClassname;
                }
            } catch (InvalidPathException ignored) {
            }
        }
        return null;
    }
//</editor-fold>

//<editor-fold desc="Check methods">

    /**
     * Check if the thread system interaction is allowed according to security policies.
     * This method verifies that the specified thread system action (create) complies
     * with the allowed name, number and call stack criteria. If any violation is detected, a SecurityException is thrown.
     * It checks if the action is restricted based on the method call, attributes, and parameters. If a method
     * violates the thread system security rules, the action is blocked.
     *
     * @param action            The thread system action being performed (e.g., read, write, execute, delete).
     * @param declaringTypeName The name of the class declaring the method.
     * @param methodName        The name of the method being invoked.
     * @param methodSignature   The signature of the method.
     * @param attributes        The attributes of the method (if any).
     * @param parameters        The parameters of the method (if any).
     * @throws SecurityException If the thread system interaction is found to be unauthorized.
     */
    public static void checkThreadSystemInteraction(
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

        String[] threadClassAllowedToBeCreated = getValueFromSettings("threadClassAllowedToBeCreated");
        int threadClassAllowedToBeCreatedSize = threadClassAllowedToBeCreated == null ? 0 : threadClassAllowedToBeCreated.length;
        int[] threadNumberAllowedToBeCreated = getValueFromSettings("threadNumberAllowedToBeCreated");
        int threadNumberAllowedToBeCreatedSize = threadNumberAllowedToBeCreated == null ? 0 : threadNumberAllowedToBeCreated.length;

        if (threadNumberAllowedToBeCreatedSize != threadClassAllowedToBeCreatedSize) {
            throw new SecurityException(localize("security.advice.thread.allowed.size", threadNumberAllowedToBeCreatedSize, threadClassAllowedToBeCreatedSize));
        }

        final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        String threadSystemMethodToCheck = threadClassAllowedToBeCreated == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (threadSystemMethodToCheck != null) {
            String illegallyInteractedThread = null;
            if (!THREAD_SYSTEM_IGNORE_PARAMETERS.contains(declaringTypeName + "." + methodName)) {
                illegallyInteractedThread = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            }
            if (illegallyInteractedThread == null && !THREAD_SYSTEM_IGNORE_ATTRIBUTES.contains(declaringTypeName + "." + methodName)) {
                illegallyInteractedThread = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(new Object[]{declaringTypeName}, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            }
            if (illegallyInteractedThread != null) {
                throw new SecurityException(localize("security.advice.illegal.method.execution", threadSystemMethodToCheck, action, illegallyInteractedThread, fullMethodSignature));
            }
        }
    }
//</editor-fold>
//</editor-fold>
}
