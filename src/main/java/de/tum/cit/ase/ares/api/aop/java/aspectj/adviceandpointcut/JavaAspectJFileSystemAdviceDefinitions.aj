package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
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

public aspect JavaAspectJFileSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    //<editor-fold desc="Constants">

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
            "ares/api/util/LruCache.class"
    );

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
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Variable criteria methods">

    //<editor-fold desc="Forbidden handling">

    /**
     * Checks if a Path is outside of the allowed paths whitelist.
     *
     * <p>Description: Returns true if allowedPaths not null or if the given actualPath does not match one of the allowedPatterns.
     *
     * @since 2.0.0
     * @author Markus
     * @param actualPath         the Path to test
     * @param allowedPaths whitelist of allowed actualPath strings
     * @param allowNonExistingPathsToBeConsidered whether non-existing paths should be considered
     * @return true if actualPath is forbidden; false otherwise
     */
    private static boolean checkIfPathIsForbidden(@Nullable Path actualPath, @Nullable String[] allowedPaths, boolean allowNonExistingPathsToBeConsidered) {
        if (actualPath == null) {
            return false;
        }
        if (allowedPaths == null || allowedPaths.length == 0) {
            return true;
        }
        Path candidate = actualPath.normalize().toAbsolutePath();
        boolean actualExists = Files.exists(candidate);

        if (actualExists) {
            try {
                candidate = candidate.toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ignored) {
                if (!allowNonExistingPathsToBeConsidered) {
                    return true;  // Cannot resolve path, treat as forbidden
                }
            }
        }

        boolean hasAllowedPrefix = false;
        for (String allowedPathsAsString : allowedPaths) {
            @Nullable Path allowedPath = variableToPath(allowedPathsAsString, allowNonExistingPathsToBeConsidered);
            if (allowedPath == null) {
                continue;
            }
            Path normalizedAllowedPath = allowedPath.normalize().toAbsolutePath();
            if (!allowNonExistingPathsToBeConsidered) {
                if (!Files.exists(normalizedAllowedPath)) {
                    continue;
                }
                try {
                    normalizedAllowedPath = normalizedAllowedPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
                } catch (IOException ignored) {
                    continue;
                }
            } else if (Files.exists(normalizedAllowedPath)) {
                try {
                    normalizedAllowedPath = normalizedAllowedPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
                } catch (IOException ignored) {
                    // use normalized path if real path resolution fails
                }
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
     * into an absolute normalized Path for security checks.
     *
     * @param variableValue the variable to transform into a Path
     * @param allowNonExistingPathsToBeConsidered whether to allow non-existing paths
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
                if (variableValue.equals("") || variableValue.equals("/")) {
                    throw new SecurityException(localize(
                            "security.instrumentation.invalid.path",
                            variableValue
                    ));
                }
                Path absolutePath = Path.of((String) variableValue).normalize().toAbsolutePath();
                if (allowNonExistingPathsToBeConsidered || Files.exists(absolutePath)) {
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
            Path observedPath = variableToPath(observedVariable, allowNonExistingPathsToBeConsidered);
            if (checkIfPathIsForbidden(observedPath, allowedPaths, allowNonExistingPathsToBeConsidered)) {
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
     * Derives the list of file-system actions that need to be validated for a given invocation.
     *
     * <p>Description: Inspects the intercepted method arguments for {@link StandardOpenOption}
     * instances and maps them onto the corresponding security actions (read, overwrite, create, delete).
     *
     * @param defaultAction action associated with the pointcut configuration (e.g., {@code overwrite})
     * @param parameters    intercepted method arguments that may contain {@link StandardOpenOption}s
     * @return ordered list of action/allow-non-existing pairs to validate
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
     * @param action action identifier to evaluate
     * @return {@code true} if paths may be missing, {@code false} otherwise
     */
    private static boolean shouldAllowNonExistingByDefault(@Nonnull String action) {
        return "create".equals(action);
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
     * Collects {@link StandardOpenOption}s from a single candidate object.
     *
     * @param candidate potential holder of {@link StandardOpenOption}s
     * @param target    accumulation set for discovered options
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
        @Nonnull final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
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
        @Nullable String systemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (systemMethodToCheck == null) {
            return;
        }
        //</editor-fold>
        @Nullable String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
        List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, parameters);
        for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
            String actionToCheck = actionCheck.getKey();
            boolean allowNonExistingPaths = Boolean.TRUE.equals(actionCheck.getValue());
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
                        actionToCheck,
                        illegallyInteractedThroughParameter,
                        fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                ));
            }
            //</editor-fold>
            //<editor-fold desc="Check attributes">
            @Nullable String illegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE), allowNonExistingPaths);
            if (illegallyInteractedThroughAttribute != null) {
                boolean isInternalAllowed = INTERNAL_PATH_SUFFIXES.stream()
                        .anyMatch(illegallyInteractedThroughAttribute::endsWith);

                if (!isInternalAllowed) {
                    throw new SecurityException(localize(
                            "security.advice.illegal.file.execution",
                            systemMethodToCheck,
                            actionToCheck,
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
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.bufferedReaderInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.scannerInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReaderInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.writerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.bufferedWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.printWriterInitMethods() ||
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
