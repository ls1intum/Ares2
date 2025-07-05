package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
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
public class JavaInstrumentationAdviceCommandSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

    //<editor-fold desc="Constants">

    /**
     * Map of methods with attribute index exceptions for command system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during command system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.lang.ProcessBuilder.start", IgnoreValues.allExcept(1))
    );

    /**
     * Map of methods with parameter index exceptions for command system ignore logic.
     *
     * <p>Description: Specifies for certain methods which parameter index should be exempted
     * from ignore rules during command system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries(
            Map.entry("java.lang.Runtime.exec", IgnoreValues.allExcept(0))
    );
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

    //<editor-fold desc="Command system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks if a class name is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPaths not null or if the given path does not match one of the allowedPatterns.
     *
     * @since 2.0.0
     * @author Markus
     * @param actualFullCommand the class name of the command being requested
     * @param allowedCommands the command classes that are allowed to be created
     * @param allowedArguments the number of commands allowed to be created
     * @return true if path is forbidden; false otherwise
     */
    private static boolean checkIfCommandIsForbidden(@Nullable String[] actualFullCommand, @Nullable String[] allowedCommands, @Nullable String[][] allowedArguments) {
        if (actualFullCommand == null) {
            return false;
        }
        if (allowedCommands == null || allowedCommands.length == 0 || allowedArguments == null || allowedArguments.length == 0) {
            return true;
        }
        @Nullable String actualCommand = actualFullCommand[0];
        @Nullable String[] actualArgument = actualFullCommand.length > 1 ? Arrays.copyOfRange(actualFullCommand, 1, actualFullCommand.length) : new String[0];

        for (int i = 0; i < allowedCommands.length; i++) {
            @Nonnull String allowedCommand = allowedCommands[i];
            @Nullable String[] allowedArgument = allowedArguments[i];
            if (allowedCommand.equals(actualCommand)) {
                return !Arrays.deepEquals(allowedArgument, actualArgument) || actualArgument.length == 0;
            }
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /**
     * Converts a variable value to a command string.
     * @param variableValue the value of the variable to convert
     * @return the command string representation of the variable value
     */
    @Nullable
    private static String[] variableToCommand(@Nullable Object variableValue) {
        if (variableValue == null) {
            return null;
        } else if (variableValue instanceof String[] && ((String[]) variableValue).length != 0) {
            return (String[]) variableValue;
        } else if (variableValue instanceof List<?> && ((List<?>) variableValue).stream().allMatch(o -> o instanceof String)) {
            return ((List<String>) variableValue).toArray(new String[0]);
        } else if (variableValue instanceof String) {
            return ((String) variableValue).split(" ");
        } else {
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
     * @param allowedCommands whitelist of allowed command classes
     * @param allowedArguments the number of commands allowed to be created
     * @return true if a violation is found, false otherwise
     */
    public static boolean analyseViolation(
            @Nullable Object observedVariable,
            @Nullable String[] allowedCommands,
            @Nullable String[][] allowedArguments
    ) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return false;
        } else if (observedVariable instanceof List<?>) {
            List<?> list = (List<?>) observedVariable;
            if (!list.stream().allMatch(o -> o instanceof String && !((String) o).contains(" "))) {
                for (Object element : list) {
                    if (analyseViolation(element, allowedCommands, allowedArguments)) {
                        return true;
                    }
                }
            }
        }
        if (observedVariable.getClass().isArray() || observedVariable instanceof String || observedVariable instanceof List<?>) {
            String[] command = variableToCommand(observedVariable);
            return checkIfCommandIsForbidden(command, allowedCommands, allowedArguments);
        }
        return false;
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
     * @param allowedCommands the command classes that are allowed to be created
     * @param allowedArguments the number of commands allowed to be created
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedCommands, @Nullable String[][] allowedArguments) {
        if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
            return null;
        } else if (observedVariable instanceof List<?>) {
            List<?> list = (List<?>) observedVariable;
            if (!list.stream().allMatch(o -> o instanceof String && !((String) o).contains(" "))) {
                for (Object element : (List<?>) observedVariable) {
                    String violationPath = extractViolationPath(element, allowedCommands, allowedArguments);
                    if (violationPath != null) {
                        return violationPath;
                    }
                }
            }
        }
        if (observedVariable.getClass().isArray() || observedVariable instanceof String || observedVariable instanceof List<?>) {
            String[] observedVariableAsCommand = variableToCommand(observedVariable);
            if (checkIfCommandIsForbidden(observedVariableAsCommand, allowedCommands, allowedArguments)) {
                return String.join(" ", observedVariableAsCommand);
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
            throw new SecurityException(localize("security.advice.command.allowed.size", argumentsAllowedToBePassedSize, commandsAllowedToBeExecutedSize));
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
            throw new SecurityException(localize("security.advice.illegal.command.execution", commandSystemMethodToCheck, action, commandIllegallyExecutedThroughParameter, fullMethodSignature));
        }
        //</editor-fold>
        //<editor-fold desc="Check attributes">
        @Nullable String commandIllegallyExecutedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, commandsAllowedToBeExecuted, argumentsAllowedToBePassed, COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
        if (commandIllegallyExecutedThroughAttribute != null) {
            throw new SecurityException(localize("security.advice.illegal.command.execution", commandSystemMethodToCheck, action, commandIllegallyExecutedThroughAttribute, fullMethodSignature));
        }
        //</editor-fold>
    }
    //</editor-fold>

    //</editor-fold>
}
