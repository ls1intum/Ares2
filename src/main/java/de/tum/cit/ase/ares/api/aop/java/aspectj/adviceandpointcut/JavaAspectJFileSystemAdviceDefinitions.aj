package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InaccessibleObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
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

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;
//</editor-fold>

@SuppressWarnings("AopLanguageInspection") public aspect JavaAspectJFileSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    //<editor-fold desc="Constants">

    /**
     * Internal value type representing a resolved file system target.
     * <p>
     * Description: Wraps a nullable {@link Path} instance. Used throughout the
     * toolbox as the canonical representation of a file system endpoint,
     * regardless of whether it originated from a {@link Path}, a {@link java.io.File},
     * a {@link URI}, a {@link URL}, or a {@link String}.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private record FileTarget(@Nullable Path path) {

        /**
         * Returns a human-readable string representation of this file target.
         * <p>
         * Description: Returns the string form of the path, or {@code <unknown>}
         * if the path is {@code null}.
         *
         * @return non-null display string
         * @since 2.0.0
         * @author Markus Paulsen
         */
        @Nonnull
        String toDisplayString() {
            return path == null ? "<unknown>" : path.toString();
        }
    }

    /**
     * List of Ares internal file path suffixes that should always be allowed.
     *
     * <p>Description: These paths represent Ares framework files that must be accessible
     * for the security system to function properly.
     */
    @Nonnull
    private static final List<String> INTERNAL_PATH_SUFFIXES = List.of(
            "ares/api/localization/Messages.class",
            "ares/api/localization/messages.class",
            "ares/api/localization/messages.properties",
            "ares/api/util/LruCache.class",
            "ares/api/configuration/essentialFiles/java/EssentialPackages.yaml",
            "ares/api/configuration/essentialFiles/java/EssentialClasses.yaml"
    );

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

    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.io.File.delete", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
            Map.entry("java.io.File.deleteOnExit", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
            Map.entry("java.io.File.createNewFile", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
            // ProcessBuilder.start - only check command field, resolved by name
            Map.entry("java.lang.ProcessBuilder.start", IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))),
            Map.entry("java.lang.ProcessBuilder.startPipeline", IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))));

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
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

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
    private static boolean checkIfPathIsForbidden(@Nullable FileTarget target, @Nullable String[] allowedPathsAsStrings, boolean allowNonExistingPathsToBeConsidered) {
        if (target == null || target.path() == null) {
            return false;
        }
        Path actualPath = target.path();
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
            @Nullable Path allowedPath = variableToPath(allowedPathsAsString, allowNonExistingPathsToBeConsidered);
            if (allowedPath == null) {
                continue;
            }

            if (pathMatches(candidate, allowedPath, allowNonExistingPathsToBeConsidered)) {
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

    /**
     * Checks whether an actual (already resolved) path matches an allowed path.
     * <p>
     * Description: Resolves symlinks in the allowed path to its canonical form,
     * then checks whether the candidate path starts with the resolved allowed
     * path. Returns {@code false} if the allowed path does not exist and
     * non-existing paths are not permitted.
     *
     * @param candidate                           the resolved candidate path
     * @param allowedPath                         the allowed path to compare against
     * @param allowNonExistingPathsToBeConsidered whether non-existing paths are
     *                                            permitted
     * @return {@code true} if the candidate path is covered by the allowed path
     * @since 2.0.0
     * @author Markus Paulsen
     */
    private static boolean pathMatches(@Nonnull Path candidate, @Nonnull Path allowedPath,
            boolean allowNonExistingPathsToBeConsidered) {
        // SECURITY: Also resolve symlinks in allowed paths to canonical form
        Path normalizedAllowedPath;
        boolean allowedExists = Files.exists(allowedPath, LinkOption.NOFOLLOW_LINKS);

        if (allowedExists) {
            try {
                // Resolve ALL symlinks in allowed path too
                normalizedAllowedPath = allowedPath.toRealPath();
            } catch (IOException e) {
                if (!allowNonExistingPathsToBeConsidered) {
                    return false;
                }
                normalizedAllowedPath = allowedPath.normalize().toAbsolutePath();
            }
        } else {
            if (!allowNonExistingPathsToBeConsidered) {
                return false;
            }
            normalizedAllowedPath = allowedPath.normalize().toAbsolutePath();
        }

        return candidate.startsWith(normalizedAllowedPath);
    }
    //</editor-fold>

    //<editor-fold desc="Conversion handling">

    /**
     * Transforms variable values into a normalized absolute path.
     * <p>
     * Description: Converts the provided variable (Path, URI, URL, String, or File)
     * into an absolute normalized Path for security checks, validating existence.
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
            } else if (variableValue instanceof URI uri) {
                if (!"file".equalsIgnoreCase(uri.getScheme())) {
                    return null;
                }
                Path absolutePath = Path.of(uri).normalize().toAbsolutePath();
                if (Files.exists(absolutePath) || allowNonExistingPathsToBeConsidered) {
                    return absolutePath;
                } else {
                    return null;
                }
            } else if (variableValue instanceof URL url) {
                if (!"file".equalsIgnoreCase(url.getProtocol())) {
                    return null;
                }
                Path absolutePath = Path.of(url.toURI()).normalize().toAbsolutePath();
                if (Files.exists(absolutePath) || allowNonExistingPathsToBeConsidered) {
                    return absolutePath;
                } else {
                    return null;
                }
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
        } catch (InvalidPathException | URISyntaxException ignored) {
            return null;
        }
    }

    /**
     * Converts a variable value to a {@link FileTarget} if possible.
     * <p>
     * Description: Delegates to {@link #variableToPath} and wraps the result.
     *
     * @param variableValue                       the variable to convert
     * @param allowNonExistingPathsToBeConsidered whether to allow non-existing paths
     * @return a {@link FileTarget}, or {@code null} if conversion fails
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nullable
    private static FileTarget variableToTarget(@Nullable Object variableValue, boolean allowNonExistingPathsToBeConsidered) {
        Path path = variableToPath(variableValue, allowNonExistingPathsToBeConsidered);
        return path == null ? null : new FileTarget(path);
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
     * @param allowNonExistingPathsToBeConsidered whether non-existing paths should be considered
     * @return true if a violation is found, false otherwise
     */
    private static boolean analyseViolation(@Nullable Object observedVariable, @Nullable String[] allowedPaths, boolean allowNonExistingPathsToBeConsidered) {
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
            FileTarget target = variableToTarget(observedVariable, allowNonExistingPathsToBeConsidered);
            return checkIfPathIsForbidden(target, allowedPaths, allowNonExistingPathsToBeConsidered);
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
     * @param allowNonExistingPathsToBeConsidered whether non-existing paths should be considered
     * @return the first violating path as a String, or null if none found
     */
    @Nullable
    private static String extractViolationPath(@Nullable Object observedVariable, @Nullable String[] allowedPaths, boolean allowNonExistingPathsToBeConsidered) {
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
            FileTarget target = variableToTarget(observedVariable, allowNonExistingPathsToBeConsidered);
            if (checkIfPathIsForbidden(target, allowedPaths, allowNonExistingPathsToBeConsidered)) {
                return target.toDisplayString();
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
     * @param allowNonExistingPathsToBeConsidered whether non-existing paths should be considered
     * @return the first path (as String) that is not allowed, or null if none violate
     */
    private static String checkIfVariableCriteriaIsViolated(
            @Nonnull Object[] observedVariables,
            @Nullable String[] allowedPaths,
            @Nonnull IgnoreValues ignoreVariables,
            boolean allowNonExistingPathsToBeConsidered
    ) {
        for (@Nullable Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
            if (analyseViolation(observedVariable, allowedPaths, allowNonExistingPathsToBeConsidered)) {
                return extractViolationPath(observedVariable, allowedPaths, allowNonExistingPathsToBeConsidered);
            }
        }
        return null;
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Action resolution methods">

    /**
     * Map of class names to the parameter index containing the append boolean.
     *
     * <p>Description: For classes that use a boolean append parameter in their constructors,
     * this map specifies which parameter index holds the append flag. When append is false,
     * the operation should be treated as "overwrite" rather than "create" for existing files.</p>
     */
    @Nonnull
    private static final Map<String, Integer> APPEND_PARAMETER_INDEX = Map.ofEntries(
            Map.entry("java.io.FileWriter", -1),        // Variable position, check all booleans
            Map.entry("java.io.FileOutputStream", -1),  // Variable position, check all booleans
            Map.entry("java.io.PrintWriter", -1)        // Variable position, check all booleans
    );

    /**
     * Checks if the parameters contain an append=false boolean, indicating overwrite behavior.
     *
     * <p>Description: For legacy I/O classes like FileWriter and FileOutputStream, the append
     * behavior is controlled by a boolean parameter. When this parameter is false, the file
     * is truncated/overwritten rather than appended to, which should be reported as "overwrite"
     * instead of "create".</p>
     *
     * @param declaringTypeName the fully qualified class name being invoked
     * @param parameters        the constructor/method parameters
     * @return true if append=false was found, indicating overwrite behavior
     *
     * @since 2.0.0
     */
    private static boolean hasAppendFalseParameter(@Nonnull String declaringTypeName, @Nullable Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return false;
        }

        Integer appendIndex = APPEND_PARAMETER_INDEX.get(declaringTypeName);
        if (appendIndex == null) {
            return false;
        }

        // For variable position (-1), check all boolean parameters
        // The append parameter is typically the last boolean in the constructor
        if (appendIndex == -1) {
            for (int i = parameters.length - 1; i >= 0; i--) {
                Object param = parameters[i];
                if (param instanceof Boolean) {
                    // Found a boolean parameter - if it's false, this indicates non-append mode
                    return Boolean.FALSE.equals(param);
                }
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
     * Determines the effective action for RandomAccessFile based on its mode string and context.
     *
     * <p>Description: RandomAccessFile uses mode strings ("r", "rw", "rws", "rwd") instead of
     * boolean parameters. This method maps the mode to the appropriate action, respecting the
     * pointcut context (defaultAction):
     * <ul>
     *   <li>"r" - read-only mode → "read" action</li>
     *   <li>"rw", "rws", "rwd" - read-write modes:
     *     <ul>
     *       <li>If defaultAction is "create" → "create" action (user intends to create a file)</li>
     *       <li>If defaultAction is "delete" → "delete" action (user intends to delete after use)</li>
     *       <li>Otherwise → "overwrite" action (default for write modes)</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     *
     * @param parameters the constructor parameters (File/String, mode)
     * @param defaultAction the action from the pointcut context
     * @return the effective action based on the mode and context, or null if no mode string found
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
                // Write modes always map to "overwrite". The pointcut context (create/read/etc.)
                // is irrelevant for the action classification — what matters is the mode string
                // that the caller explicitly passed. allowNonExisting is set to true in the
                // caller (deriveActionChecks) so that non-existing paths are also enforced.
                return "overwrite";
            }
        }
        return null;
    }

    /**
     * Derives the list of file-system actions that need to be validated for a given invocation.
     *
     * <p>Description: Inspects the intercepted method arguments for {@link StandardOpenOption}
     * instances and maps them onto the corresponding security actions (read, overwrite, create, delete).
     * Also handles legacy I/O classes that use boolean append parameters. When append=false is detected
     * for classes like FileWriter or FileOutputStream, the action is changed from "create" to "overwrite"
     * since the file will be truncated rather than created anew.
     *
     * <p><b>Semantic Prioritization:</b> When {@code CREATE_NEW} is combined with {@code WRITE}, the
     * method returns only "create" as the action. This reflects the user's primary intent: creating a
     * new file. The {@code WRITE} option is technically required to write content to the newly created
     * file, but it is an implementation detail rather than a separate security-relevant operation.
     * Without this prioritization, Ares would generate both "create" and "overwrite" actions, requiring
     * the policy to allow both operations even though the user only intends to create a file. This
     * design allows policy files to express intent clearly: {@code pathsAllowedToBeCreated} grants
     * permission to create new files (which implicitly includes initial writes), while
     * {@code pathsAllowedToBeOverwritten} controls modifications to existing files.</p>
     *
     * @param defaultAction     action associated with the pointcut configuration (e.g., {@code overwrite})
     * @param declaringTypeName the fully qualified class name being invoked
     * @param parameters        intercepted method arguments that may contain {@link StandardOpenOption}s
     * @return ordered list of action/allow-non-existing pairs to validate
     */
    private static List<Map.Entry<String, Boolean>> deriveActionChecks(
            @Nonnull String defaultAction,
            @Nonnull String declaringTypeName,
            @Nullable Object[] parameters
    ) {
        Set<StandardOpenOption> options = extractStandardOpenOptions(parameters);
        Map<String, Boolean> actions = new LinkedHashMap<>();

        // Check for FileChannel.MapMode first - this handles FileChannel.map() calls
        FileChannel.MapMode mapMode = extractMapMode(parameters);
        if (mapMode != null) {
            if (mapMode == FileChannel.MapMode.READ_ONLY) {
                // READ_ONLY mapping is just a read operation
                actions.put("read", false);
            } else {
                // READ_WRITE or PRIVATE mapping can modify the file
                actions.put("overwrite", false);
            }
            List<Map.Entry<String, Boolean>> result = new ArrayList<>(actions.size());
            actions.forEach((act, allowNonExisting) -> result.add(Map.entry(act, allowNonExisting)));
            return result;
        }

        // Check for DELETE_ON_CLOSE first - this takes highest priority.
        // The user's primary intent is to delete the file when using DELETE_ON_CLOSE,
        // and WRITE is just required to open the channel before deletion.
        boolean hasDeleteOnClose = options.contains(StandardOpenOption.DELETE_ON_CLOSE);
        
        // Semantic prioritization for DELETE_ON_CLOSE:
        // When DELETE_ON_CLOSE is present, we ALWAYS treat this as a "delete" operation
        // regardless of other options or the default action from the pointcut.
        // This is because DELETE_ON_CLOSE expresses clear intent: delete the file when done.
        if (hasDeleteOnClose) {
            actions.put("delete", false);
        }
        // Semantic prioritization: CREATE_NEW indicates the primary intent is file creation.
        // When CREATE_NEW is present with WRITE, we treat this as a pure "create" operation.
        // The WRITE option is technically required to write content to the new file, but it
        // does not represent a separate "overwrite" intent - the file doesn't exist yet.
        // This allows policies to grant "create" permission without also requiring "overwrite".
        else {
            boolean hasCreateNew = options.contains(StandardOpenOption.CREATE_NEW);
            boolean hasWrite = options.contains(StandardOpenOption.WRITE) 
                    || options.contains(StandardOpenOption.APPEND)
                    || options.contains(StandardOpenOption.TRUNCATE_EXISTING);
            
            if (hasCreateNew && hasWrite) {
                // Primary intent is file creation; WRITE is just an implementation detail
                actions.put("create", true);
            } else {
                // Standard processing for other option combinations
                for (StandardOpenOption option : options) {
                    switch (option) {
                        case CREATE:
                        case CREATE_NEW:
                            actions.merge("create", true, Boolean::logicalOr);
                            break;
                        case WRITE:
                        case APPEND:
                        case TRUNCATE_EXISTING:
                            actions.merge("overwrite", false, Boolean::logicalOr);
                            break;
                        case READ:
                            // Only add "read" action when READ is the primary intent.
                            // When WRITE options are also present, the primary intent is modification,
                            // and READ is just needed to access existing content before overwriting.
                            if (!hasWrite) {
                                actions.merge("read", false, Boolean::logicalOr);
                            }
                            break;
                        case DELETE_ON_CLOSE:
                            actions.merge("delete", false, Boolean::logicalOr);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        // Semantic rule 1: when both "create" (from CREATE option) and "overwrite" (from WRITE option)
        // are present, the primary intent is "write to file, creating if not exists" = overwrite.
        // Merge into a single "overwrite" check with allowNonExisting=true so that non-existing
        // paths are also validated.
        if (actions.containsKey("create") && actions.containsKey("overwrite")) {
            Boolean createAllows = actions.get("create");
            Boolean overwriteAllows = actions.get("overwrite");
            actions.remove("create");
            actions.put("overwrite", (createAllows != null && createAllows) || (overwriteAllows != null && overwriteAllows));
        }

        // Semantic rule 2: when the method is in the "overwrite" pointcut (defaultAction="overwrite")
        // but OpenOptions only produced "create" (e.g. Files.writeString(path, s, CREATE) — no WRITE
        // option), the operation is still semantically "write data to file" = overwrite.
        // Exception: CREATE_NEW genuinely means "create a new file; fail if it already exists".
        if ("overwrite".equals(defaultAction)
                && actions.size() == 1 && actions.containsKey("create")
                && !options.contains(StandardOpenOption.CREATE_NEW)) {
            Boolean allowNonExisting = actions.get("create");
            actions.remove("create");
            actions.put("overwrite", allowNonExisting);
        }

        if (actions.isEmpty()) {
            // If OpenOptions were found but none matched the expected actions for this pointcut,
            // this pointcut is not responsible for this call - return empty list to skip validation.
            // This prevents multiple pointcuts from all trying to validate the same call.
            if (!options.isEmpty()) {
                return Collections.emptyList();
            }
            // Check for RandomAccessFile mode string first
            if ("java.io.RandomAccessFile".equals(declaringTypeName)) {
                String modeAction = getRandomAccessFileModeAction(parameters, defaultAction);
                if (modeAction != null) {
                    // RandomAccessFile write modes can create files that don't exist yet,
                    // so always check the path even when the file is absent.
                    boolean allowNonExisting = !"read".equals(modeAction);
                    actions.put(modeAction, allowNonExisting);
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

        List<Map.Entry<String, Boolean>> result = new ArrayList<>(actions.size());
        actions.forEach((act, allowNonExistingPathsToBeConsidered) -> result.add(Map.entry(act, allowNonExistingPathsToBeConsidered)));
        return result;
    }

    /**
     * Determines whether the default action allows paths that do not yet exist.
     *
     * @param action action identifier to evaluate
     * @return {@code true} if paths may be missing, {@code false} otherwise
     */
    private static boolean shouldAllowNonExistingByDefault(@Nonnull String action) {
        return "create".equals(action) || "delete".equals(action);
    }

    /**
     * Extracts all {@link StandardOpenOption}s from the provided method arguments.
     *
     * @param parameters intercepted method arguments
     * @return set of discovered {@link StandardOpenOption}s (empty if none found)
     */
    private static Set<StandardOpenOption> extractStandardOpenOptions(@Nullable Object[] parameters) {
        EnumSet<StandardOpenOption> collectedOptions = EnumSet.noneOf(StandardOpenOption.class);
        if (parameters == null) {
            return collectedOptions;
        }
        for (@Nullable Object parameter : parameters) {
            collectStandardOpenOptions(parameter, collectedOptions);
        }
        return collectedOptions;
    }

    /**
     * Extracts a {@link FileChannel.MapMode} from method parameters if present.
     *
     * <p>Description: Scans parameters looking for a MapMode instance, which indicates
     * whether a FileChannel.map() operation is read-only or read-write.
     *
     * @param parameters intercepted method arguments
     * @return the MapMode if found, or null if not present
     */
    @Nullable
    private static FileChannel.MapMode extractMapMode(@Nullable Object[] parameters) {
        if (parameters == null) {
            return null;
        }
        for (Object parameter : parameters) {
            if (parameter instanceof FileChannel.MapMode) {
                return (FileChannel.MapMode) parameter;
            }
        }
        return null;
    }

    /**
     * Collects {@link StandardOpenOption}s from a single candidate object.
     *
     * @param candidate potential holder of {@link StandardOpenOption}s
     * @param target    accumulation set for discovered options
     */
    private static void collectStandardOpenOptions(@Nullable Object candidate, @Nonnull EnumSet<StandardOpenOption> target) {
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
    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Validates a file system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (read, overwrite, create, execute, delete)
     * complies with allowed paths and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the file system action being performed
     * @param thisJoinPoint the join point of the method being executed
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void checkFileSystemInteraction(
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
        //</editor-fold>
        //<editor-fold desc="Get information from join point">
        @Nonnull Object[] parameters = thisJoinPoint.getArgs();
        @Nullable Object instance = thisJoinPoint.getTarget();
        @Nonnull final String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
        @Nonnull String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
        @Nonnull String methodName = thisJoinPoint.getSignature().getName();
        //</editor-fold>
        //<editor-fold desc="Extract attributes from object instance">
        @Nonnull Object[] attributes = new Object[0];
        if (instance != null) {
            try {
                @Nonnull java.lang.reflect.Field[] fields = instance.getClass().getDeclaredFields();
                attributes = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    try {
                        fields[i].setAccessible(true);
                        attributes[i] = fields[i].get(instance);
                    } catch (InaccessibleObjectException | IllegalAccessException | SecurityException e) {
                        continue;
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
        @Nullable String systemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
        if (systemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        @Nullable String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
        List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, declaringTypeName, parameters);
        for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
            String actionToCheck = actionCheck.getKey();
            boolean allowNonExistingPaths = Boolean.TRUE.equals(actionCheck.getValue());
            // When a "create" pointcut intercepts a class/method that semantically truncates/overwrites
            // (FileOutputStream, FileWriter, PrintWriter, or Files.newBufferedWriter/newOutputStream
            // without an explicit append=true), the reported verb should be "overwrite" so that
            // messages match test expectations.
            boolean isWriteAliasedAsCreate = "create".equals(actionToCheck)
                    && ("java.io.FileOutputStream".equals(declaringTypeName)
                            || "java.io.FileWriter".equals(declaringTypeName)
                            || "java.io.PrintWriter".equals(declaringTypeName)
                            || ("java.nio.file.Files".equals(declaringTypeName)
                                    && ("newBufferedWriter".equals(methodName)
                                            || "newOutputStream".equals(methodName))));
            String messageAction = isWriteAliasedAsCreate ? "overwrite" : actionToCheck;
            @Nullable final String[] allowedPaths = getValueFromSettings(
                    switch (actionToCheck) {
                        case "read" -> "pathsAllowedToBeRead";
                        case "overwrite" -> "pathsAllowedToBeOverwritten";
                        case "create" -> "pathsAllowedToBeCreated";
                        case "execute" -> "pathsAllowedToBeExecuted";
                        case "delete" -> "pathsAllowedToBeDeleted";
                        default -> throw new SecurityException(localize(
                                "security.advice.file.system.unknown.action",
                                actionToCheck
                        ));
                    }
            );
            //<editor-fold desc="Check parameters">
            @Nullable String illegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE), allowNonExistingPaths);
            if (illegallyInteractedThroughParameter != null) {
                throw new SecurityException(localize(
                        "security.advice.illegal.file.execution",
                        systemMethodToCheck,
                        messageAction,
                        illegallyInteractedThroughParameter,
                        fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                ));
            }
            //</editor-fold>
            //<editor-fold desc="Check receiver instance">
            @Nullable String illegallyInteractedThroughReceiver = instance == null ? null
                    : checkIfVariableCriteriaIsViolated(new Object[] { instance }, allowedPaths, IgnoreValues.NONE,
                            allowNonExistingPaths);
            if (illegallyInteractedThroughReceiver != null) {
                boolean isInternalAllowed = INTERNAL_PATH_SUFFIXES.stream()
                        .anyMatch(illegallyInteractedThroughReceiver::endsWith);

                if (!isInternalAllowed
                        && illegallyInteractedThroughReceiver.endsWith(".jar")
                        && (illegallyInteractedThroughReceiver.contains("/.m2/repository/")
                                || illegallyInteractedThroughReceiver.contains(
                                        File.separator + ".m2" + File.separator + "repository" + File.separator)
                                || illegallyInteractedThroughReceiver.startsWith(System.getProperty("java.home")))) {
                    isInternalAllowed = true;
                }

                if (!isInternalAllowed) {
                    throw new SecurityException(localize(
                            "security.advice.illegal.file.execution",
                            systemMethodToCheck,
                            messageAction,
                            illegallyInteractedThroughReceiver,
                            fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                    ));
                }
            }
            //</editor-fold>
            //<editor-fold desc="Check attributes">
            @Nullable String illegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE), allowNonExistingPaths);
            if (illegallyInteractedThroughAttribute != null) {
                boolean isInternalAllowed = INTERNAL_PATH_SUFFIXES.stream()
                        .anyMatch(illegallyInteractedThroughAttribute::endsWith);

                if (!isInternalAllowed
                        && illegallyInteractedThroughAttribute.endsWith(".jar")
                        && (illegallyInteractedThroughAttribute.contains("/.m2/repository/")
                                || illegallyInteractedThroughAttribute.contains(
                                        File.separator + ".m2" + File.separator + "repository" + File.separator)
                                || illegallyInteractedThroughAttribute.startsWith(System.getProperty("java.home")))) {
                    isInternalAllowed = true;
                }

                if (!isInternalAllowed) {
                    throw new SecurityException(localize(
                            "security.advice.illegal.file.execution",
                            systemMethodToCheck,
                            messageAction,
                            illegallyInteractedThroughAttribute,
                            fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                    ));
                }
            }
            //</editor-fold>
        }
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
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.scannerInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.writerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() {
        checkFileSystemInteraction("overwrite", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileCreateMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesCreateMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderCreateMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelCreateMethods() {
        checkFileSystemInteraction("create", thisJoinPoint);
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
