package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation file system security advice.
 * <p>
 * Description: Provides static methods to enforce file system security policies
 * at runtime by checking file system interactions (read, create, overwrite,
 * execute, delete) against allowed paths, call stack criteria, and variable
 * criteria. Uses reflection to interact with test case settings and
 * localization utilities. Designed to prevent unauthorized file system
 * operations during Java application execution, especially in test and
 * instrumentation scenarios.
 * <p>
 * Design Rationale: Centralizes file system security checks for Java
 * instrumentation advice, ensuring consistent enforcement of security policies.
 * Uses static utility methods and a private constructor to prevent
 * instantiation. Reflection is used to decouple the toolbox from direct
 * dependencies on settings and localization classes, supporting flexible and
 * dynamic test setups.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public final class JavaInstrumentationAdviceFileSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

	// <editor-fold desc="Constants">
	/**
	 * Map of methods with attribute index exceptions for file system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during file system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
			Map.entry("java.io.File.delete", IgnoreValues.allExcept(1)),
			Map.entry("java.io.File.deleteOnExit", IgnoreValues.allExcept(1)),
			Map.entry("java.io.File.createNewFile", IgnoreValues.allExcept(1)),
			// ProcessBuilder.start - only check command (index 0), not flags or arguments
			Map.entry("java.lang.ProcessBuilder.start", IgnoreValues.allExcept(0)),
			Map.entry("java.lang.ProcessBuilder.startPipeline", IgnoreValues.allExcept(0)));

	/**
	 * Map of methods with parameter index exceptions for file system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during file system checks. For methods like
	 * Files.createTempFile and Files.writeString, only the first parameter (the
	 * Path) should be checked, not content or prefix/suffix strings.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries(
			// Files.createTempFile(Path dir, String prefix, String suffix,
			// FileAttribute<?>...) - only check dir (index 0)
			Map.entry("java.nio.file.Files.createTempFile", IgnoreValues.allExcept(0)),
			// Files.writeString(Path path, CharSequence csq, OpenOption...) - only check
			// path (index 0)
			Map.entry("java.nio.file.Files.writeString", IgnoreValues.allExcept(0)),
			// Files.write(Path path, byte[] bytes, OpenOption...) - only check path (index
			// 0)
			Map.entry("java.nio.file.Files.write", IgnoreValues.allExcept(0)),
			// Files.readString(Path path, Charset cs) - only check path (index 0)
			Map.entry("java.nio.file.Files.readString", IgnoreValues.allExcept(0)),
			// File.createTempFile(String prefix, String suffix) - no path parameter at all
			Map.entry("java.io.File.createTempFile", IgnoreValues.ALL),
			// Runtime.exec(String[]) - only check command (index 0), not flags like "-c"
			Map.entry("java.lang.Runtime.exec", IgnoreValues.allExcept(0)));

	private static final EnumSet<StandardOpenOption> CREATE_OPTIONS = EnumSet.of(StandardOpenOption.CREATE,
			StandardOpenOption.CREATE_NEW);

	/**
	 * Internal Ares files that should be excluded from file system interception.
	 * These are implementation details of Ares itself and should not trigger
	 * sandbox violations when accessed internally.
	 */
	private static final Set<String> INTERNAL_PATH_SUFFIXES = Set.of("ares/api/localization/Messages.class",
			"ares/api/localization/messages.class", "ares/api/localization/messages.properties",
			"ares/api/util/LruCache.class");

	// </editor-fold>

	// <editor-fold desc="Constructor">
	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * <p>
	 * Description: Throws a SecurityException if instantiation is attempted,
	 * enforcing the utility class pattern.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private JavaInstrumentationAdviceFileSystemToolbox() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationAdviceFileSystemToolbox"));
	}
	// </editor-fold>

	// <editor-fold desc="File system methods">

	// <editor-fold desc="Variable criteria methods">

	// <editor-fold desc="Forbidden handling">

	/**
	 * Checks if a Path is outside of the allowed paths whitelist.
	 * <p>
	 * Description: Returns true if allowedPathsAsStrings is null or if the given
	 * actualPath does not match one of the allowed patterns. This method resolves
	 * symlinks FIRST to prevent symlink-based sandbox escapes (TOCTOU attacks).
	 * <p>
	 * Security Note: Symlinks are resolved to their canonical form BEFORE checking
	 * against allowed paths, preventing attacks where a path like
	 * "/allowed/../../../etc/passwd" could bypass prefix checks.
	 *
	 * @since 2.0.0
	 * @author Markus
	 * @param actualPath                          the Path to test
	 * @param allowedPathsAsStrings               whitelist of allowed actualPath
	 *                                            strings
	 * @param allowNonExistingPathsToBeConsidered whether to allow paths that don't
	 *                                            exist yet
	 * @return true if actualPath is forbidden; false otherwise
	 */
	private static boolean checkIfPathIsForbidden(@Nullable Path actualPath, @Nullable String[] allowedPathsAsStrings,
			boolean allowNonExistingPathsToBeConsidered) {
		if (actualPath == null) {
			return false;
		}
		if (allowedPathsAsStrings == null || allowedPathsAsStrings.length == 0) {
			return true;
		}

		// SECURITY: Resolve symlinks FIRST to get the canonical path before any checks.
		// This prevents TOCTOU attacks where symlinks could be manipulated between
		// check and use.
		Path candidate;
		boolean actualExists = Files.exists(actualPath, LinkOption.NOFOLLOW_LINKS);

		if (actualExists) {
			try {
				// Resolve ALL symlinks to get the true canonical path
				// Do NOT use NOFOLLOW_LINKS here - we want to follow symlinks to see the real
				// target
				candidate = actualPath.toRealPath();
			} catch (IOException e) {
				// If we can't resolve the real path for an existing file, fail secure
				if (!allowNonExistingPathsToBeConsidered) {
					return true;
				}
				// Fall back to normalized absolute path if real path resolution fails
				candidate = actualPath.normalize().toAbsolutePath();
			}
		} else {
			// For non-existing paths, use normalized absolute path
			candidate = actualPath.normalize().toAbsolutePath();
		}

		boolean hasAllowedPrefix = false;
		for (String allowedPathsAsString : allowedPathsAsStrings) {
			@Nullable
			Path allowedPath = variableToPath(allowedPathsAsString, allowNonExistingPathsToBeConsidered);
			if (allowedPath == null) {
				continue;
			}

			// SECURITY: Also resolve symlinks in allowed paths to canonical form
			Path normalizedAllowedPath;
			boolean allowedExists = Files.exists(allowedPath, LinkOption.NOFOLLOW_LINKS);

			if (allowedExists) {
				try {
					// Resolve ALL symlinks in allowed path too
					normalizedAllowedPath = allowedPath.toRealPath();
				} catch (IOException e) {
					if (!allowNonExistingPathsToBeConsidered) {
						continue;
					}
					normalizedAllowedPath = allowedPath.normalize().toAbsolutePath();
				}
			} else {
				if (!allowNonExistingPathsToBeConsidered) {
					continue;
				}
				normalizedAllowedPath = allowedPath.normalize().toAbsolutePath();
			}

			if (candidate.startsWith(normalizedAllowedPath)) {
				hasAllowedPrefix = true;
				break;
			}
		}

		if (allowNonExistingPathsToBeConsidered) {
			return !hasAllowedPrefix;
		}

		if (!actualExists) {
			return false;
		}

		return !hasAllowedPrefix;
	}
	// </editor-fold>

	// <editor-fold desc="Conversion handling">
	/**
	 * Transforms variable values into a normalized absolute path.
	 * <p>
	 * Description: Converts the provided variable (Path, String, or File) into an
	 * absolute normalized Path for security checks, validating existence.
	 *
	 * @param variableValue the variable to transform into a Path
	 * @return the normalized absolute Path
	 * @throws InvalidPathException if transformation fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nullable
	private static Path variableToPath(@Nullable Object variableValue, boolean allowNonExistingPathsToBeConsidered) {
		try {
			if (variableValue == null) {
				return null;
			} else if (variableValue instanceof Path) {
				return ((Path) variableValue).normalize().toAbsolutePath();
			} else if (variableValue instanceof String) {
				// Empty string is not a valid path
				if (variableValue.equals("")) {
					throw new SecurityException(localize("security.instrumentation.invalid.path", variableValue));
				}
				// "/" is the root directory and is a valid path - let it be processed normally
				Path absolutePath = Path.of((String) variableValue).normalize().toAbsolutePath();
				if (Files.exists(absolutePath) || allowNonExistingPathsToBeConsidered) {
					return absolutePath;
				} else {
					return null;
				}
			} else if (variableValue instanceof File) {
				return Path.of(((File) variableValue).toURI()).normalize().toAbsolutePath();
			} else {
				return null;
			}
		} catch (InvalidPathException ignored) {
			return null;
		}
	}
	// </editor-fold>

	// <editor-fold desc="Violation analysis">
	/**
	 * Analyzes a variable to determine if it violates allowed paths.
	 * <p>
	 * Description: Recursively checks if the variable or its elements (if an array
	 * or List) are in violation of the allowed paths. Returns true if any element
	 * is forbidden.
	 *
	 * @since 2.0.0
	 * @author Markus
	 * @param observedVariable the variable to analyze
	 * @param allowedPaths     whitelist of allowed path strings; if null, all paths
	 *                         are considered allowed
	 * @return true if a violation is found, false otherwise
	 */
	private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedPaths,
			boolean allowNonExistingPathsToBeConsidered) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return false;
		} else if (observedVariable.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(observedVariable); i++) {
				Object element = Array.get(observedVariable, i);
				boolean violation = analyseViolation(element, allowedPaths, allowNonExistingPathsToBeConsidered);
				if (violation) {
					return true;
				}
			}
			return false;
		} else if (observedVariable instanceof List<?>) {
			for (Object element : (List<?>) observedVariable) {
				boolean violation = analyseViolation(element, allowedPaths, allowNonExistingPathsToBeConsidered);
				if (violation) {
					return true;
				}
			}
			return false;
		} else {
			Path observedPath = variableToPath(observedVariable, allowNonExistingPathsToBeConsidered);
			return checkIfPathIsForbidden(observedPath, allowedPaths, allowNonExistingPathsToBeConsidered);
		}
	}

	/**
	 * Extracts and returns the first violating path string from an array or list
	 * variable.
	 * <p>
	 * Description: Iterates through the variable’s elements (array or List),
	 * converts each to a Path if possible, and returns the string of the first path
	 * that does not satisfy the allowedPaths whitelist.
	 *
	 * @since 2.0.0
	 * @author Markus
	 * @param observedVariable the array or List to inspect
	 * @param allowedPaths     whitelist of allowed path strings
	 * @return the first violating path as a String, or null if none found
	 */
	@Nullable
	private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedPaths,
			boolean allowNonExistingPathsToBeConsidered) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return null;
		} else if (observedVariable.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(observedVariable); i++) {
				Object element = Array.get(observedVariable, i);
				String violationPath = extractViolationPath(element, allowedPaths, allowNonExistingPathsToBeConsidered);
				if (violationPath != null) {
					return violationPath;
				}
			}
			return null;
		} else if (observedVariable instanceof List<?>) {
			for (Object element : (List<?>) observedVariable) {
				String violationPath = extractViolationPath(element, allowedPaths, allowNonExistingPathsToBeConsidered);
				if (violationPath != null) {
					return violationPath;
				}
			}
			return null;
		} else {
			Path observedPath = variableToPath(observedVariable, allowNonExistingPathsToBeConsidered);
			if (observedPath != null
					&& checkIfPathIsForbidden(observedPath, allowedPaths, allowNonExistingPathsToBeConsidered)) {
				return observedPath.toString();
			}
		}
		return null;
	}

	/**
	 * Checks an array of observedVariables against allowed file system paths.
	 * <p>
	 * Description: Iterates through the filtered observedVariables (excluding those
	 * matching ignoreVariables). For each non-null variable, if it is an array or a
	 * List (excluding byte[]/Byte[]), each element is converted to a Path and
	 * tested against allowedPaths. Otherwise, the variable itself is converted to a
	 * Path and tested. The first violating path found is returned.
	 *
	 * @since 2.0.0
	 * @author Markus
	 * @param observedVariables array of values to validate
	 * @param allowedPaths      whitelist of allowed path strings; if null, all
	 *                          paths are considered allowed
	 * @param ignoreVariables   criteria determining which observedVariables to skip
	 * @return the first path (as String) that is not allowed, or null if none
	 *         violate
	 */
	private static String checkIfVariableCriteriaIsViolated(@Nonnull Object[] observedVariables,
			@Nullable String[] allowedPaths, @Nonnull IgnoreValues ignoreVariables,
			boolean allowNonExistingPathsToBeConsidered) {
		for (@Nullable
		Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
			if (analyseViolation(observedVariable, allowedPaths, allowNonExistingPathsToBeConsidered)) {
				return extractViolationPath(observedVariable, allowedPaths, allowNonExistingPathsToBeConsidered);
			}
		}
		return null;
	}
	// </editor-fold>
	// </editor-fold>

	// <editor-fold desc="Check methods">
	/**
	 * Map of class names to the parameter index containing the append boolean.
	 * <p>
	 * Description: For classes that use a boolean append parameter in their
	 * constructors, this map specifies which parameter index holds the append flag.
	 * When append is false, the operation should be treated as "overwrite" rather
	 * than "create" for existing files.
	 * </p>
	 */
	@Nonnull
	private static final Map<String, Integer> APPEND_PARAMETER_INDEX = Map.ofEntries(
			Map.entry("java.io.FileWriter", -1), // Variable position, check all booleans
			Map.entry("java.io.FileOutputStream", -1), // Variable position, check all booleans
			Map.entry("java.io.PrintWriter", -1) // Variable position, check all booleans
	);

	/**
	 * Checks if the parameters contain an append=false boolean, indicating
	 * overwrite behavior.
	 * <p>
	 * Description: For legacy I/O classes like FileWriter and FileOutputStream, the
	 * append behavior is controlled by a boolean parameter. When this parameter is
	 * false, the file is truncated/overwritten rather than appended to, which
	 * should be reported as "overwrite" instead of "create".
	 * </p>
	 *
	 * @param declaringTypeName the fully qualified class name being invoked
	 * @param parameters        the constructor/method parameters
	 * @return true if append=false was found, indicating overwrite behavior
	 * @since 2.0.0
	 */
	private static boolean hasAppendFalseParameter(@Nonnull String declaringTypeName, @Nullable Object[] parameters) {
		if (parameters == null || parameters.length == 0) {
			// PrintWriter(File) and PrintWriter(File, Charset) have no boolean append
			// parameter
			// but always truncate the file, so they should be treated as "overwrite"
			if ("java.io.PrintWriter".equals(declaringTypeName)) {
				return true; // Treat as append=false (overwrite mode)
			}
			return false;
		}

		Integer appendIndex = APPEND_PARAMETER_INDEX.get(declaringTypeName);
		if (appendIndex == null) {
			return false;
		}

		// For variable position (-1), check all boolean parameters
		// The append parameter is typically the last boolean in the constructor
		if (appendIndex == -1) {
			boolean foundBoolean = false;
			for (int i = parameters.length - 1; i >= 0; i--) {
				Object param = parameters[i];
				if (param instanceof Boolean) {
					foundBoolean = true;
					// Found a boolean parameter - if it's false, this indicates non-append mode
					return Boolean.FALSE.equals(param);
				}
			}
			// For PrintWriter: if no boolean found, default is to truncate (overwrite)
			if (!foundBoolean && "java.io.PrintWriter".equals(declaringTypeName)) {
				return true; // Treat as append=false (overwrite mode)
			}
			return false;
		}

		// For fixed position, check the specific index
		if (appendIndex < parameters.length) {
			Object param = parameters[appendIndex];
			if (param instanceof Boolean) {
				return Boolean.FALSE.equals(param);
			}
		}
		return false;
	}

	/**
	 * Determines the effective action for RandomAccessFile based on its mode
	 * string.
	 * <p>
	 * Description: RandomAccessFile uses mode strings ("r", "rw", "rws", "rwd")
	 * instead of boolean parameters. This method maps the mode to the appropriate
	 * action:
	 * <ul>
	 * <li>"r" - read-only mode → "read" action</li>
	 * <li>"rw", "rws", "rwd" - read-write modes → "overwrite" action
	 * <p>
	 * Note: RandomAccessFile with write modes is primarily used for modifying
	 * existing files. While it CAN create a file if it doesn't exist, the semantic
	 * intent when opening with "rw" mode is typically to write/modify content, not
	 * to create a new file. The "create" action is reserved for APIs specifically
	 * designed for file creation (like File.createNewFile, Files.createFile, etc.).
	 * </p>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param parameters    the constructor parameters (File/String, mode)
	 * @param defaultAction the action from the pointcut context (unused, mode
	 *                      parameter takes priority)
	 * @return the effective action based on the mode, or null if no mode string
	 *         found
	 * @since 2.0.0
	 */
	@Nullable
	private static String getRandomAccessFileModeAction(@Nullable Object[] parameters, @Nonnull String defaultAction) {
		if (parameters == null || parameters.length < 2) {
			return null;
		}
		// RandomAccessFile constructor: (File/String file, String mode)
		// The mode is always the second parameter
		Object modeParam = parameters[1];
		if (modeParam instanceof String) {
			String mode = (String) modeParam;
			if ("r".equals(mode)) {
				return "read";
			} else if ("rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode)) {
				// Write modes always result in "overwrite" action.
				// The mode parameter takes priority over the pointcut context because
				// it explicitly declares the user's intent at runtime.
				return "overwrite";
			}
		}
		return null;
	}

	/**
	 * Derives the list of file-system actions that need to be validated for a given
	 * invocation.
	 * <p>
	 * Description: Inspects the intercepted method arguments for
	 * {@link StandardOpenOption} instances and maps them onto the corresponding
	 * security actions (read, overwrite, create, delete). Also handles legacy I/O
	 * classes that use boolean append parameters. When append=false is detected for
	 * classes like FileWriter or FileOutputStream, the action is changed from
	 * "create" to "overwrite" since the file will be truncated rather than created
	 * anew. The returned list preserves insertion order and collapses duplicate
	 * actions while tracking whether non-existing paths should be allowed for each
	 * action.
	 * </p>
	 * <p>
	 * <b>Semantic Prioritization:</b> When {@code CREATE_NEW} is combined with
	 * {@code WRITE}, the method returns only "create" as the action. This reflects
	 * the user's primary intent: creating a new file. The {@code WRITE} option is
	 * technically required to write content to the newly created file, but it is an
	 * implementation detail rather than a separate security-relevant operation.
	 * Without this prioritization, Ares would generate both "create" and
	 * "overwrite" actions, requiring the policy to allow both operations even
	 * though the user only intends to create a file. This design allows policy
	 * files to express intent clearly: {@code pathsAllowedToBeCreated} grants
	 * permission to create new files (which implicitly includes initial writes),
	 * while {@code pathsAllowedToBeOverwritten} controls modifications to existing
	 * files.
	 * </p>
	 *
	 * @param defaultAction     action associated with the pointcut configuration
	 *                          (e.g., {@code overwrite})
	 * @param declaringTypeName the fully qualified class name being invoked
	 * @param parameters        intercepted method arguments that may contain
	 *                          {@link StandardOpenOption}s
	 * @return ordered list of action/allow-non-existing pairs to validate
	 * @since 2.0.0
	 */
	private static List<Map.Entry<String, Boolean>> deriveActionChecks(@Nonnull String defaultAction,
			@Nonnull String declaringTypeName, @Nullable Object[] parameters) {
		Set<StandardOpenOption> options = extractStandardOpenOptions(parameters);
		Map<String, Boolean> actions = new LinkedHashMap<>();

		// Check for FileChannel.MapMode first - this handles FileChannel.map() calls
		// Note: Using string comparison to avoid creating additional class references
		// that may not be in the agent JAR
		java.nio.channels.FileChannel.MapMode mapMode = extractMapMode(parameters);
		if (mapMode != null) {
			// Compare using the name() method to avoid switch on enum which creates
			// synthetic classes
			String mapModeName = mapMode.toString();
			if ("READ_ONLY".equals(mapModeName)) {
				// READ_ONLY mapping is just a read operation
				actions.put("read", false);
			} else {
				// READ_WRITE or PRIVATE mapping can modify the file
				actions.put("overwrite", false);
			}
			List<Map.Entry<String, Boolean>> result = new ArrayList<>(actions.size());
			for (Map.Entry<String, Boolean> entry : actions.entrySet()) {
				result.add(Map.entry(entry.getKey(), entry.getValue()));
			}
			return result;
		}

		// Check for DELETE_ON_CLOSE first - this takes highest priority regardless of
		// the pointcut's defaultAction. The user's primary intent is to delete the
		// file.
		// Note: DELETE_ON_CLOSE is often used with WRITE to open the channel, but the
		// semantic intent is deletion, not writing.
		boolean hasDeleteOnClose = options.contains(StandardOpenOption.DELETE_ON_CLOSE);

		// Semantic prioritization for DELETE_ON_CLOSE:
		// When DELETE_ON_CLOSE is present, we ALWAYS treat this as a "delete"
		// operation,
		// regardless of what pointcut caught it (create, overwrite, etc.)
		if (hasDeleteOnClose) {
			actions.put("delete", false);
		}
		// Semantic prioritization: CREATE_NEW indicates the primary intent is file
		// creation.
		// When CREATE_NEW is present with WRITE, we treat this as a pure "create"
		// operation.
		// The WRITE option is technically required to write content to the new file,
		// but it
		// does not represent a separate "overwrite" intent - the file doesn't exist
		// yet.
		// This allows policies to grant "create" permission without also requiring
		// "overwrite".
		else {
			boolean hasCreateNew = options.contains(StandardOpenOption.CREATE_NEW);
			boolean hasWrite = options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND)
					|| options.contains(StandardOpenOption.TRUNCATE_EXISTING);

			if (hasCreateNew && hasWrite) {
				// Primary intent is file creation; WRITE is just an implementation detail
				actions.put("create", true);
			} else {
				// Standard processing for other option combinations
				// Note: Using if-else chain instead of switch on enum to avoid creating
				// synthetic SwitchMap inner classes that may not be in the agent JAR

				// Semantic rule: If WRITE (or related) options are present, READ is implicit
				// and should not be validated separately. The primary intent is to
				// write/modify,
				// and read access is typically needed to support that (e.g., MappedByteBuffer
				// with READ + WRITE + TRUNCATE_EXISTING). We only validate the write operation.
				boolean hasWriteOption = options.contains(StandardOpenOption.WRITE)
						|| options.contains(StandardOpenOption.APPEND)
						|| options.contains(StandardOpenOption.TRUNCATE_EXISTING);

				for (StandardOpenOption option : options) {
					String optionName = option.name();
					if ("CREATE".equals(optionName) || "CREATE_NEW".equals(optionName)) {
						mergeBoolean(actions, "create", true);
					} else if ("WRITE".equals(optionName) || "APPEND".equals(optionName)
							|| "TRUNCATE_EXISTING".equals(optionName)) {
						mergeBoolean(actions, "overwrite", false);
					} else if ("READ".equals(optionName)) {
						// Only add "read" action if no write options are present
						// When READ is combined with WRITE, the intent is to modify the file,
						// and read access is just a supporting mechanism
						if (!hasWriteOption) {
							mergeBoolean(actions, "read", false);
						}
					} else if ("DELETE_ON_CLOSE".equals(optionName)) {
						mergeBoolean(actions, "delete", false);
					}
					// Ignore other options like SYNC, DSYNC, SPARSE, NOFOLLOW_LINKS
				}
			}
		}

		if (actions.isEmpty()) {
			// If OpenOptions were found but none matched the expected actions for this
			// pointcut,
			// this pointcut is not responsible for this call - return empty list to skip
			// validation.
			// This prevents multiple pointcuts from all trying to validate the same call.
			if (!options.isEmpty()) {
				return Collections.emptyList();
			}
			// Check for RandomAccessFile mode string first
			if ("java.io.RandomAccessFile".equals(declaringTypeName)) {
				String modeAction = getRandomAccessFileModeAction(parameters, defaultAction);
				if (modeAction != null) {
					actions.put(modeAction, shouldAllowNonExistingByDefault(modeAction));
				} else {
					actions.put(defaultAction, shouldAllowNonExistingByDefault(defaultAction));
				}
			}
			// Check for legacy I/O classes with boolean append parameter
			// If append=false is detected, change "create" to "overwrite" since the file
			// will be truncated
			else {
				String effectiveAction = defaultAction;
				if ("create".equals(defaultAction) && hasAppendFalseParameter(declaringTypeName, parameters)) {
					effectiveAction = "overwrite";
				}
				actions.put(effectiveAction, shouldAllowNonExistingByDefault(effectiveAction));
			}
		}

		// Convert the LinkedHashMap entries to a list
		// Note: Avoid using lambda expressions here as they create anonymous inner
		// classes
		// that may not be included in the agent JAR and cause NoClassDefFoundError at
		// runtime
		List<Map.Entry<String, Boolean>> result = new ArrayList<>(actions.size());
		for (Map.Entry<String, Boolean> entry : actions.entrySet()) {
			result.add(Map.entry(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	/**
	 * Merge a boolean value into the actions map using logical OR.
	 * <p>
	 * Description: This helper method replaces Map.merge with Boolean::logicalOr to
	 * avoid creating lambda/method-reference inner classes that may not be included
	 * in the agent JAR and cause NoClassDefFoundError at runtime.
	 * </p>
	 *
	 * @param actions the map to update
	 * @param key     the key to merge
	 * @param value   the value to merge (true = allow non-existing paths)
	 * @since 2.0.0
	 */
	private static void mergeBoolean(Map<String, Boolean> actions, String key, Boolean value) {
		Boolean existing = actions.get(key);
		if (existing == null) {
			actions.put(key, value);
		} else {
			// Logical OR: if either existing or new value is true, result is true
			actions.put(key, existing || value);
		}
	}

	/**
	 * Determines whether the default action allows paths that do not yet exist.
	 * <p>
	 * Description: The create action is the only one that implicitly authorises
	 * interactions with non-existing paths. All other actions require the path to
	 * be present.
	 * </p>
	 *
	 * @param action action identifier to evaluate
	 * @return {@code true} if paths may be missing, {@code false} otherwise
	 * @since 2.0.0
	 */
	private static boolean shouldAllowNonExistingByDefault(@Nonnull String action) {
		return "create".equals(action);
	}

	/**
	 * Extracts all {@link StandardOpenOption}s from the provided method arguments.
	 * <p>
	 * Description: Traverses arrays, collections, and nested option containers to
	 * normalise the input into an {@link EnumSet} of distinct
	 * {@link StandardOpenOption} values.
	 * </p>
	 *
	 * @param parameters intercepted method arguments
	 * @return set of discovered {@link StandardOpenOption}s (empty if none found)
	 * @since 2.0.0
	 */
	private static Set<StandardOpenOption> extractStandardOpenOptions(@Nullable Object[] parameters) {
		EnumSet<StandardOpenOption> collectedOptions = EnumSet.noneOf(StandardOpenOption.class);
		if (parameters == null) {
			return collectedOptions;
		}
		for (@Nullable
		Object parameter : parameters) {
			collectStandardOpenOptions(parameter, collectedOptions);
		}
		return collectedOptions;
	}

	/**
	 * Extracts a {@link java.nio.channels.FileChannel.MapMode} from method
	 * parameters if present.
	 * <p>
	 * Description: Scans parameters looking for a MapMode instance, which indicates
	 * whether a FileChannel.map() operation is read-only or read-write.
	 * </p>
	 *
	 * @param parameters intercepted method arguments
	 * @return the MapMode if found, or null if not present
	 * @since 2.0.0
	 */
	@Nullable
	private static java.nio.channels.FileChannel.MapMode extractMapMode(@Nullable Object[] parameters) {
		if (parameters == null) {
			return null;
		}
		for (Object parameter : parameters) {
			if (parameter instanceof java.nio.channels.FileChannel.MapMode) {
				return (java.nio.channels.FileChannel.MapMode) parameter;
			}
		}
		return null;
	}

	/**
	 * Collects {@link StandardOpenOption}s from a single candidate object.
	 * <p>
	 * Description: Supports direct {@link StandardOpenOption} instances, the wider
	 * {@link OpenOption} abstraction, arrays, and {@link Collection} containers,
	 * recursing into nested structures where required.
	 * </p>
	 *
	 * @param candidate potential holder of {@link StandardOpenOption}s
	 * @param target    accumulation set for discovered options
	 * @since 2.0.0
	 */
	private static void collectStandardOpenOptions(@Nullable Object candidate,
			@Nonnull EnumSet<StandardOpenOption> target) {
		if (candidate == null) {
			return;
		}
		if (candidate instanceof StandardOpenOption) {
			target.add((StandardOpenOption) candidate);
		} else if (candidate instanceof OpenOption) {
			if (candidate instanceof StandardOpenOption) {
				target.add((StandardOpenOption) candidate);
			}
		} else if (candidate instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) candidate;
			for (Object element : collection) {
				collectStandardOpenOptions(element, target);
			}
		} else if (candidate.getClass().isArray()) {
			Class<?> componentType = candidate.getClass().getComponentType();
			if (componentType != null && OpenOption.class.isAssignableFrom(componentType)) {
				int length = Array.getLength(candidate);
				for (int i = 0; i < length; i++) {
					collectStandardOpenOptions(Array.get(candidate, i), target);
				}
			}
		}
	}

	/**
	 * Performs the security validation for a single derived file-system action.
	 * <p>
	 * Description: Reuses the previously gathered contextual information to
	 * evaluate both method parameters and instance attributes against the allowed
	 * path lists. When a violation is detected, a {@link SecurityException} is
	 * raised containing localisation-aware details.
	 * </p>
	 *
	 * @param action                              concrete file-system action under
	 *                                            inspection
	 * @param allowNonExistingPathsToBeConsidered whether non-existing paths are
	 *                                            permitted
	 * @param declaringTypeName                   fully qualified declaring type
	 *                                            name
	 * @param methodName                          method being intercepted
	 * @param methodSignature                     JVM method signature
	 * @param attributes                          instance attributes (if any)
	 * @param parameters                          intercepted method arguments
	 * @param instance                            instance on which the method is
	 *                                            invoked
	 * @param restrictedPackage                   package prefix under security
	 *                                            scrutiny
	 * @param allowedClasses                      classes allowed within the
	 *                                            restricted package
	 * @param fileSystemMethodToCheck             offending method discovered in the
	 *                                            restricted call stack
	 * @param studentCalledMethod                 external method initiating the
	 *                                            restricted call (may be null)
	 * @param fullMethodSignature                 human-readable method signature
	 *                                            for diagnostics
	 * @throws SecurityException if the interaction violates configured policies
	 * @since 2.0.0
	 */
	private static void checkFileSystemInteractionForAction(@Nonnull String action,
			boolean allowNonExistingPathsToBeConsidered, @Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nonnull String methodSignature, @Nullable Object[] attributes, @Nullable Object[] parameters,
			@Nullable Object instance, @Nullable String restrictedPackage, @Nullable String[] allowedClasses,
			@Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		// <editor-fold desc="Resolve allowed paths">
		@Nullable
		final String[] allowedPaths = getValueFromSettings(switch (action) {
		case "read" -> "pathsAllowedToBeRead";
		case "overwrite" -> "pathsAllowedToBeOverwritten";
		case "create" -> "pathsAllowedToBeCreated";
		case "execute" -> "pathsAllowedToBeExecuted";
		case "delete" -> "pathsAllowedToBeDeleted";
		default -> throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
				.localize("security.advice.file.system.unknown.action", action));
		});
		// </editor-fold>
		// <editor-fold desc="Check parameters">
		@Nullable
		String pathIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(
						parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT
								.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE),
						allowNonExistingPathsToBeConsidered);
		if (pathIllegallyInteractedThroughParameter != null) {
			// Check if this is a .class file access by ClassLoader - should be allowed
			boolean isClassLoaderAccess = pathIllegallyInteractedThroughParameter.endsWith(".class") && 
					studentCalledMethod != null && 
					(studentCalledMethod.startsWith("java.lang.Class.forName") ||
					 studentCalledMethod.startsWith("java.lang.ClassLoader") ||
					 studentCalledMethod.startsWith("jdk.internal.loader"));
			if (!isClassLoaderAccess) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
						"security.advice.illegal.file.execution", fileSystemMethodToCheck, action,
						pathIllegallyInteractedThroughParameter, fullMethodSignature
								+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
			}
		}
		// </editor-fold>
		// <editor-fold desc="Check attributes">
		@Nullable
		String pathIllegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(
						attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT
								.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE),
						allowNonExistingPathsToBeConsidered);
		if (pathIllegallyInteractedThroughAttribute != null) {
			// Check if the path is an internal path that should be allowed
			// Note: Using explicit loop instead of stream().anyMatch() with method
			// reference
			// to avoid creating lambda classes that may not be in the agent JAR
			boolean isInternalAllowed = false;
			
			// Root path "/" is used by ClassLoader during class loading and should be allowed
			// This is a side effect of how the JVM resolves classes and is not a security concern
			if (pathIllegallyInteractedThroughAttribute.equals("/")) {
				isInternalAllowed = true;
			}
			
			// .class file access by ClassLoader should be allowed
			// When the JVM loads a class (e.g., via Class.forName), it reads the .class file
			// from the filesystem. This is not a security concern as it's part of normal
			// class loading behavior, not arbitrary file access by student code.
			if (pathIllegallyInteractedThroughAttribute.endsWith(".class") && 
					studentCalledMethod != null && 
					(studentCalledMethod.startsWith("java.lang.Class.forName") ||
					 studentCalledMethod.startsWith("java.lang.ClassLoader") ||
					 studentCalledMethod.startsWith("jdk.internal.loader"))) {
				isInternalAllowed = true;
			}
			
			for (String suffix : INTERNAL_PATH_SUFFIXES) {
				if (pathIllegallyInteractedThroughAttribute.endsWith(suffix)) {
					isInternalAllowed = true;
					break;
				}
			}

			if (!isInternalAllowed) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
						"security.advice.illegal.file.execution", fileSystemMethodToCheck, action,
						pathIllegallyInteractedThroughAttribute, fullMethodSignature
								+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
			}
		}
		// </editor-fold>
	}

	/**
	 * Validates a file system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (read, overwrite, create,
	 * execute, delete) complies with allowed paths and call stack criteria. Throws
	 * SecurityException if a policy violation is detected.
	 *
	 * @param action            the file system action being performed
	 * @param declaringTypeName the fully qualified class name of the caller
	 * @param methodName        the name of the method invoked
	 * @param methodSignature   the method signature descriptor
	 * @param attributes        optional method attributes
	 * @param parameters        optional method parameters
	 * @throws SecurityException if unauthorized access is detected
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static void checkFileSystemInteraction(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		// <editor-fold desc="Check instrumentation mode early">
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
			return;
		}
		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		@Nullable
		final String[] allowedClasses = getValueFromSettings("allowedListedClasses");
		// </editor-fold>
		// <editor-fold desc="Get information from attributes">
		@Nonnull
		final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String fileSystemMethodToCheck = (restrictedPackage == null) ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
		if (fileSystemMethodToCheck == null) {
			return;
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		// </editor-fold>

		List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, declaringTypeName, parameters);
		for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
			checkFileSystemInteractionForAction(actionCheck.getKey(), Boolean.TRUE.equals(actionCheck.getValue()),
					declaringTypeName, methodName, methodSignature, attributes, parameters, instance, restrictedPackage,
					allowedClasses, fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
		}
	}
	// </editor-fold>
}
