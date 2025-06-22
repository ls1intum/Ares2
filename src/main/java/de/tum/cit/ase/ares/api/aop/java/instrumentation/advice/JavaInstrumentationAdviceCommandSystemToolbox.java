package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
//</editor-fold>

/**
 * Utility class for Java instrumentation command system security advice.
 *
 * <p>Description: Provides static methods to enforce command system security policies at runtime
 * by checking command system interactions (create) against allowed classes and command counts,
 * call stack criteria, and variable criteria. Uses reflection to interact with test case settings
 * and localization utilities. Designed to prevent unauthorized command system operations during
 * Java application execution, especially in test and instrumentation scenarios.
 *
 * <p>Design Rationale: Centralizes command system security checks for Java instrumentation advice,
 * ensuring consistent enforcement of security policies. Uses static utility methods and a private
 * constructor to prevent instantiation. Reflection is used to decouple the toolbox from direct
 * dependencies on settings and localization classes, supporting flexible and dynamic test setups.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaInstrumentationAdviceCommandSystemToolbox {

    //<editor-fold desc="Constants">
    /**
     * List of call stack classes to ignore during command system checks.
     *
     * <p>Description: Contains class name prefixes that should be skipped when analyzing the call stack
     * to avoid false positives from test harness or localization utilities.
     */
    @Nonnull
    private static final List<String> COMMAND_SYSTEM_IGNORE_CALLSTACK = List.of();

    /**
     * Map of methods with attribute index exceptions for command system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during command system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries();

    /**
     * Map of methods with parameter index exceptions for command system ignore logic.
     *
     * <p>Description: Specifies for certain methods which parameter index should be exempted
     * from ignore rules during command system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();
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
    private JavaInstrumentationAdviceCommandSystemToolbox() {
        throw new SecurityException(
                "Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceCommandSystemToolbox is a utility class and should not be instantiated."
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

    //<editor-fold desc="Command system methods">

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
            for (@Nonnull String allowedClass : COMMAND_SYSTEM_IGNORE_CALLSTACK) {
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
     * Checks if a class name is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPaths not null or if the given path does not match one of the allowedPatterns.
     *
     * @since 2.0.0
     * @author Markus
     * @param actualFullCommand the class name of the command being requested
     * @param commandsAllowedToBeExecuted the command classes that are allowed to be created
     * @param argumentsAllowedToBePassed the number of commands allowed to be created
     * @return true if path is forbidden; false otherwise
     */
    private static boolean checkIfCommandIsForbidden(@Nullable String actualFullCommand, @Nullable String[] commandsAllowedToBeExecuted, @Nullable String[][] argumentsAllowedToBePassed) {
        if (actualFullCommand == null) {
            return false;
        }
        if (commandsAllowedToBeExecuted == null || commandsAllowedToBeExecuted.length == 0 || argumentsAllowedToBePassed == null || argumentsAllowedToBePassed.length == 0) {
            return true;
        }
        @Nonnull String[] actualCommandSplit = actualFullCommand.split(" ");
        @Nullable String actualCommand = actualCommandSplit[0];
        @Nullable String[] actualArguments = actualCommandSplit.length > 1 ? Arrays.copyOfRange(actualCommandSplit, 1, actualCommandSplit.length) : new String[0];
        for (int i = 0; i < commandsAllowedToBeExecuted.length; i++) {
            @Nonnull String allowedCommand = commandsAllowedToBeExecuted[i];
            @Nullable String[] allowedArguments = argumentsAllowedToBePassed[i];
            if (allowedCommand.equals(actualCommand)) {
                return Arrays.deepEquals(allowedArguments, actualArguments);
            }
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /** Converts a variable value to its class name.
     *
     * <p>Description: If the variable is null, throws an InvalidPathException.
     * If the variable is a lambda expression, returns "Lambda-Expression".
     * Otherwise, returns the class name of the variable.
     *
     */
    @Nonnull
    private static String variableToCommand(@Nullable Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        } else if (variableValue instanceof String && ((String) variableValue).isEmpty()) {
            return (String) variableValue;
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
     * @param commandsAllowedToBeExecuted whitelist of allowed command classes
     * @param argumentsAllowedToBePassed the number of commands allowed to be created
     * @return true if a violation is found, false otherwise
     */
    private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] commandsAllowedToBeExecuted, @Nullable String[][] argumentsAllowedToBePassed) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return false;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                if (analyseViolation(element, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
                    return true;
                }
            }
            return false;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                if (analyseViolation(element, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
                    return true;
                }
            }
            return false;
        } else {
            try {
                String observedClassname = variableToCommand(observedVariable);
                return checkIfCommandIsForbidden(observedClassname, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
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
     * @param commandsAllowedToBeExecuted the command classes that are allowed to be created
     * @param argumentsAllowedToBePassed the number of commands allowed to be created
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] commandsAllowedToBeExecuted, @Nullable String[][] argumentsAllowedToBePassed) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return null;
        } else if (observedVariable.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(observedVariable); i++) {
                Object element = Array.get(observedVariable, i);
                String violationPath = extractViolationPath(element, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else if (observedVariable instanceof List<?>) {
            for (Object element : (List<?>) observedVariable) {
                String violationPath = extractViolationPath(element, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
                if (violationPath != null) {
                    return violationPath;
                }
            }
            return null;
        } else {
            try {
                String observedClassname = variableToCommand(observedVariable);
                if (checkIfCommandIsForbidden(observedClassname, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
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
     * @param commandsAllowedToBeExecuted whitelist of allowed command classes
     * @param argumentsAllowedToBePassed the number of commands allowed to be created
     * @param ignoreVariables criteria determining which observedVariables to skip
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] observedVariables,
            @Nullable String[] commandsAllowedToBeExecuted,
            @Nullable String[][] argumentsAllowedToBePassed,
            @Nonnull IgnoreValues ignoreVariables
    ) {
        for (@Nullable Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
            if (analyseViolation(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
                return extractViolationPath(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
            }
        }
        return null;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Validates a command system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (create)
     * complies with allowed commands and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the command system action being performed
     * @param declaringTypeName the fully qualified class name of the caller
     * @param methodName the name of the method invoked
     * @param methodSignature the method signature descriptor
     * @param attributes optional method attributes
     * @param parameters optional method parameters
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static void checkCommandSystemInteraction(
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

        @Nullable String[] commandsAllowedToBeExecuted = getValueFromSettings("commandsAllowedToBeExecuted");
        int commandsAllowedToBeExecutedSize = commandsAllowedToBeExecuted == null ? 0 : commandsAllowedToBeExecuted.length;
        @Nullable String[][] argumentsAllowedToBePassed = getValueFromSettings("argumentsAllowedToBePassed");
        int argumentsAllowedToBePassedSize = argumentsAllowedToBePassed == null ? 0 : argumentsAllowedToBePassed.length;

        if (commandsAllowedToBeExecutedSize != argumentsAllowedToBePassedSize) {
            throw new SecurityException(localize("security.advice.command.allowed.size", commandsAllowedToBeExecutedSize, argumentsAllowedToBePassedSize));
        }
        //</editor-fold>
        //<editor-fold desc="Get information from attributes">
        @Nonnull final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String commandSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (commandSystemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String commandIllegallyExecutedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, commandsAllowedToBeExecuted, argumentsAllowedToBePassed, COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
        if (commandIllegallyExecutedThroughParameter != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", commandSystemMethodToCheck, action, commandIllegallyExecutedThroughParameter, fullMethodSignature));
        }
        //</editor-fold>
        //<editor-fold desc="Check attributes">
        @Nullable String commandIllegallyExecutedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(new Object[]{declaringTypeName}, commandsAllowedToBeExecuted, argumentsAllowedToBePassed, COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
        if (commandIllegallyExecutedThroughAttribute != null) {
            throw new SecurityException(localize("security.advice.illegal.method.execution", commandSystemMethodToCheck, action, commandIllegallyExecutedThroughAttribute, fullMethodSignature));
        }
        //</editor-fold>
    }
    //</editor-fold>

    //</editor-fold>
}
