package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;
//</editor-fold>

public aspect JavaAspectJThreadSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    //<editor-fold desc="Constants">

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

    //<editor-fold desc="Thread system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks whether the thread limit for the class at the specified index is still available.
     * If the count at that index is greater than zero, this method decrements it and returns
     * <code>false</code> (indicating it was allowed). If the count is zero or below, it returns
     * <code>true</code> (indicating the class is disallowed because the quota is exhausted).
     *
     * @param allowedThreadNumbers An array of permissible thread counts, parallel to
     *                                       the class array that determines which classes can create threads.
     * @param index                          The index corresponding to the class being checked.
     * @return <code>true</code> if no more threads can be created (disallowed), or
     *         <code>false</code> if the class is allowed to create another thread and the count was decremented.
     */
    private static boolean handleFoundClassIsForbidden(@Nullable int[] allowedThreadNumbers, int index) {
        if (allowedThreadNumbers == null) {
            return true;
        }
        boolean threadDisallowed = allowedThreadNumbers[index] <= 0;
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
     * @param allowedThreadClasses the thread classes that are allowed to be created
     * @param allowedThreadNumbers the number of threads allowed to be created
     * @return true if path is forbidden; false otherwise
     */
    private static boolean checkIfThreadIsForbidden(@Nullable String actualClassname, @Nullable String[] allowedThreadClasses, @Nullable int[] allowedThreadNumbers) {
        if (actualClassname == null) {
            return false;
        }
        if (allowedThreadClasses == null || allowedThreadClasses.length == 0 || allowedThreadNumbers == null || allowedThreadNumbers.length == 0) {
            return true;
        }
        boolean actualIsLambda = "Lambda-Expression".equals(actualClassname);
        int starIndex = -1;
        for (int i = 0; i < allowedThreadClasses.length; i++) {
            @Nonnull String allowedClassName = allowedThreadClasses[i];
            boolean allowedIsLambda = "Lambda-Expression".equals(allowedClassName);
            if ("*".equals(allowedClassName)) {
                starIndex = i;
            }
            if (allowedIsLambda && actualIsLambda) {
                return handleFoundClassIsForbidden(allowedThreadNumbers, i);
            }
            if (allowedIsLambda || actualIsLambda) {
                continue;
            }
            try {
                Class<?> allowedClass = Class.forName(allowedClassName, true, ClassLoader.getSystemClassLoader());
                Class<?> actualClass = Class.forName(actualClassname, true, ClassLoader.getSystemClassLoader());
                if (allowedClass.isAssignableFrom(actualClass)) {
                    return handleFoundClassIsForbidden(allowedThreadNumbers, i);
                }
            } catch (ClassNotFoundException | IllegalStateException | NullPointerException ignored) {
            }
        }
        if (starIndex != -1) {
            return handleFoundClassIsForbidden(allowedThreadNumbers, starIndex);
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
    @Nullable
    private static String variableToClassname(@Nullable Object variableValue) {
        try {
            if (variableValue == null) {
                return null;
            } else if (isThreadFieldHolder(variableValue)) {
                return getTaskFromThreadFieldHolder(variableValue);
            } else if (variableValue instanceof Runnable || variableValue instanceof Callable<?> || variableValue instanceof ForkJoinTask<?> || variableValue instanceof CompletableFuture<?> || variableValue instanceof Supplier<?> || variableValue instanceof Function<?, ?> || variableValue instanceof BiFunction<?, ?, ?> || variableValue instanceof CompletionStage<?>) {
                @Nonnull Class<?> variableClass = variableValue.getClass();
                return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
            } else {
                return null;
            }
        } catch (InvalidPathException ignored) {
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
     * @param allowedThreadClasses whitelist of allowed thread classes
     * @param allowedThreadNumbers the number of threads allowed to be created
     * @return true if a violation is found, false otherwise
     */
    private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedThreadClasses, @Nullable int[] allowedThreadNumbers) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return false;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                boolean violation = analyseViolation(element, allowedThreadClasses, allowedThreadNumbers);
                if (violation) {
                    return true;
                }
            }
            return false;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                boolean violation = analyseViolation(element, allowedThreadClasses, allowedThreadNumbers);
                if (violation) {
                    return true;
                }
            }
            return false;
        } else {
            String observedClassname = variableToClassname(observedVariable);
            return checkIfThreadIsForbidden(observedClassname, allowedThreadClasses, allowedThreadNumbers);
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
     * @param allowedThreadClasses the thread classes that are allowed to be created
     * @param allowedThreadNumbers the number of threads allowed to be created
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedThreadClasses, @Nullable int[] allowedThreadNumbers) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return null;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                String violationPath = extractViolationPath(element, allowedThreadClasses, allowedThreadNumbers);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                String violationPath = extractViolationPath(element, allowedThreadClasses, allowedThreadNumbers);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else {
            try {
                String observedClassname = variableToClassname(observedVariable);
                if (checkIfThreadIsForbidden(observedClassname, allowedThreadClasses, allowedThreadNumbers)) {
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
     * @param allowedThreadClasses whitelist of allowed thread classes
     * @param allowedThreadNumbers the number of threads allowed to be created
     * @param ignoreVariables criteria determining which observedVariables to skip
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] observedVariables,
            @Nullable String[] allowedThreadClasses,
            @Nullable int[] allowedThreadNumbers,
            @Nonnull IgnoreValues ignoreVariables
    ) {
        for (@Nullable Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
            if (analyseViolation(observedVariable, allowedThreadClasses, allowedThreadNumbers)) {
                return extractViolationPath(observedVariable, allowedThreadClasses, allowedThreadNumbers);
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
        @Nullable String[] allowedThreadClasses = getValueFromSettings("threadClassAllowedToBeCreated");
        int allowedThreadClassesSize = allowedThreadClasses == null ? 0 : allowedThreadClasses.length;
        @Nullable int[] allowedThreadNumbers = getValueFromSettings("threadNumberAllowedToBeCreated");
        int allowedThreadNumbersSize = allowedThreadNumbers == null ? 0 : allowedThreadNumbers.length;
        if (allowedThreadNumbersSize != allowedThreadClassesSize) {
            throw new SecurityException(localize("security.advice.thread.allowed.size", allowedThreadNumbersSize, allowedThreadClassesSize));
        }
        //</editor-fold>
        //<editor-fold desc="Get information from attributes">
        @Nonnull Object[] parameters = thisJoinPoint.getArgs();
        @Nonnull final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String systemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (systemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String illegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedThreadClasses, allowedThreadNumbers, THREAD_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
        if (illegallyInteractedThroughParameter != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", systemMethodToCheck, action, illegallyInteractedThroughParameter, fullMethodSignature));
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