package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;

//</editor-fold>

@SuppressWarnings("AopLanguageInspection") public aspect JavaAspectJCommandSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    //<editor-fold desc="Constants">

    /**
     * Internal value type representing a resolved command target.
     * <p>
     * Description: Pairs a nullable command name with an argument array.
     * Used throughout the toolbox as the canonical representation of a command
     * invocation, regardless of whether it originated from a {@code String},
     * a {@code String[]}, or a {@code List<String>}.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private record CommandTarget(@Nullable String command, @Nonnull String[] arguments) {

        /**
         * Returns a human-readable string representation of this command target.
         *
         * @return non-null display string of the command invocation
         * @since 2.0.0
         * @author Markus Paulsen
         */
        @Nonnull
        String toDisplayString() {
            if (command == null) {
                return "<unknown>";
            }
            if (arguments.length == 0) {
                return command;
            }
            return command + " " + String.join(" ", arguments);
        }
    }

    /**
     * Resolve the index of a named field within the given class.
     *
     * <p>Description: Returns the positional index of the first field whose name
     * matches {@code fieldName}.  Used to avoid hard-coding field indices that
     * can shift across JDK versions (e.g. JDK 21 added a LOGGER field to
     * {@link ProcessBuilder}).
     */
    private static int findFieldIndex(Class<?> clazz, String fieldName) {
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fieldName.equals(fields[i].getName())) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Map of methods with attribute index exceptions for command system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during command system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.lang.ProcessBuilder.start", IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command")))
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

    //<editor-fold desc="Command system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks if a command target is outside of the allowed commands whitelist.
     * <p>
     * Description: Returns {@code true} if the allowed lists are null/empty, or
     * if no entry in the parallel allowlists matches both the command name and
     * the arguments of the target. Command matching is delegated to
     * {@link #commandMatches} and argument matching to {@link #argumentsMatch}.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param actual           the resolved command target to evaluate
     * @param allowedCommands  parallel array of allowed command names
     * @param allowedArguments parallel array of allowed argument patterns
     * @return {@code true} if the command is forbidden; {@code false} if it is
     *         explicitly allowed
     */
    private static boolean checkIfCommandIsForbidden(@Nullable CommandTarget actual, @Nullable String[] allowedCommands, @Nullable String[][] allowedArguments) {
        if (actual == null) {
            return false;
        }
        if (allowedCommands == null || allowedCommands.length == 0 || allowedArguments == null || allowedArguments.length == 0) {
            return true;
        }
        for (int i = 0; i < allowedCommands.length; i++) {
            String allowedCommand = allowedCommands[i];
            String[] allowedArgument = allowedArguments[i];
            if (commandMatches(actual.command, allowedCommand)
                    && argumentsMatch(allowedArgument, actual.arguments)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether an actual command name matches an allowed command name.
     * <p>
     * Description: Returns {@code false} if either value is null. Otherwise
     * performs an exact string equality check.
     *
     * @param actualCommand  the command name from the intercepted call; may be null
     * @param allowedCommand the command name from the security policy; may be null
     * @return {@code true} if the actual command matches the allowed command
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean commandMatches(@Nullable String actualCommand, @Nullable String allowedCommand) {
        if (actualCommand == null || allowedCommand == null) {
            return false;
        }
        return allowedCommand.equals(actualCommand);
    }

    /**
     * Checks if actual arguments match allowed arguments with flexible path
     * matching.
     * <p>
     * Description: Compares argument arrays with support for suffix matching and
     * contains matching. When an actual argument ends with an allowed argument
     * (path suffix) or contains it as a substring, it's considered a match. This
     * allows relative paths in policies to match absolute paths at runtime and
     * shell expressions containing the allowed path to match.
     *
     * @param allowedArguments the allowed arguments from policy
     * @param actualArguments  the actual arguments from the command
     * @return true if arguments match, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean argumentsMatch(@Nullable String[] allowedArguments, @Nullable String[] actualArguments) {
        if (allowedArguments == null && actualArguments == null) {
            return true;
        }
        if (allowedArguments == null || actualArguments == null) {
            return false;
        }
        if (allowedArguments.length != actualArguments.length) {
            return false;
        }
        for (int i = 0; i < allowedArguments.length; i++) {
            @Nonnull String allowed = allowedArguments[i];
            @Nonnull String actual = actualArguments[i];
            // Exact match
            if (allowed.equals(actual)) {
                continue;
            }
            // Suffix match for paths: actual "/Users/.../src/main/file.sh" matches allowed
            // "src/main/file.sh"
            if (actual.endsWith("/" + allowed) || actual.endsWith("\\" + allowed)) {
                continue;
            }
            // Contains match for shell expressions: actual
            // "'/Users/.../file.sh' > '/tmp/out' ; cat '/tmp/out'" matches allowed
            // "file.sh" when the actual argument contains the allowed value as a substring
            if (actual.contains(allowed)) {
                continue;
            }
            // No match
            return false;
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /**
     * Converts a variable value to a command string array.
     * <p>
     * Description: Converts various input types to a command array representation.
     * For String inputs, uses shell-like parsing that properly handles quoted
     * arguments.
     *
     * @param variableValue the value of the variable to convert
     * @return the command string array representation of the variable value
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nullable
    private static CommandTarget variableToCommand(@Nullable Object variableValue) {
        String[] parts = null;
        if (variableValue == null) {
            return null;
        } else if (variableValue instanceof String[] && ((String[]) variableValue).length != 0) {
            parts = (String[]) variableValue;
        } else if (variableValue instanceof List<?> && ((List<?>) variableValue).stream().allMatch(o -> o instanceof String)) {
            @SuppressWarnings("unchecked")
            List<String> stringList = (List<String>) variableValue;
            parts = stringList.toArray(new String[0]);
        } else if (variableValue instanceof String) {
            parts = parseCommandString((String) variableValue);
        }
        if (parts == null || parts.length == 0) {
            return null;
        }
        String command = parts[0];
        String[] arguments = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        return new CommandTarget(command, arguments);
    }

    /**
     * Parses a command string into an array of arguments, handling quoted strings.
     * <p>
     * Description: Implements shell-like parsing that properly handles
     * single-quoted, double-quoted, and unquoted arguments. This prevents command
     * injection attacks where malicious input could manipulate argument boundaries
     * through spaces in quoted strings.
     *
     * @param command the command string to parse
     * @return array of parsed arguments
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    private static String[] parseCommandString(@Nonnull String command) {
        List<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (escaped) {
                // Handle escaped characters
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\' && !inSingleQuote) {
                // Backslash escapes next character (except in single quotes)
                escaped = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                // Toggle single quote mode (single quotes don't interpret anything)
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                // Toggle double quote mode
                inDoubleQuote = !inDoubleQuote;
                continue;
            }

            if (c == ' ' && !inSingleQuote && !inDoubleQuote) {
                // Space outside quotes - argument boundary
                if (!current.isEmpty()) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }
                continue;
            }

            // Regular character - add to current argument
            current.append(c);
        }

        // Add final argument if present
        if (!current.isEmpty()) {
            args.add(current.toString());
        }

        return args.toArray(new String[0]);
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
            // Recurse only when elements are themselves nested Lists/arrays (i.e. multiple
            // commands). A flat List<String> is a single (command, arg, arg, ...) sequence
            // and must be handed to variableToCommand whole; otherwise individual args that
            // happen to contain spaces (e.g. "echo $PATH") would be re-checked as standalone
            // commands and never match any allow-list entry.
            if (list.stream().anyMatch(o -> o instanceof List<?> || (o != null && o.getClass().isArray()))) {
                for (Object element : list) {
                    if (analyseViolation(element, allowedCommands, allowedArguments)) {
                        return true;
                    }
                }
                return false;
            }
        }
        if (observedVariable.getClass().isArray() || observedVariable instanceof String || observedVariable instanceof List<?>) {
            CommandTarget command = variableToCommand(observedVariable);
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
            // Mirror the recursion guard from analyseViolation: only descend into nested
            // Lists/arrays so flat List<String> command sequences are matched as a whole.
            if (list.stream().anyMatch(o -> o instanceof List<?> || (o != null && o.getClass().isArray()))) {
                for (Object element : list) {
                    String violationPath = extractViolationPath(element, allowedCommands, allowedArguments);
                    if (violationPath != null) {
                        return violationPath;
                    }
                }
                return null;
            }
        }
        if (observedVariable.getClass().isArray() || observedVariable instanceof String || observedVariable instanceof List<?>) {
            CommandTarget observedCommand = variableToCommand(observedVariable);
            if (checkIfCommandIsForbidden(observedCommand, allowedCommands, allowedArguments)) {
                return observedCommand != null ? observedCommand.toDisplayString() : null;
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
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static void checkCommandSystemInteraction(
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

        @Nullable String[] commandsAllowedToBeExecuted = getValueFromSettings("commandsAllowedToBeExecuted");
        int commandsAllowedToBeExecutedSize = commandsAllowedToBeExecuted == null ? 0 : commandsAllowedToBeExecuted.length;
        @Nullable String[][] argumentsAllowedToBePassed = getValueFromSettings("argumentsAllowedToBePassed");
        int argumentsAllowedToBePassedSize = argumentsAllowedToBePassed == null ? 0 : argumentsAllowedToBePassed.length;

        if (commandsAllowedToBeExecutedSize != argumentsAllowedToBePassedSize) {
            throw new SecurityException(localize("security.advice.command.allowed.size", argumentsAllowedToBePassedSize, commandsAllowedToBeExecutedSize));
        }
        //</editor-fold>
        //<editor-fold desc="Get information from join point">
        @Nonnull Object[] parameters = thisJoinPoint.getArgs();
        @Nullable Object instance = thisJoinPoint.getTarget();
        @Nonnull final String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
        @Nonnull final String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
        @Nonnull final String methodName = thisJoinPoint.getSignature().getName();
        //</editor-fold>
        //<editor-fold desc="Extract attributes from object instance">
        @Nonnull Object[] attributes = new Object[0];
        if (instance != null) {
            try {
                @Nonnull Field[] fields = instance.getClass().getDeclaredFields();
                attributes = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    try {
                        fields[i].setAccessible(true);
                        attributes[i] = fields[i].get(instance);
                    } catch (InaccessibleObjectException e) {
                        throw new SecurityException(localize("security.instrumentation.inaccessible.object.exception", fields[i].getName(), instance.getClass().getName()), e);
                    } catch (IllegalAccessException e) {
                        throw new SecurityException(localize("security.instrumentation.illegal.access.exception", fields[i].getName(), instance.getClass().getName()), e);
                    } catch (IllegalArgumentException e) {
                        throw new SecurityException(localize("security.instrumentation.illegal.argument.exception", fields[i].getName(), fields[i].getDeclaringClass().getName(), instance.getClass().getName()), e);
                    } catch (NullPointerException e) {
                        throw new SecurityException(localize("security.instrumentation.null.pointer.exception", fields[i].getName(), instance.getClass().getName()), e);
                    } catch (ExceptionInInitializerError e) {
                        throw new SecurityException(localize("security.instrumentation.exception.in-initializer.error", fields[i].getName(), instance.getClass().getName()), e);
                    }
                }
            } catch (SecurityException e) {
                throw e;
            }
        }
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String commandSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
        if (commandSystemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        @Nullable String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
        //<editor-fold desc="Check parameters">
        @Nullable String commandIllegallyExecutedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, commandsAllowedToBeExecuted, argumentsAllowedToBePassed, COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
        if (commandIllegallyExecutedThroughParameter != null) {
            throw new SecurityException(localize(
                    "security.advice.illegal.command.execution",
                    commandSystemMethodToCheck,
                    action,
                    commandIllegallyExecutedThroughParameter,
                    fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
            ));
        }
        //</editor-fold>
        //<editor-fold desc="Check attributes">
        @Nullable String commandIllegallyExecutedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, commandsAllowedToBeExecuted, argumentsAllowedToBePassed, COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
        if (commandIllegallyExecutedThroughAttribute != null) {
            throw new SecurityException(localize(
                    "security.advice.illegal.command.execution",
                    commandSystemMethodToCheck,
                    action,
                    commandIllegallyExecutedThroughAttribute,
                    fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
            ));
        }
        //</editor-fold>
    }
    //</editor-fold>

    //</editor-fold>

    before(): de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJCommandSystemPointcutDefinitions.commandExecuteMethods() {
        checkCommandSystemInteraction("execute", thisJoinPoint);
    }

}
