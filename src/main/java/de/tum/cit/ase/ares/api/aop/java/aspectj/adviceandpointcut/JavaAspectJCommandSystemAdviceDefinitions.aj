package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;
//</editor-fold>

/**
 * Aspect for Java AspectJ command system security advice.
 * <p>
 * Description: Provides advice that enforces command system security policies at
 * runtime by checking command system interactions (create) against allowed
 * classes and command counts, call stack criteria, and variable criteria. Uses
 * reflection to interact with test case settings and localization utilities.
 * Designed to prevent unauthorized command system operations during Java
 * application execution, especially in test and instrumentation scenarios.
 * <p>
 * Design Rationale: Centralizes command system security checks for Java AspectJ
 * advice, ensuring consistent enforcement of security policies. It is the
 * AspectJ counterpart of {@code JavaInstrumentationAdviceCommandSystemToolbox}.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJCommandSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

	// <editor-fold desc="Constants">

	/**
	 * Resolves the index of a named field within the given class.
	 * <p>
	 * Description: Returns the positional index of the first field whose name
	 * matches {@code fieldName}. Used to avoid hard-coding field indices that can
	 * shift across JDK versions (e.g. JDK 21 added a LOGGER field to
	 * {@link ProcessBuilder}).
	 */
	private static int findFieldIndex(Class<?> clazz, String fieldName) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i].getName())) {
				return i;
			}
		}
		// Fail closed (mirrors the instrumentation backend): a not-found field must not
		// silently default to index 0, which would make the attribute ignore-filter keep
		// the wrong field and skip the command that actually needs checking.
		throw new SecurityException(localize("security.instrumentation.field.not.found", fieldName, clazz.getName()));
	}

	/**
	 * Map of methods with attribute index exceptions for command system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during command system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(Map.entry(
			"java.lang.ProcessBuilder.start", IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))));

	/**
	 * Map of methods with parameter index exceptions for command system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during command system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map
			.ofEntries(Map.entry("java.lang.Runtime.exec", IgnoreValues.allExcept(0)));
	// </editor-fold>

	// <editor-fold desc="Command system methods">

	// <editor-fold desc="Variable criteria methods">

	// <editor-fold desc="Forbidden handling">

	/**
	 * Checks if a command target is outside of the allowed commands whitelist.
	 * <p>
	 * Description: Returns {@code true} if the allowed lists are null/empty, or if
	 * no entry in the parallel allowlists matches both the command name and the
	 * arguments of the target. Command matching is delegated to
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
	private static boolean checkIfCommandIsForbidden(@Nullable CommandTarget actual, @Nullable String[] allowedCommands,
			@Nullable String[][] allowedArguments) {
		if (actual == null) {
			return false;
		}
		if (allowedCommands == null || allowedCommands.length == 0 || allowedArguments == null
				|| allowedArguments.length == 0) {
			return true;
		}
		for (int i = 0; i < allowedCommands.length; i++) {
			String allowedCommand = allowedCommands[i];
			String[] allowedArgument = allowedArguments[i];
			if (commandMatches(actual.command(), allowedCommand)
					&& argumentsMatch(allowedArgument, actual.arguments())) {
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
	 * Checks if actual arguments match allowed arguments with relative-vs-absolute
	 * path tolerance.
	 * <p>
	 * Description: Each argument must match exactly, or (for a non-empty allowed
	 * token) the actual argument must end with the allowed token on a
	 * path-separator boundary, so a relative path in the policy matches an absolute
	 * path at runtime. Substring ("contains") matching is intentionally NOT used: it
	 * would let a student append arbitrary content to an allowed argument and defeat
	 * argument pinning.
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
			@Nonnull
			String allowed = allowedArguments[i];
			@Nonnull
			String actual = actualArguments[i];
			// Exact match
			if (allowed.equals(actual)) {
				continue;
			}
			// Relative-vs-absolute path tolerance: a relative allowed path matches an
			// actual absolute path that ends with it on a path-separator boundary (e.g.
			// allowed "src/main/file.sh" matches actual "/Users/.../src/main/file.sh").
			// Guarded on a non-empty allowed token so an empty policy value cannot match an
			// arbitrary argument. Substring ("contains") matching is deliberately NOT used:
			// it would let a student append arbitrary content (allowed "echo hi" matching
			// actual "echo hi; rm -rf ~"), which defeats argument pinning.
			if (!allowed.isEmpty() && (actual.endsWith("/" + allowed) || actual.endsWith("\\" + allowed))) {
				continue;
			}
			// No match
			return false;
		}
		return true;
	}
	// </editor-fold>

	// <editor-fold desc="Conversion handling">

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
		} else if (variableValue instanceof List<?>
				&& ((List<?>) variableValue).stream().allMatch(o -> o instanceof String)) {
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
	// </editor-fold>

	// <editor-fold desc="Violation analysis">

	/**
	 * Analyzes a variable to determine if it violates allowed commands.
	 * <p>
	 * Description: Recursively checks if the variable or its elements (if an array
	 * or List) are in violation of the allowed commands. Returns true if any
	 * element is forbidden.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariable the variable to analyze
	 * @param allowedCommands  whitelist of allowed commands
	 * @param allowedArguments the allowed arguments per command
	 * @return true if a violation is found, false otherwise
	 */
	public static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedCommands,
			@Nullable String[][] allowedArguments) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return false;
		} else if (observedVariable instanceof List<?>) {
			List<?> list = (List<?>) observedVariable;
			// Recurse only when elements are themselves nested Lists/arrays (i.e. multiple
			// commands). A flat List<String> is a single (command, arg, arg, ...) sequence
			// and must be handed to variableToCommand whole; otherwise individual args that
			// happen to contain spaces (e.g. "echo $PATH") would be re-checked as
			// standalone
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
		if (observedVariable.getClass().isArray() || observedVariable instanceof String
				|| observedVariable instanceof List<?>) {
			CommandTarget command = variableToCommand(observedVariable);
			return checkIfCommandIsForbidden(command, allowedCommands, allowedArguments);
		}
		return false;
	}

	/**
	 * Extracts and returns the first violating command string from an array or list
	 * variable.
	 * <p>
	 * Description: Iterates through the variable’s elements (array or List),
	 * converts each to a command array if possible, and returns the string of the
	 * first command that does not satisfy the allowed commands whitelist.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariable the array or List to inspect
	 * @param allowedCommands  the commands that are allowed to be executed
	 * @param allowedArguments the allowed arguments per command
	 * @return the first violating command as a String, or null if none found
	 */
	@Nullable
	private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedCommands,
			@Nullable String[][] allowedArguments) {
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
		if (observedVariable.getClass().isArray() || observedVariable instanceof String
				|| observedVariable instanceof List<?>) {
			CommandTarget observedCommand = variableToCommand(observedVariable);
			if (checkIfCommandIsForbidden(observedCommand, allowedCommands, allowedArguments)) {
				return observedCommand != null ? observedCommand.toDisplayString() : null;
			}
		}
		return null;
	}

	/**
	 * Extracts the first command target that also represents a forbidden executable
	 * file path.
	 *
	 * @param observedVariable      intercepted command variable
	 * @param pathsAllowedToExecute executable file allow-list
	 * @return the first forbidden executable file path, or null if none is found
	 */
	@Nullable
	private static String extractExecutablePathViolation(@Nullable Object observedVariable,
			@Nullable String[] pathsAllowedToExecute) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return null;
		} else if (observedVariable instanceof List<?>) {
			List<?> list = (List<?>) observedVariable;
			if (list.stream().anyMatch(o -> o instanceof List<?> || (o != null && o.getClass().isArray()))) {
				for (Object element : list) {
					String violationPath = extractExecutablePathViolation(element, pathsAllowedToExecute);
					if (violationPath != null) {
						return violationPath;
					}
				}
				return null;
			}
		}
		if (observedVariable.getClass().isArray() || observedVariable instanceof String
				|| observedVariable instanceof List<?>) {
			CommandTarget observedCommand = variableToCommand(observedVariable);
			return extractExecutablePathViolation(observedCommand, pathsAllowedToExecute);
		}
		return null;
	}

	/**
	 * Extracts the command path if it points to a forbidden executable file.
	 *
	 * @param observedCommand       parsed command target
	 * @param pathsAllowedToExecute executable file allow-list
	 * @return the forbidden executable file path, or null if the command is not a
	 *         file path or is allowed
	 */
	@Nullable
	private static String extractExecutablePathViolation(@Nullable CommandTarget observedCommand,
			@Nullable String[] pathsAllowedToExecute) {
		if (observedCommand == null || observedCommand.command() == null) {
			return null;
		}
		if (pathsAllowedToExecute == null || pathsAllowedToExecute.length == 0) {
			// No executable-path allow-list configured: the command-name allow-list (already
			// checked by the caller) governs; do not additionally restrict by executable file
			// path. This keeps pathsAllowedToBeExecuted an opt-in narrowing rather than a
			// deny-all that would block every allow-listed command.
			return null;
		}
		@Nullable
		Path commandPath = resolveExecutable(observedCommand.command());
		if (commandPath == null) {
			// An execute-path list IS configured, but the command resolves to no existing
			// executable file (including a bare name not found on PATH), so it cannot be
			// proven to be on the list and is denied rather than silently passed.
			return observedCommand.command();
		}
		for (String allowedPathAsString : pathsAllowedToExecute) {
			@Nullable
			Path allowedPath = commandToComparablePath(allowedPathAsString);
			if (allowedPath != null && pathMatches(commandPath, allowedPath)) {
				return null;
			}
		}
		return commandPath.toString();
	}

	/**
	 * Resolves a command token to an existing executable file, searching
	 * {@code PATH} for bare command names so that the executable allow-list also
	 * constrains commands invoked without an explicit path (e.g. {@code git}).
	 *
	 * @param command command token
	 * @return the resolved existing path, or null if no executable file was found
	 */
	@Nullable
	private static Path resolveExecutable(@Nonnull String command) {
		// A token containing a path separator is used by the OS directly (absolute or
		// relative to the working directory); resolve it literally. A bare command name
		// is NOT resolved against the working directory by the OS - it is looked up on
		// PATH only - so we must not treat "git" as "./git" here.
		if (command.indexOf('/') >= 0 || command.indexOf('\\') >= 0) {
			return commandToExistingPath(command);
		}
		@Nullable
		String pathEnvironment = System.getenv("PATH");
		if (pathEnvironment == null || pathEnvironment.isBlank()) {
			return null;
		}
		for (String directory : pathEnvironment.split(java.io.File.pathSeparator)) {
			// A blank PATH entry denotes the current working directory on POSIX.
			String searchDirectory = directory.isBlank() ? "." : directory;
			for (String extension : executableExtensions()) {
				@Nullable
				Path candidate = commandToExistingPath(searchDirectory + java.io.File.separator + command + extension);
				// Mirror the OS PATH search: skip non-executable matches and keep looking, so
				// an allowed-but-non-executable file cannot mask a later executable one.
				if (candidate != null && Files.isExecutable(candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the executable filename extensions to try when searching
	 * {@code PATH}. On non-Windows platforms only the bare name is used; on Windows
	 * the entries of {@code PATHEXT} are appended.
	 *
	 * @return the extensions to append to a bare command name (always includes "")
	 */
	@Nonnull
	private static String[] executableExtensions() {
		@Nullable
		String osName = System.getProperty("os.name");
		boolean isWindows = osName != null && osName.toLowerCase(java.util.Locale.ROOT).contains("win");
		if (!isWindows) {
			return new String[] { "" };
		}
		@Nullable
		String pathExtensions = System.getenv("PATHEXT");
		if (pathExtensions == null || pathExtensions.isBlank()) {
			return new String[] { "", ".EXE", ".BAT", ".CMD", ".COM" };
		}
		String[] rawExtensions = pathExtensions.split(java.io.File.pathSeparator);
		String[] extensionsWithBare = new String[rawExtensions.length + 1];
		extensionsWithBare[0] = "";
		System.arraycopy(rawExtensions, 0, extensionsWithBare, 1, rawExtensions.length);
		return extensionsWithBare;
	}

	/**
	 * Resolves a command token to a comparable existing path.
	 *
	 * @param command command token
	 * @return normalised path, or null if the command is not an existing path
	 */
	@Nullable
	private static Path commandToExistingPath(@Nonnull String command) {
		@Nullable
		Path path = commandToComparablePath(command);
		if (path == null || !Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return null;
		}
		return path;
	}

	/**
	 * Resolves a path-like command or policy entry for allow-list comparison.
	 *
	 * @param pathAsString path string
	 * @return comparable absolute path, or null if the value is not a valid path
	 */
	@Nullable
	private static Path commandToComparablePath(@Nullable String pathAsString) {
		if (pathAsString == null || pathAsString.isBlank()) {
			return null;
		}
		try {
			Path path = Path.of(pathAsString).normalize().toAbsolutePath();
			if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				return path.toRealPath();
			}
			return path;
		} catch (InvalidPathException | java.io.IOException e) {
			return null;
		}
	}

	/**
	 * Checks whether the candidate path is covered by an allowed path.
	 *
	 * @param candidate   candidate path
	 * @param allowedPath allowed path or directory
	 * @return true if the candidate is equal to or below the allowed path
	 */
	private static boolean pathMatches(@Nonnull Path candidate, @Nonnull Path allowedPath) {
		return candidate.equals(allowedPath) || candidate.startsWith(allowedPath);
	}

	/**
	 * Checks an array of observedVariables against the allowed commands whitelist.
	 * <p>
	 * Description: Iterates through the filtered observedVariables (excluding those
	 * matching ignoreVariables). For each non-null variable, if it is an array or a
	 * List (excluding byte[]/Byte[]), each element is converted to a command array
	 * and tested against the allowed commands. Otherwise, the variable itself is
	 * converted and tested. The first violating command found is returned.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariables           array of values to validate
	 * @param commandsAllowedToBeExecuted whitelist of allowed commands
	 * @param argumentsAllowedToBePassed  the allowed arguments per command
	 * @param ignoreVariables             criteria determining which
	 *                                    observedVariables to skip
	 * @return the first violating command (as String) or null if none violate
	 */
	private static String checkIfVariableCriteriaIsViolated(@Nonnull Object[] observedVariables,
			@Nullable String[] commandsAllowedToBeExecuted, @Nullable String[][] argumentsAllowedToBePassed,
			@Nonnull IgnoreValues ignoreVariables) {
		for (@Nullable
		Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
			if (analyseViolation(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
				return extractViolationPath(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
			}
		}
		return null;
	}

	/**
	 * Checks whether any observed command resolves to a forbidden executable file
	 * path.
	 *
	 * @param observedVariables     intercepted command variables
	 * @param pathsAllowedToExecute executable file allow-list
	 * @param ignoreVariables       criteria determining which observed variables to
	 *                              skip
	 * @return the first forbidden executable file path, or null if none violate
	 */
	@Nullable
	private static String checkIfExecutablePathCriteriaIsViolated(@Nonnull Object[] observedVariables,
			@Nullable String[] pathsAllowedToExecute, @Nonnull IgnoreValues ignoreVariables) {
		for (@Nullable
		Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
			@Nullable
			String violationPath = extractExecutablePathViolation(observedVariable, pathsAllowedToExecute);
			if (violationPath != null) {
				return violationPath;
			}
		}
		return null;
	}
	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Check methods">

	/**
	 * Validates a command system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (create) complies with
	 * allowed commands and call stack criteria. Throws SecurityException if a
	 * policy violation is detected.
	 *
	 * @param action        the command system action being performed
	 * @param thisJoinPoint the AspectJ join point of the intercepted call
	 * @throws SecurityException if unauthorized access is detected
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static void checkCommandSystemInteraction(@Nonnull String action, @Nonnull JoinPoint thisJoinPoint) {
		// <editor-fold desc="Get information from settings">
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (aopMode == null || !aopMode.equals("ASPECTJ")) {
			return;
		}
		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		@Nullable
		final String[] allowedClasses = getValueFromSettings("allowedListedClasses");

		@Nullable
		String[] commandsAllowedToBeExecuted = getValueFromSettings("commandsAllowedToBeExecuted");
		int commandsAllowedToBeExecutedSize = commandsAllowedToBeExecuted == null ? 0
				: commandsAllowedToBeExecuted.length;
		@Nullable
		String[][] argumentsAllowedToBePassed = getValueFromSettings("argumentsAllowedToBePassed");
		int argumentsAllowedToBePassedSize = argumentsAllowedToBePassed == null ? 0 : argumentsAllowedToBePassed.length;
		@Nullable
		String[] pathsAllowedToBeExecuted = getValueFromSettings("pathsAllowedToBeExecuted");

		if (commandsAllowedToBeExecutedSize != argumentsAllowedToBePassedSize) {
			throw new SecurityException(localize("security.advice.command.allowed.size",
					argumentsAllowedToBePassedSize, commandsAllowedToBeExecutedSize));
		}
		// </editor-fold>
		// <editor-fold desc="Get information from join point">
		@Nonnull
		Object[] parameters = thisJoinPoint.getArgs();
		@Nullable
		Object instance = thisJoinPoint.getTarget();
		@Nonnull
		final String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
		@Nonnull
		final String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
		@Nonnull
		final String methodName = thisJoinPoint.getSignature().getName();
		// </editor-fold>
		// <editor-fold desc="Extract attributes from object instance">
		// Reading an instance's declared fields is best-effort: when the JVM refuses
		// access to a field (e.g. a JDK-internal field reached via Ares's own timeout
		// executor), skip that field instead of turning a JDK-side reflection limit into
		// an Ares-Code SecurityException that would abort otherwise legal student code.
		// The security check still runs over the parameters and the accessible fields.
		// Mirrors the instrumentation backend and the network AspectJ advice.
		@Nonnull
		Object[] attributes = new Object[0];
		if (instance != null) {
			@Nonnull
			Field[] fields = instance.getClass().getDeclaredFields();
			attributes = new Object[fields.length];
			for (int i = 0; i < fields.length; i++) {
				try {
					fields[i].setAccessible(true);
					attributes[i] = fields[i].get(instance);
				} catch (InaccessibleObjectException | IllegalAccessException | SecurityException | IllegalArgumentException
						| NullPointerException | ExceptionInInitializerError ignored) {
					attributes[i] = null;
				}
			}
		}
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String commandSystemMethodToCheck = (restrictedPackage == null) ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (commandSystemMethodToCheck == null) {
			return;
		}
		// </editor-fold>
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		boolean noAllowRuleConfigured = commandsAllowedToBeExecuted == null || commandsAllowedToBeExecuted.length == 0
				|| argumentsAllowedToBePassed == null || argumentsAllowedToBePassed.length == 0;
		// <editor-fold desc="Check parameters">
		@Nullable
		String commandIllegallyExecutedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(parameters, commandsAllowedToBeExecuted, argumentsAllowedToBePassed,
						COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(
								extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
		if (commandIllegallyExecutedThroughParameter != null) {
			throw new SecurityException(localize(
					"security.advice.illegal.command.execution", commandSystemMethodToCheck, action,
					commandIllegallyExecutedThroughParameter,
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
							+ " | " + buildDenialReason(noAllowRuleConfigured)));
		}
		@Nullable
		String pathIllegallyExecutedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfExecutablePathCriteriaIsViolated(parameters, pathsAllowedToBeExecuted,
						COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(
								extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
		if (pathIllegallyExecutedThroughParameter != null) {
			// Reaching this check means the command-name check on the same variables already
			// passed, i.e. the command IS allow-listed; only its executable file path is not
			// in the execute allow-list. Report that precisely instead of the misleading
			// generic "no allow rule configured" reason.
			throw new SecurityException(localize(
					"security.advice.illegal.file.execution", commandSystemMethodToCheck, action,
					pathIllegallyExecutedThroughParameter,
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
							+ " | " + localize("security.advice.denial.reason.command.executable.path.not.allowed")));
		}
		// </editor-fold>
		// <editor-fold desc="Check attributes">
		@Nullable
		String commandIllegallyExecutedThroughAttribute = (attributes == null || attributes.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(attributes, commandsAllowedToBeExecuted, argumentsAllowedToBePassed,
						COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(
								extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
		if (commandIllegallyExecutedThroughAttribute != null) {
			throw new SecurityException(localize(
					"security.advice.illegal.command.execution", commandSystemMethodToCheck, action,
					commandIllegallyExecutedThroughAttribute,
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
							+ " | " + buildDenialReason(noAllowRuleConfigured)));
		}
		@Nullable
		String pathIllegallyExecutedThroughAttribute = (attributes == null || attributes.length == 0) ? null
				: checkIfExecutablePathCriteriaIsViolated(attributes, pathsAllowedToBeExecuted,
						COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(
								extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE));
		if (pathIllegallyExecutedThroughAttribute != null) {
			// Reaching this check means the command-name check on the same attributes already
			// passed, i.e. the command IS allow-listed; only its executable file path is not
			// in the execute allow-list. Report that precisely instead of the misleading
			// generic "no allow rule configured" reason.
			throw new SecurityException(localize(
					"security.advice.illegal.file.execution", commandSystemMethodToCheck, action,
					pathIllegallyExecutedThroughAttribute,
					fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
							+ " | " + localize("security.advice.denial.reason.command.executable.path.not.allowed")));
		}
		// </editor-fold>
	}
	// </editor-fold>

	// </editor-fold>

	before(): de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJCommandSystemPointcutDefinitions.commandExecuteMethods() {
		checkCommandSystemInteraction("execute", thisJoinPoint);
	}

}
