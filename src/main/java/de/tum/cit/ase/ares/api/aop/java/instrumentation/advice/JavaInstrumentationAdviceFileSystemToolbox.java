package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox.*;

//<editor-fold desc="imports">
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation file system security advice.
 *
 * <p>Description: Provides static methods to enforce file system security policies at runtime
 * by checking file system interactions (read, create, overwrite, execute, delete) against allowed paths,
 * call stack criteria, and variable criteria. Uses reflection to interact with test case settings
 * and localization utilities. Designed to prevent unauthorized file system operations during
 * Java application execution, especially in test and instrumentation scenarios.
 *
 * <p>Design Rationale: Centralizes file system security checks for Java instrumentation advice,
 * ensuring consistent enforcement of security policies. Uses static utility methods and a private
 * constructor to prevent instantiation. Reflection is used to decouple the toolbox from direct
 * dependencies on settings and localization classes, supporting flexible and dynamic test setups.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public final class JavaInstrumentationAdviceFileSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

    //<editor-fold desc="Constants">
    /**
     * Map of methods with attribute index exceptions for file system ignore logic.
     *
     * <p>Description: Specifies for certain methods which attribute index should be exempted
     * from ignore rules during file system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
            Map.entry("java.io.File.delete", IgnoreValues.allExcept(1)),
            Map.entry("java.io.File.deleteOnExit", IgnoreValues.allExcept(1)),
            Map.entry("java.io.File.createNewFile", IgnoreValues.allExcept(1))
    );

    /**
     * Map of methods with parameter index exceptions for file system ignore logic.
     *
     * <p>Description: Specifies for certain methods which parameter index should be exempted
     * from ignore rules during file system checks.
     */
    @Nonnull
    private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();

    private static final EnumSet<StandardOpenOption> CREATE_OPTIONS =
            EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW);

    /**
     * Internal Ares files that should be excluded from file system interception.
     * These are implementation details of Ares itself and should not trigger
     * sandbox violations when accessed internally.
     */
    private static final Set<String> INTERNAL_PATH_SUFFIXES = Set.of(
            "ares/api/localization/Messages.class",
            "ares/api/localization/messages.class",
            "ares/api/localization/messages.properties",
            "ares/api/util/LruCache.class"
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
    private JavaInstrumentationAdviceFileSystemToolbox() {
        throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
                "security.instrumentation.utility.initialization",
                "JavaInstrumentationAdviceFileSystemToolbox"
        ));
    }
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks if a Path is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPathsAsStrings is null or if the given actualPath
     * does not match one of the allowed patterns. This method resolves symlinks FIRST to
     * prevent symlink-based sandbox escapes (TOCTOU attacks).
     *
     * <p>Security Note: Symlinks are resolved to their canonical form BEFORE checking against
     * allowed paths, preventing attacks where a path like "/allowed/../../../etc/passwd" could
     * bypass prefix checks.
     *
     * @since 2.0.0
     * @author Markus
     * @param actualPath         the Path to test
     * @param allowedPathsAsStrings whitelist of allowed actualPath strings
     * @param allowNonExistingPathsToBeConsidered whether to allow paths that don't exist yet
     * @return true if actualPath is forbidden; false otherwise
     */
    private static boolean checkIfPathIsForbidden(@Nullable Path actualPath, @Nullable String[] allowedPathsAsStrings, boolean allowNonExistingPathsToBeConsidered) {
        if (actualPath == null) {
            return false;
        }
        if (allowedPathsAsStrings == null || allowedPathsAsStrings.length == 0) {
            return true;
        }

        // SECURITY: Resolve symlinks FIRST to get the canonical path before any checks.
        // This prevents TOCTOU attacks where symlinks could be manipulated between check and use.
        Path candidate;
        boolean actualExists = Files.exists(actualPath, LinkOption.NOFOLLOW_LINKS);

        if (actualExists) {
            try {
                // Resolve ALL symlinks to get the true canonical path
                // Do NOT use NOFOLLOW_LINKS here - we want to follow symlinks to see the real target
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
    //</editor-fold>

    //<editor-fold desc="Conversion handling">
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
    @Nullable
    private static Path variableToPath(@Nullable Object variableValue, boolean allowNonExistingPathsToBeConsidered) {
        try {
            if (variableValue == null) {
                return null;
            } else if (variableValue instanceof Path) {
                return ((Path) variableValue).normalize().toAbsolutePath();
            } else if (variableValue instanceof String) {
                // Easy fix for cases where an empty string or root '/'' is provided (often an incorrect entry)
                if(variableValue.equals("") || variableValue.equals("/")) {
                    throw new SecurityException(localize(
                            "security.instrumentation.invalid.path",
                            variableValue
                    ));
                }
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
            Path observedPath = variableToPath(observedVariable, allowNonExistingPathsToBeConsidered);
            return checkIfPathIsForbidden(observedPath, allowedPaths, allowNonExistingPathsToBeConsidered);
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
            Path observedPath = variableToPath(observedVariable, allowNonExistingPathsToBeConsidered);
            if (observedPath != null && checkIfPathIsForbidden(observedPath, allowedPaths, allowNonExistingPathsToBeConsidered)) {
                return observedPath.toString();
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

    //<editor-fold desc="Check methods">
    /**
     * Derives the list of file-system actions that need to be validated for a given invocation.
     *
     * <p>Description: Inspects the intercepted method arguments for {@link StandardOpenOption}
     * instances and maps them onto the corresponding security actions (read, overwrite, create, delete).
     * The returned list preserves insertion order and collapses duplicate actions while tracking whether
     * non-existing paths should be allowed for each action.</p>
     *
     * @param defaultAction action associated with the pointcut configuration (e.g., {@code overwrite})
     * @param parameters    intercepted method arguments that may contain {@link StandardOpenOption}s
     * @return ordered list of action/allow-non-existing pairs to validate
     *
     * @since 2.0.0
     */
    private static List<Map.Entry<String, Boolean>> deriveActionChecks(@Nonnull String defaultAction, @Nullable Object[] parameters) {
        Set<StandardOpenOption> options = extractStandardOpenOptions(parameters);
        Map<String, Boolean> actions = new LinkedHashMap<>();
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
                    actions.merge("read", false, Boolean::logicalOr);
                    break;
                case DELETE_ON_CLOSE:
                    actions.merge("delete", false, Boolean::logicalOr);
                    break;
                default:
                    break;
            }
        }

        if (actions.isEmpty()) {
            actions.put(defaultAction, shouldAllowNonExistingByDefault(defaultAction));
        }

        List<Map.Entry<String, Boolean>> result = new ArrayList<>(actions.size());
        actions.forEach((act, allowNonExistingPathsToBeConsidered) -> result.add(Map.entry(act, allowNonExistingPathsToBeConsidered)));
        return result;
    }

    /**
     * Determines whether the default action allows paths that do not yet exist.
     *
     * <p>Description: The create action is the only one that implicitly authorises interactions
     * with non-existing paths. All other actions require the path to be present.</p>
     *
     * @param action action identifier to evaluate
     * @return {@code true} if paths may be missing, {@code false} otherwise
     *
     * @since 2.0.0
     */
    private static boolean shouldAllowNonExistingByDefault(@Nonnull String action) {
        return "create".equals(action);
    }

    /**
     * Extracts all {@link StandardOpenOption}s from the provided method arguments.
     *
     * <p>Description: Traverses arrays, collections, and nested option containers to normalise the
     * input into an {@link EnumSet} of distinct {@link StandardOpenOption} values.</p>
     *
     * @param parameters intercepted method arguments
     * @return set of discovered {@link StandardOpenOption}s (empty if none found)
     *
     * @since 2.0.0
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
     * Collects {@link StandardOpenOption}s from a single candidate object.
     *
     * <p>Description: Supports direct {@link StandardOpenOption} instances, the wider
     * {@link OpenOption} abstraction, arrays, and {@link Collection} containers, recursing into nested
     * structures where required.</p>
     *
     * @param candidate potential holder of {@link StandardOpenOption}s
     * @param target    accumulation set for discovered options
     *
     * @since 2.0.0
     */
    private static void collectStandardOpenOptions(@Nullable Object candidate, @Nonnull EnumSet<StandardOpenOption> target) {
        if (candidate == null) {
            return;
        }
        if (candidate instanceof StandardOpenOption standardOpenOption) {
            target.add(standardOpenOption);
        } else if (candidate instanceof OpenOption openOption) {
            if (openOption instanceof StandardOpenOption standardOpenOption) {
                target.add(standardOpenOption);
            }
        } else if (candidate instanceof Collection<?> collection) {
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
     *
     * <p>Description: Reuses the previously gathered contextual information to evaluate both method
     * parameters and instance attributes against the allowed path lists. When a violation is detected,
     * a {@link SecurityException} is raised containing localisation-aware details.</p>
     *
     * @param action                      concrete file-system action under inspection
     * @param allowNonExistingPathsToBeConsidered       whether non-existing paths are permitted
     * @param declaringTypeName           fully qualified declaring type name
     * @param methodName                  method being intercepted
     * @param methodSignature             JVM method signature
     * @param attributes                  instance attributes (if any)
     * @param parameters                  intercepted method arguments
     * @param instance                    instance on which the method is invoked
     * @param restrictedPackage           package prefix under security scrutiny
     * @param allowedClasses              classes allowed within the restricted package
     * @param fileSystemMethodToCheck     offending method discovered in the restricted call stack
     * @param studentCalledMethod         external method initiating the restricted call (may be null)
     * @param fullMethodSignature         human-readable method signature for diagnostics
     *
     * @throws SecurityException if the interaction violates configured policies
     *
     * @since 2.0.0
     */
    private static void checkFileSystemInteractionForAction(
            @Nonnull String action,
            boolean allowNonExistingPathsToBeConsidered,
            @Nonnull String declaringTypeName,
            @Nonnull String methodName,
            @Nonnull String methodSignature,
            @Nullable Object[] attributes,
            @Nullable Object[] parameters,
            @Nullable Object instance,
            @Nullable String restrictedPackage,
            @Nullable String[] allowedClasses,
            @Nonnull String fileSystemMethodToCheck,
            @Nullable String studentCalledMethod,
            @Nonnull String fullMethodSignature
    ) {
        //<editor-fold desc="Resolve allowed paths">
        @Nullable final String[] allowedPaths = getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "overwrite" -> "pathsAllowedToBeOverwritten";
                    case "create" -> "pathsAllowedToBeCreated";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
                            "security.advice.file.system.unknown.action",
                            action
                    ));
                }
        );
        //</editor-fold>
        //<editor-fold desc="Check parameters">
        @Nullable String pathIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths, FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE), allowNonExistingPathsToBeConsidered);
        if (pathIllegallyInteractedThroughParameter != null) {
            throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
                    "security.advice.illegal.file.execution",
                    fileSystemMethodToCheck,
                    action,
                    pathIllegallyInteractedThroughParameter,
                    fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
            ));
        }
        //</editor-fold>
        //<editor-fold desc="Check attributes">
        @Nullable String pathIllegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE), allowNonExistingPathsToBeConsidered);
        if (pathIllegallyInteractedThroughAttribute != null) {
            boolean isInternalAllowed =
                    INTERNAL_PATH_SUFFIXES.stream()
                            .anyMatch(pathIllegallyInteractedThroughAttribute::endsWith);

            if (!isInternalAllowed) {
                throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
                        "security.advice.illegal.file.execution",
                        fileSystemMethodToCheck,
                        action,
                        pathIllegallyInteractedThroughAttribute,
                        fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                ));
            }
        }
        //</editor-fold>
    }

    /**
     * Validates a file system interaction against security policies.
     *
     * <p>Description: Verifies that the specified action (read, overwrite, create, execute, delete)
     * complies with allowed paths and call stack criteria. Throws SecurityException
     * if a policy violation is detected.
     *
     * @param action the file system action being performed
     * @param declaringTypeName the fully qualified class name of the caller
     * @param methodName the name of the method invoked
     * @param methodSignature the method signature descriptor
     * @param attributes optional method attributes
     * @param parameters optional method parameters
     * @throws SecurityException if unauthorized access is detected
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public static void checkFileSystemInteraction(
            @Nonnull String action,
            @Nonnull String declaringTypeName,
            @Nonnull String methodName,
            @Nonnull String methodSignature,
            @Nullable Object[] attributes,
            @Nullable Object[] parameters,
            @Nullable Object instance
    ) {
        //<editor-fold desc="Check instrumentation mode early">
        @Nullable final String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        @Nullable final String restrictedPackage = getValueFromSettings("restrictedPackage");
        @Nullable final String[] allowedClasses = getValueFromSettings("allowedListedClasses");
        //</editor-fold>
        //<editor-fold desc="Get information from attributes">
        @Nonnull final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        //</editor-fold>
        //<editor-fold desc="Check callstack">
        @Nullable String fileSystemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (fileSystemMethodToCheck == null) {
            return;
        }
        @Nullable String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
        //</editor-fold>

        List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, parameters);
        for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
            checkFileSystemInteractionForAction(
                    actionCheck.getKey(),
                    Boolean.TRUE.equals(actionCheck.getValue()),
                    declaringTypeName,
                    methodName,
                    methodSignature,
                    attributes,
                    parameters,
                    instance,
                    restrictedPackage,
                    allowedClasses,
                    fileSystemMethodToCheck,
                    studentCalledMethod,
                    fullMethodSignature
            );
        }
    }
    //</editor-fold>
}
