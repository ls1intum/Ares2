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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.jar.JarFile;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
//</editor-fold>

@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJFileSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

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
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i].getName())) {
				return i;
			}
		}
		// Fail closed (mirrors the instrumentation backend): a not-found field must not
		// silently default to index 0, which would make the attribute ignore-filter keep
		// the wrong field and skip the command/path that actually needs checking.
		throw new SecurityException(localize("security.instrumentation.field.not.found", fieldName, clazz.getName()));
	}

	/**
	 * Map of methods with attribute index exceptions for file system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during file system checks. Entries cover
	 * {@link java.io.File} mutating methods (delete, deleteOnExit, createNewFile)
	 * where only the {@code path} field is relevant, and
	 * {@link ProcessBuilder#start()} / {@link ProcessBuilder#startPipeline()} where
	 * only the {@code command} field carries the executable path.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries(
			Map.entry("java.io.File.delete", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
			Map.entry("java.io.File.deleteOnExit", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
			Map.entry("java.io.File.createNewFile", IgnoreValues.allExcept(findFieldIndex(java.io.File.class, "path"))),
			// ProcessBuilder.start - only check command field, resolved by name
			Map.entry("java.lang.ProcessBuilder.start",
					IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))),
			Map.entry("java.lang.ProcessBuilder.startPipeline",
					IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))));

	/**
	 * Map of methods with parameter index exceptions for file system ignore logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during file system checks. For a method like
	 * Files.writeString, only the first parameter (the Path) should be checked, not
	 * the content.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries(
			// createTempFile is absent on purpose: its overloads put a directory or a
			// name prefix at the same index, so checkTempFileCreationSpecialCase decides.
			// Files.writeString(Path path, CharSequence csq, OpenOption...) - only check
			// path (index 0)
			Map.entry("java.nio.file.Files.writeString", IgnoreValues.allExcept(0)),
			// Files.write(Path path, byte[] bytes, OpenOption...) - only check path (index
			// 0)
			Map.entry("java.nio.file.Files.write", IgnoreValues.allExcept(0)),
			// Files.readString(Path path, Charset cs) - only check path (index 0)
			Map.entry("java.nio.file.Files.readString", IgnoreValues.allExcept(0)),
			// Files.newDirectoryStream(Path dir, String glob) / (Path,
			// DirectoryStream.Filter) - only check dir (index 0); the glob/filter is not
			// a path and must not be treated as one.
			Map.entry("java.nio.file.Files.newDirectoryStream", IgnoreValues.allExcept(0)),
			// Runtime.exec(String[]) - only check command (index 0), not flags like "-c"
			Map.entry("java.lang.Runtime.exec", IgnoreValues.allExcept(0)),
			// RandomAccessFile(File|String file, String mode) - only check the file
			// (index 0); the mode string ("r"/"rw"/...) is not a path.
			Map.entry("java.io.RandomAccessFile.<init>", IgnoreValues.allExcept(0)),
			// DataOutputStream.writeUTF/writeChars/writeBytes(String) - the argument is
			// payload written to the wrapped stream, never a path.
			Map.entry("java.io.DataOutputStream.writeUTF", IgnoreValues.ALL),
			Map.entry("java.io.DataOutputStream.writeChars", IgnoreValues.ALL),
			Map.entry("java.io.DataOutputStream.writeBytes", IgnoreValues.ALL),
			// The same three on a RandomAccessFile write to the file it has open.
			Map.entry("java.io.RandomAccessFile.writeUTF", IgnoreValues.ALL),
			Map.entry("java.io.RandomAccessFile.writeChars", IgnoreValues.ALL),
			Map.entry("java.io.RandomAccessFile.writeBytes", IgnoreValues.ALL),
			// PrintStream(OutputStream|File|String, [boolean], [String charset]) - keep
			// only index 0 (the sink); a charset name like "UTF-8" is not a path.
			Map.entry("java.io.PrintStream.<init>", IgnoreValues.allExcept(0)),
			// Scanner(File|Path, [String charset]) - only the file (index 0); a charset
			// name like "UTF-8" is not a path.
			Map.entry("java.util.Scanner.<init>", IgnoreValues.allExcept(0)),
			// StringReader(String) - the argument is the text it reads, never a path.
			Map.entry("java.io.StringReader.<init>", IgnoreValues.ALL),
			// FileSystemView.getChild(File dir, String name) and isParent(File dir, File
			// file) list dir; only dir is checked, the name is not a path and the file is
			// not listed.
			Map.entry("javax.swing.filechooser.FileSystemView.getChild", IgnoreValues.allExcept(0)),
			Map.entry("javax.swing.filechooser.FileSystemView.isParent", IgnoreValues.allExcept(0)));

	/**
	 * The JDK class whose subclasses share its argument rules. A call on a
	 * subclass of it, a student's own included, that has no rule of its own is
	 * read with the rule of the method it inherits, so a child name is never taken
	 * for a path.
	 */
	@Nonnull
	private static final String FILE_SYSTEM_VIEW = "javax.swing.filechooser.FileSystemView";

	/**
	 * Returns which arguments of a call are checked as paths. The exact rule wins;
	 * a receiver whose class extends FileSystemView falls back to that class's
	 * rule, found by comparing class names, so no desktop class is loaded.
	 *
	 * @param methodKey  the called method as declaring type and name
	 * @param methodName the called method's name
	 * @param instance   the receiver, or {@code null} for a static call
	 * @return the rule for the call's arguments
	 */
	@Nonnull
	private static IgnoreValues parameterIgnoreValues(@Nonnull String methodKey, @Nonnull String methodName,
			@Nullable Object instance) {
		@Nullable
		IgnoreValues exact = FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.get(methodKey);
		if (exact != null) {
			return exact;
		}
		if (instance == null || !extendsFileSystemView(instance.getClass())) {
			return IgnoreValues.NONE;
		}
		return FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(FILE_SYSTEM_VIEW + "." + methodName,
				IgnoreValues.NONE);
	}

	/**
	 * Tells whether a class is FileSystemView or extends it, by name, walking up
	 * the superclasses.
	 *
	 * @param type the receiver's class
	 * @return whether FileSystemView is among its classes
	 */
	private static boolean extendsFileSystemView(@Nonnull Class<?> type) {
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			if (FILE_SYSTEM_VIEW.equals(current.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Stand-in shown in the denial message for a {@code java.util.prefs.Preferences}
	 * operation, which has no path to report because the OS fixes the store's
	 * location.
	 */
	@Nonnull
	private static final String PREFERENCES_BACKING_STORE = "<java.util.prefs backing store>";

	/**
	 * Returns whether the intercepted call targets the Java Preferences API. Its
	 * backing store sits where the operating system puts it, so the student passes
	 * no path and the key and value arguments must not be checked as paths. No
	 * policy can scope that store to a directory, so only an allow-list of all
	 * paths ("*") permits it.
	 *
	 * @param declaringTypeName the fully qualified type the called method belongs to
	 * @return true for any {@code java.util.prefs} type
	 */
	private static boolean isPreferencesBackingStore(@Nullable String declaringTypeName) {
		return declaringTypeName != null && declaringTypeName.startsWith("java.util.prefs.");
	}

	/**
	 * Returns whether the allow-list authorises every path, the only rule under
	 * which the unscopable Preferences backing store may be touched.
	 *
	 * @param allowedPaths the configured allow-list for the action, may be null
	 * @return true if the list contains the {@code "*"} wildcard
	 */
	private static boolean allowsAllPaths(@Nullable String[] allowedPaths) {
		if (allowedPaths == null) {
			return false;
		}
		for (String allowedPath : allowedPaths) {
			if ("*".equals(allowedPath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Native-library file suffixes whose loads under {@code java.home} are JVM
	 * infrastructure rather than student file access.
	 */
	@Nonnull
	private static final List<String> NATIVE_LIBRARY_SUFFIXES = List.of(".dylib", ".jnilib", ".so", ".dll");

	/**
	 * The JDK class whose public static methods return the system properties as the
	 * JVM recorded them at start-up. Reading it needs the JVM argument
	 * {@code --add-exports java.base/jdk.internal.misc=ALL-UNNAMED}, and no agent.
	 */
	@Nonnull
	private static final String START_UP_PROPERTIES_CLASS = "jdk.internal.misc.VM";

	/**
	 * The JDK class that keeps the directory {@code File.createTempFile} writes to
	 * when no directory is given. On JDK 17 it copies {@code java.io.tmpdir} when
	 * first used, so Ares reads the copy rather than the property.
	 */
	@Nonnull
	private static final String JDK_FILE_TEMP_DIRECTORY_HOLDER = "java.io.File$TempDirectory";

	/**
	 * The system properties as this JVM started with them, which a later
	 * {@code System.setProperty} cannot change, or {@code null} if this JVM does not
	 * let Ares read them.
	 */
	@Nullable
	private static final Map<String, String> START_UP_PROPERTIES = readStartUpPropertiesOrNull();

	/**
	 * The Java installation as the JVM started with it, so changing
	 * {@code java.home} later cannot turn a student folder into a trusted one.
	 */
	@Nullable
	private static final String TRUSTED_JAVA_HOME = startUpProperty("java.home");

	/**
	 * The Maven repository as the JVM started with it, from {@code maven.repo.local}
	 * or else {@code ~/.m2/repository}. A value set after start-up is not trusted.
	 */
	@Nullable
	private static final String TRUSTED_MAVEN_REPOSITORY = resolveTrustedMavenRepository();

	/**
	 * The default temp directory as the JVM started with it, with links followed
	 * once when this aspect is first used and never again, so a link changed later
	 * cannot move it. {@code null} if it could not be determined.
	 */
	@Nullable
	private static final Path TRUSTED_DEFAULT_TEMP_DIRECTORY = resolveRealDirectory(startUpProperty("java.io.tmpdir"));

	// </editor-fold>

	// <editor-fold desc="File system methods">

	// <editor-fold desc="Variable criteria methods">

	// <editor-fold desc="Forbidden handling">

	/**
	 * Tells whether a path lies outside every allowed path. A missing or empty
	 * allow-list allows nothing, so every path is forbidden; a {@code *} entry
	 * allows everything. Links are resolved first, so a link cannot lead out of an
	 * allowed folder.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param target                              the path to test
	 * @param allowedPathsAsStrings               the allowed paths, possibly null
	 * @param allowNonExistingPathsToBeConsidered whether a path that does not exist
	 *                                            yet is still checked
	 * @return true if the path is forbidden
	 */
	private static boolean checkIfPathIsForbidden(@Nullable FileTarget target, @Nullable String[] allowedPathsAsStrings,
			boolean allowNonExistingPathsToBeConsidered) {
		if (target == null || target.path() == null) {
			return false;
		}
		Path actualPath = target.path();
		if (allowedPathsAsStrings == null || allowedPathsAsStrings.length == 0) {
			return true;
		}
		for (String allowedPath : allowedPathsAsStrings) {
			if ("*".equals(allowedPath)) {
				return false;
			}
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
			// For non-existing targets, resolve symlinks in the deepest existing ancestor
			// (usually the parent directory) and re-append the remaining segments, so a
			// symlinked parent cannot redirect a create/overwrite outside the allow-list.
			candidate = resolveExistingAncestorRealPath(actualPath.normalize().toAbsolutePath());
		}

		boolean hasAllowedPrefix = false;
		for (String allowedPathsAsString : allowedPathsAsStrings) {
			// Always resolve policy-entry paths even when they do not exist yet,
			// so that a rule such as "allow protected/file.txt" is not silently
			// skipped just because the file has not been created yet.
			@Nullable
			Path allowedPath = variableToPath(allowedPathsAsString, true);
			if (allowedPath == null) {
				continue;
			}

			if (pathMatches(candidate, allowedPath, allowNonExistingPathsToBeConsidered)) {
				hasAllowedPrefix = true;
				break;
			}
		}

		// A candidate that matches no allowed prefix is a violation regardless of
		// whether it currently exists. A non-existing wrong target must not silently
		// bypass the allowlist: doing so would leak file existence and let a
		// not-yet-created path slip through the rule (TOCTOU).
		return !hasAllowedPrefix;
	}

	/**
	 * Resolves symlinks in the deepest existing ancestor of a (possibly
	 * non-existing) absolute path and re-appends the remaining segments. This
	 * prevents a symlinked parent directory from redirecting a create/overwrite of a
	 * not-yet-existing leaf outside the allow-list. Falls back to the input path if
	 * no ancestor exists or the real path cannot be resolved.
	 *
	 * @param absolute the normalised absolute path whose leaf does not exist
	 * @return the canonicalised path with symlinks in the existing prefix resolved
	 */
	@Nonnull
	private static Path resolveExistingAncestorRealPath(@Nonnull Path absolute) {
		Path ancestor = absolute;
		while (ancestor != null && !Files.exists(ancestor, LinkOption.NOFOLLOW_LINKS)) {
			ancestor = ancestor.getParent();
		}
		if (ancestor == null) {
			return absolute;
		}
		try {
			Path realAncestor = ancestor.toRealPath();
			Path relative = ancestor.relativize(absolute);
			return realAncestor.resolve(relative);
		} catch (IOException ignored) {
			return absolute;
		}
	}

	/**
	 * Checks whether an actual (already resolved) path matches an allowed path.
	 * <p>
	 * Description: Resolves symlinks in the allowed path to its canonical form,
	 * then checks whether the candidate path starts with the resolved allowed path.
	 * Returns {@code false} if the allowed path does not exist and non-existing
	 * paths are not permitted.
	 *
	 * @param candidate                           the resolved candidate path
	 * @param allowedPath                         the allowed path to compare
	 *                                            against
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
			// Policy entries for non-existing paths must still be honoured. Resolve
			// symlinks in the allowed path's deepest existing ancestor too, so it matches a
			// candidate whose own ancestor symlinks were already resolved; otherwise a
			// policy entry like /tmp/link/new.txt would never match the canonicalised
			// candidate /real/dir/new.txt.
			normalizedAllowedPath = resolveExistingAncestorRealPath(allowedPath.normalize().toAbsolutePath());
		}

		return candidate.startsWith(normalizedAllowedPath);
	}
	// </editor-fold>

	// <editor-fold desc="Conversion handling">
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
	// allowNonExistingPathsToBeConsidered is retained for call-site signature symmetry
	// with variableToTarget; existence is now decided uniformly in checkIfPathIsForbidden.
	@SuppressWarnings("unused")
	@Nullable
	private static Path variableToPath(@Nullable Object variableValue, boolean allowNonExistingPathsToBeConsidered) {
		try {
			if (variableValue == null) {
				return null;
			} else if (variableValue instanceof Path) {
				// Path is an interface; only call into trusted JDK path impls.
				requireTrustedRuntimeType(variableValue);
				return ((Path) variableValue).normalize().toAbsolutePath();
			} else if (variableValue instanceof URI uri) {
				if (!"file".equalsIgnoreCase(uri.getScheme())) {
					return null;
				}
				// Return the resolved path even when it does not exist yet: the allow-list
				// decision (including the non-existing case) is made uniformly in
				// checkIfPathIsForbidden. Returning null here would silently allow a
				// non-existing (e.g. TOCTOU) target.
				return Path.of(uri).normalize().toAbsolutePath();
			} else if (variableValue instanceof URL url) {
				if (!"file".equalsIgnoreCase(url.getProtocol())) {
					return null;
				}
				return Path.of(url.toURI()).normalize().toAbsolutePath();
			} else if (variableValue instanceof String) {
				// Empty string is not a valid path
				if (variableValue.equals("")) {
					throw new SecurityException(localize("security.instrumentation.invalid.path", variableValue));
				}
				// "/" is the root directory and is a valid path - let it be processed normally
				return Path.of((String) variableValue).normalize().toAbsolutePath();
			} else if (variableValue instanceof File) {
				// File is not final; a student subclass could run code in toURI().
				requireTrustedRuntimeType(variableValue);
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
	 * @param allowNonExistingPathsToBeConsidered whether to allow non-existing
	 *                                            paths
	 * @return a {@link FileTarget}, or {@code null} if conversion fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nullable
	private static FileTarget variableToTarget(@Nullable Object variableValue,
			boolean allowNonExistingPathsToBeConsidered) {
		Path path = variableToPath(variableValue, allowNonExistingPathsToBeConsidered);
		return path == null ? null : new FileTarget(path);
	}
	// </editor-fold>

	// <editor-fold desc="Violation analysis">
	/**
	 * Analyses a variable to determine if it violates allowed paths.
	 * <p>
	 * Description: Recursively checks if the variable or its elements (if an array
	 * or List) are in violation of the allowed paths. Returns true if any element
	 * is forbidden.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariable the variable to analyse
	 * @param allowedPaths     the allowed paths; null or empty allows nothing
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
			FileTarget target = variableToTarget(observedVariable, allowNonExistingPathsToBeConsidered);
			return checkIfPathIsForbidden(target, allowedPaths, allowNonExistingPathsToBeConsidered);
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
	 * @author Markus Paulsen
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
			FileTarget target = variableToTarget(observedVariable, allowNonExistingPathsToBeConsidered);
			if (checkIfPathIsForbidden(target, allowedPaths, allowNonExistingPathsToBeConsidered)) {
				return target.toDisplayString();
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
	 * @author Markus Paulsen
	 * @param observedVariables array of values to validate
	 * @param allowedPaths      the allowed paths; null or empty allows nothing
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
	 * The {@code Files} methods that always open their file for writing. Given no
	 * options they create a missing file or empty an existing one; given options,
	 * the JDK adds {@code WRITE} to them.
	 */
	@Nonnull
	private static final Set<String> WRITE_OPENING_METHODS = Set.of("java.nio.file.Files.write",
			"java.nio.file.Files.writeString", "java.nio.file.Files.newOutputStream",
			"java.nio.file.Files.newBufferedWriter");

	/**
	 * The methods that open a channel for reading, writing or both, decided by the
	 * options alone. Without {@code WRITE} or {@code APPEND} the JDK opens the
	 * channel for reading.
	 */
	@Nonnull
	private static final Set<String> CHANNEL_OPENING_METHODS = Set.of("java.nio.file.Files.newByteChannel",
			"java.nio.channels.FileChannel.open", "java.nio.channels.AsynchronousFileChannel.open");

	/**
	 * The classes whose constructors open a named file for writing, creating it if
	 * it is missing. A constructor that is given a stream instead names no file, so
	 * nothing is checked for it.
	 */
	@Nonnull
	private static final Set<String> WRITE_OPENING_CONSTRUCTORS = Set.of("java.io.FileOutputStream",
			"java.io.FileWriter", "java.io.PrintWriter", "java.io.PrintStream", "java.util.Formatter");

	/**
	 * The action that stands for opening a file for writing. It is checked as
	 * {@code overwrite} when the file exists and as {@code create} when it does
	 * not, so the policy decides by what the call really does to the file.
	 */
	@Nonnull
	private static final String WRITE_ACTION = "write";

	/**
	 * Returns the action a {@code RandomAccessFile} constructor stands for: a read
	 * for mode {@code "r"}, an opening for writing for the other modes.
	 *
	 * @param parameters the constructor arguments, the mode second
	 * @return {@code read}, {@link #WRITE_ACTION}, or {@code null} without a known
	 *         mode
	 */
	@Nullable
	private static String getRandomAccessFileModeAction(@Nullable Object[] parameters) {
		if (parameters == null || parameters.length < 2 || !(parameters[1] instanceof String mode)) {
			return null;
		}
		if ("r".equals(mode)) {
			return "read";
		}
		return "rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode) ? WRITE_ACTION : null;
	}

	/**
	 * Returns the options a call really opens its file with, as the JDK sets them.
	 * The write methods add {@code WRITE}, or use create, truncate and write when
	 * given none; a channel opened without a write option is opened for reading.
	 *
	 * @param methodKey the called method as declaring type and name
	 * @param options   the options found among the arguments
	 * @return the options the file is opened with
	 */
	@Nonnull
	private static Set<StandardOpenOption> effectiveOpenOptions(@Nonnull String methodKey,
			@Nonnull Set<StandardOpenOption> options) {
		EnumSet<StandardOpenOption> effective = options.isEmpty() ? EnumSet.noneOf(StandardOpenOption.class)
				: EnumSet.copyOf(options);
		if (WRITE_OPENING_METHODS.contains(methodKey)) {
			if (effective.isEmpty()) {
				effective.add(StandardOpenOption.CREATE);
				effective.add(StandardOpenOption.TRUNCATE_EXISTING);
			}
			effective.add(StandardOpenOption.WRITE);
		} else if (!effective.contains(StandardOpenOption.WRITE) && !effective.contains(StandardOpenOption.APPEND)) {
			effective.add(StandardOpenOption.READ);
		}
		return effective;
	}

	/**
	 * Turns the options a file is opened with into the actions to check. Writing
	 * with {@code CREATE_NEW} is a create, with {@code CREATE} an opening for
	 * writing decided by whether the file exists, and without either an overwrite.
	 *
	 * @param options the options the file is opened with
	 * @return the actions to check, each with whether a missing path is checked
	 */
	@Nonnull
	private static List<Map.Entry<String, Boolean>> openOptionActions(@Nonnull Set<StandardOpenOption> options) {
		List<Map.Entry<String, Boolean>> actions = new ArrayList<>();
		if (options.contains(StandardOpenOption.DELETE_ON_CLOSE)) {
			actions.add(Map.entry("delete", false));
		}
		if (options.contains(StandardOpenOption.READ)) {
			actions.add(Map.entry("read", false));
		}
		if (options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND)) {
			if (options.contains(StandardOpenOption.CREATE_NEW)) {
				actions.add(Map.entry("create", true));
			} else if (options.contains(StandardOpenOption.CREATE)) {
				actions.add(Map.entry(WRITE_ACTION, true));
			} else {
				actions.add(Map.entry("overwrite", false));
			}
		}
		return actions;
	}

	/**
	 * Decides how an opening for writing is checked: as an overwrite when the first
	 * checked argument that names a path names an existing file, otherwise as a
	 * create.
	 *
	 * @param parameters      the call arguments
	 * @param ignoreVariables which arguments are not paths
	 * @return {@code overwrite} or {@code create}
	 */
	@Nonnull
	private static String resolveWriteAction(@Nullable Object[] parameters, @Nonnull IgnoreValues ignoreVariables) {
		if (parameters == null) {
			return "create";
		}
		for (Object parameter : filterVariables(parameters, ignoreVariables)) {
			FileTarget target = variableToTarget(parameter, true);
			if (target != null) {
				return Files.exists(target.path()) ? "overwrite" : "create";
			}
		}
		return "create";
	}

	/**
	 * Tells whether a call writes through a file the code already opened: a file
	 * channel or a {@code RandomAccessFile}. Opening it for writing was checked, so
	 * such a write is allowed where the policy lets the file be overwritten or
	 * created.
	 *
	 * @param action   the action being checked
	 * @param instance the receiver of the call
	 * @return whether the call writes through an open file
	 */
	private static boolean isWriteThroughOpenFile(@Nonnull String action, @Nullable Object instance) {
		return "overwrite".equals(action) && (instance instanceof FileChannel
				|| instance instanceof java.nio.channels.AsynchronousFileChannel
				|| instance instanceof java.io.RandomAccessFile);
	}

	/**
	 * Returns the paths the policy lets be overwritten together with those it lets
	 * be created, for a write through a file that is already open.
	 *
	 * @return both lists in one, or {@code null} if neither is set
	 */
	@Nullable
	private static String[] pathsAllowedToBeOverwrittenOrCreated() {
		@Nullable
		String[] overwrite = getValueFromSettings("pathsAllowedToBeOverwritten");
		@Nullable
		String[] create = getValueFromSettings("pathsAllowedToBeCreated");
		if (overwrite == null || create == null) {
			return overwrite == null ? create : overwrite;
		}
		String[] both = new String[overwrite.length + create.length];
		System.arraycopy(overwrite, 0, both, 0, overwrite.length);
		System.arraycopy(create, 0, both, overwrite.length, create.length);
		return both;
	}

	/**
	 * Finds the file a {@code getResourceAsStream} call would read, so that file is
	 * checked instead of the resource name. A resource in a JAR stands for that
	 * JAR, one in the Java runtime image needs no check, and a missing resource
	 * reads nothing.
	 *
	 * @param instance   the {@code Class} or JDK class loader the call is made on
	 * @param parameters the call arguments, the resource name first
	 * @return the values to check as paths, or {@code null} to check the call as
	 *         it is
	 */
	@Nullable
	private static Object[] resourceReadTargets(@Nullable Object instance, @Nullable Object[] parameters) {
		if (parameters == null || parameters.length != 1 || !(parameters[0] instanceof String name)) {
			return null;
		}
		URL resource;
		if (instance instanceof Class<?> type) {
			resource = type.getResource(name);
		} else if (instance instanceof ClassLoader loader && isJdkClassLoader(loader)) {
			resource = loader.getResource(name);
		} else {
			return null;
		}
		if (resource == null || "jrt".equals(resource.getProtocol())) {
			return new Object[0];
		}
		try {
			if ("file".equals(resource.getProtocol())) {
				return new Object[] { Path.of(resource.toURI()) };
			}
			if ("jar".equals(resource.getProtocol())) {
				String location = resource.getPath();
				int separator = location.indexOf("!/");
				return new Object[] { Path.of(new URI(separator < 0 ? location : location.substring(0, separator))) };
			}
		} catch (URISyntaxException | IllegalArgumentException | java.nio.file.FileSystemNotFoundException e) {
			return null;
		}
		return null;
	}

	/**
	 * Tells whether a class loader belongs to the JDK, so asking it where a
	 * resource lies runs no supervised code.
	 *
	 * @param loader the class loader
	 * @return whether its class was loaded by the bootstrap or platform loader
	 */
	private static boolean isJdkClassLoader(@Nonnull ClassLoader loader) {
		ClassLoader definingLoader = loader.getClass().getClassLoader();
		return definingLoader == null || definingLoader == ClassLoader.getPlatformClassLoader();
	}

	// <editor-fold desc="Copy/transfer per-parameter role special case (I-114)">

	/**
	 * Special-cases {@code java.nio.file.Files.copy} and
	 * {@code FileChannel.transferTo}/{@code transferFrom}, which the generic
	 * {@link #deriveActionChecks} path would otherwise validate as ONE uniform
	 * action applied to the whole parameters array. That would let a policy
	 * granting only {@code pathsAllowedToBeOverwritten} authorise reading the
	 * SOURCE side too, since {@code copy}'s source path was checked against the
	 * OVERWRITE allow-list instead of READ (I-114/TD-063) — a real
	 * privilege-escalation path, not just a misclassification.
	 * <p>
	 * Each of these methods is registered in both {@code filesReadMethods()} (or
	 * {@code fileChannelReadMethods()}) and {@code filesWriteMethods()} (or
	 * {@code fileChannelWriteMethods()}), so this advice fires once per action;
	 * this method independently checks only the parameter/receiver relevant to
	 * the currently-firing action, and reports {@code true} once it has fully
	 * handled the call (the caller must not additionally run the generic path).
	 * <p>
	 * A destination that is itself a channel (not a {@link Path}/{@link File}) is
	 * not resolvable to a checkable path with the existing machinery (the same
	 * limitation the generic "receiver instance" check already has for any
	 * channel-typed value) — that side is intentionally left unchecked here,
	 * since a channel obtained via {@code FileChannel.open(path, ...)} was
	 * already subject to a create/overwrite check at the point it was opened.
	 *
	 * @return {@code true} if the call was one of the special-cased methods and
	 *         has been fully handled (the generic path must be skipped)
	 */
	private boolean checkCopyOrTransferSpecialCase(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object[] parameters, @Nullable Object instance,
			@Nonnull String systemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature, @Nonnull JoinPoint thisJoinPoint) {
		if ("java.nio.file.Files".equals(declaringTypeName) && "copy".equals(methodName)) {
			if ("read".equals(action)) {
				// source (index 0). The other overloads' index-0 argument
				// (InputStream) is not Path-shaped and resolves to no target, so this
				// is a safe no-op for them.
				checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"),
						isolateParameter(parameters, 0), false, systemMethodToCheck, studentCalledMethod,
						fullMethodSignature, thisJoinPoint);
			} else if ("overwrite".equals(action)) {
				// destination (index 1): an existing file is overwritten only with
				// REPLACE_EXISTING; any other destination is created.
				Object[] destination = isolateParameter(parameters, 1);
				boolean overwritesExisting = hasReplaceExisting(parameters)
						&& "overwrite".equals(resolveWriteAction(destination, IgnoreValues.NONE));
				String resolvedAction = overwritesExisting ? "overwrite" : "create";
				String settingKey = overwritesExisting ? "pathsAllowedToBeOverwritten" : "pathsAllowedToBeCreated";
				checkSinglePathRole(resolvedAction, getValueFromSettings(settingKey), destination, true,
						systemMethodToCheck, studentCalledMethod, fullMethodSignature, thisJoinPoint);
			} else {
				throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
			}
			return true;
		}
		if ("java.nio.channels.FileChannel".equals(declaringTypeName) && "transferTo".equals(methodName)) {
			if ("read".equals(action)) {
				// receiver (the channel being read FROM); the target argument (index 2) is
				// itself a channel and is not checked here — see class-level Javadoc above.
				checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"),
						instance == null ? new Object[0] : new Object[] { instance }, false, systemMethodToCheck,
						studentCalledMethod, fullMethodSignature, thisJoinPoint);
			} else {
				throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
			}
			return true;
		}
		if ("java.nio.channels.FileChannel".equals(declaringTypeName) && "transferFrom".equals(methodName)) {
			if ("overwrite".equals(action)) {
				// receiver (the channel being written TO); the src argument (index 0) is
				// itself a channel and is not checked here — see class-level Javadoc above.
				checkSinglePathRole("overwrite", pathsAllowedToBeOverwrittenOrCreated(),
						instance == null ? new Object[0] : new Object[] { instance }, false, systemMethodToCheck,
						studentCalledMethod, fullMethodSignature, thisJoinPoint);
			} else {
				throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks one side of a student's file copy or transfer against the given
	 * allow-list and throws a {@code SecurityException} on a violation. No
	 * exemption applies: the student names both paths, so neither is Ares or the
	 * JDK at work.
	 */
	private void checkSinglePathRole(@Nonnull String actionLabel, @Nullable String[] allowedPaths,
			@Nonnull Object[] candidates, boolean allowNonExistingPaths, @Nonnull String systemMethodToCheck,
			@Nullable String studentCalledMethod, @Nonnull String fullMethodSignature,
			@Nonnull JoinPoint thisJoinPoint) {
		if (candidates.length == 0) {
			return;
		}
		boolean noAllowRuleConfigured = allowedPaths == null || allowedPaths.length == 0;
		@Nullable
		String violation = checkIfVariableCriteriaIsViolated(candidates, allowedPaths, IgnoreValues.NONE,
				allowNonExistingPaths);
		if (violation == null) {
			return;
		}
		throw new SecurityException(localize("security.advice.illegal.file.execution", systemMethodToCheck,
				localizeAction(actionLabel), violation, describeDeniedCall(thisJoinPoint, fullMethodSignature)
						+ describeCaller(studentCalledMethod),
						buildDenialReason("file", actionLabel, noAllowRuleConfigured)));
	}

	/**
	 * Returns {@code true} if {@code parameters} contains
	 * {@link StandardCopyOption#REPLACE_EXISTING}, scanning into varargs arrays
	 * the same way {@link #collectStandardOpenOptions} does for
	 * {@link StandardOpenOption}.
	 */
	private static boolean hasReplaceExisting(@Nullable Object[] parameters) {
		if (parameters == null) {
			return false;
		}
		for (Object parameter : parameters) {
			if (containsReplaceExisting(parameter)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsReplaceExisting(@Nullable Object candidate) {
		if (candidate == null) {
			return false;
		}
		if (candidate == StandardCopyOption.REPLACE_EXISTING) {
			return true;
		}
		if (candidate.getClass().isArray()) {
			int length = Array.getLength(candidate);
			for (int i = 0; i < length; i++) {
				if (containsReplaceExisting(Array.get(candidate, i))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Isolates a single parameter into its own one-element array so
	 * {@link #checkIfVariableCriteriaIsViolated} validates only that argument, or
	 * an empty array if {@code index} is out of bounds (e.g. an overload that
	 * doesn't have a parameter at that position).
	 */
	@Nonnull
	private static Object[] isolateParameter(@Nullable Object[] parameters, int index) {
		return (parameters != null && parameters.length > index) ? new Object[] { parameters[index] } : new Object[0];
	}

	/**
	 * Decides a {@code createTempFile} call on its own, since its overloads put a
	 * directory or a name prefix at the same position. The directory the JDK will
	 * really write to, named or default, needs a {@code pathsAllowedToBeCreated}
	 * entry or must be the default temp directory the JVM started with.
	 *
	 * @return {@code true} if the call was {@code createTempFile} and is allowed,
	 *         {@code false} for any other call
	 * @throws SecurityException if the call is not allowed or fits no overload
	 */
	private static boolean checkTempFileCreationSpecialCase(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object[] parameters, @Nonnull String systemMethodToCheck,
			@Nullable String studentCalledMethod, @Nonnull String fullMethodSignature,
			@Nonnull JoinPoint thisJoinPoint) {
		if (!isTempFileCreation(declaringTypeName, methodName)) {
			return false;
		}
		if (!"create".equals(action)) {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
		}
		@Nullable
		Object explicitDirectory = resolveExplicitTempDirectory(declaringTypeName, parameters, systemMethodToCheck,
				fullMethodSignature);
		@Nonnull
		Object effectiveDirectory = explicitDirectory != null ? explicitDirectory
				: resolveDefaultTempDirectory(declaringTypeName);
		requireDirectoryAllowedForTempFiles(effectiveDirectory, systemMethodToCheck, studentCalledMethod,
				fullMethodSignature, thisJoinPoint);
		return true;
	}

	/**
	 * Tells whether a call is {@code Files.createTempFile} or
	 * {@code File.createTempFile}.
	 *
	 * @param declaringTypeName the class that declares the called method
	 * @param methodName        the called method
	 * @return {@code true} for either of the two methods
	 */
	private static boolean isTempFileCreation(@Nonnull String declaringTypeName, @Nonnull String methodName) {
		return "createTempFile".equals(methodName)
				&& ("java.nio.file.Files".equals(declaringTypeName) || "java.io.File".equals(declaringTypeName));
	}

	/**
	 * Returns the directory a {@code createTempFile} call names, or {@code null}
	 * when it names none. {@code Files.createTempFile} takes three arguments, or
	 * four with the directory first; {@code File.createTempFile} takes two, or
	 * three with the directory last, where {@code null} means the default one.
	 *
	 * @return the directory argument, or {@code null} for the default directory
	 * @throws SecurityException if the arguments fit neither overload
	 */
	@Nullable
	private static Object resolveExplicitTempDirectory(@Nonnull String declaringTypeName,
			@Nullable Object[] parameters, @Nonnull String systemMethodToCheck, @Nonnull String fullMethodSignature) {
		boolean isFilesMethod = "java.nio.file.Files".equals(declaringTypeName);
		int count = parameters == null ? -1 : parameters.length;
		if ((isFilesMethod && count == 3) || (!isFilesMethod && count == 2)) {
			return null;
		}
		if (isFilesMethod && count == 4 && parameters[0] instanceof Path) {
			return parameters[0];
		}
		if (!isFilesMethod && count == 3 && (parameters[2] == null || parameters[2] instanceof File)) {
			return parameters[2];
		}
		throw new SecurityException(localize("security.advice.file.system.malformed.temp.file.creation",
				systemMethodToCheck, fullMethodSignature));
	}

	/**
	 * Returns the directory a {@code createTempFile} call without one writes to.
	 * {@code Files} uses the {@code java.io.tmpdir} the JVM started with, as a path
	 * that is not followed until the file is created; {@code File} uses the JDK's
	 * own copy, which on JDK 17 is taken from the property when first used.
	 *
	 * @param declaringTypeName the class that declares the called method
	 * @return the directory the JDK will write to
	 * @throws SecurityException if Ares cannot find out which directory that is
	 */
	@Nonnull
	private static Object resolveDefaultTempDirectory(@Nonnull String declaringTypeName) {
		if ("java.io.File".equals(declaringTypeName)) {
			return readJdkFileTempDirectory();
		}
		requireStartUpProperties();
		@Nullable
		String startUpDirectory = startUpProperty("java.io.tmpdir");
		if (startUpDirectory == null) {
			throw new SecurityException(localize("security.advice.startup.properties.unavailable"));
		}
		return Path.of(startUpDirectory);
	}

	/**
	 * Prepares the JDK class behind {@code File.createTempFile}'s default directory
	 * before this advice's guard is entered. Preparing it creates a
	 * {@code SecureRandom}, which can run student code such as a provider, and that
	 * code must stay checked. A failure is left to the later read, which refuses.
	 *
	 * @param thisJoinPoint the intercepted call
	 */
	private static void initialiseJdkFileTempDirectoryBeforeTheGuard(@Nonnull JoinPoint thisJoinPoint) {
		if (!"java.io.File".equals(thisJoinPoint.getSignature().getDeclaringTypeName())
				|| !"createTempFile".equals(thisJoinPoint.getSignature().getName())) {
			return;
		}
		try {
			Class.forName(JDK_FILE_TEMP_DIRECTORY_HOLDER, true, null);
		} catch (ClassNotFoundException | LinkageError unprepared) {
			return;
		}
	}

	/**
	 * Reads the directory {@code File.createTempFile} without a directory writes
	 * to, from the JDK class that keeps it, which was prepared before the guard.
	 * That class must hold exactly one static {@code File}. Needs the JVM argument
	 * {@code --add-opens java.base/java.io=ALL-UNNAMED}.
	 *
	 * @return the directory the JDK will write to
	 * @throws SecurityException if the directory cannot be read
	 */
	@Nonnull
	private static File readJdkFileTempDirectory() {
		@Nullable
		Object directory;
		try {
			@Nullable
			java.lang.reflect.Field holderField = findSingleStaticFileField(
					Class.forName(JDK_FILE_TEMP_DIRECTORY_HOLDER, false, null));
			if (holderField == null) {
				throw new SecurityException(localize("security.advice.temp.directory.holder.unreadable"));
			}
			holderField.setAccessible(true);
			directory = holderField.get(null);
		} catch (ReflectiveOperationException | InaccessibleObjectException | LinkageError unreadable) {
			throw new SecurityException(localize("security.advice.temp.directory.holder.unreadable"), unreadable);
		}
		if (directory instanceof File file) {
			return file;
		}
		throw new SecurityException(localize("security.advice.temp.directory.holder.unreadable"));
	}

	/**
	 * Returns the only static field of type {@code File} a class declares, or
	 * {@code null} if it declares none or more than one.
	 *
	 * @param holder the class to search
	 * @return the field, or {@code null}
	 */
	@Nullable
	private static java.lang.reflect.Field findSingleStaticFileField(@Nonnull Class<?> holder) {
		@Nullable
		java.lang.reflect.Field found = null;
		for (java.lang.reflect.Field field : holder.getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == File.class) {
				if (found != null) {
					return null;
				}
				found = field;
			}
		}
		return found;
	}

	/**
	 * Allows a {@code createTempFile} directory that {@code pathsAllowedToBeCreated}
	 * covers, or that is exactly the default temp directory the JVM started with.
	 * Ares's own internal file names grant nothing here, since a student can name a
	 * directory after them.
	 *
	 * @throws SecurityException if neither holds
	 */
	private static void requireDirectoryAllowedForTempFiles(@Nonnull Object explicitDirectory,
			@Nonnull String systemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature, @Nonnull JoinPoint thisJoinPoint) {
		@Nullable
		String[] allowedPaths = getValueFromSettings("pathsAllowedToBeCreated");
		@Nullable
		String violation = checkIfVariableCriteriaIsViolated(new Object[] { explicitDirectory }, allowedPaths,
				IgnoreValues.NONE, true);
		if (violation == null || isTrustedDefaultTempDirectory(explicitDirectory)) {
			return;
		}
		boolean noAllowRuleConfigured = allowedPaths == null || allowedPaths.length == 0;
		throw new SecurityException(localize("security.advice.illegal.file.execution", systemMethodToCheck,
				localizeAction("create"), violation, describeDeniedCall(thisJoinPoint, fullMethodSignature)
						+ describeCaller(studentCalledMethod),
						buildDenialReason("file", "create", noAllowRuleConfigured)));
	}

	/**
	 * Tells whether a directory, with links followed now, is exactly the default
	 * temp directory the JVM started with; a subdirectory or a link leading
	 * elsewhere does not count, and a directory that does not resolve counts as
	 * different. Runs inside the advice's guard.
	 *
	 * @param explicitDirectory the directory, a {@code Path} or a {@code File}
	 * @return {@code true} only for the start-up default temp directory itself
	 * @throws SecurityException if the JVM's start-up values cannot be read
	 */
	private static boolean isTrustedDefaultTempDirectory(@Nonnull Object explicitDirectory) {
		requireStartUpProperties();
		if (TRUSTED_DEFAULT_TEMP_DIRECTORY == null) {
			return false;
		}
		try {
			@Nullable
			Path candidate = variableToPath(explicitDirectory, true);
			return candidate != null && candidate.toRealPath().equals(TRUSTED_DEFAULT_TEMP_DIRECTORY);
		} catch (IOException | InvalidPathException unresolvable) {
			return false;
		}
	}

	// </editor-fold>

	/**
	 * Derives the file actions to check for one call. Opening a file for writing
	 * yields {@link #WRITE_ACTION}. Options decide where a method takes them, the
	 * mode for {@code RandomAccessFile}, and the pointcut's action otherwise.
	 *
	 * @param defaultAction     the pointcut's action
	 * @param declaringTypeName the called method's class
	 * @param methodName        the called method
	 * @param parameters        the call arguments
	 * @return each action with whether a missing path is checked
	 */
	private static List<Map.Entry<String, Boolean>> deriveActionChecks(@Nonnull String defaultAction,
			@Nonnull String declaringTypeName, @Nonnull String methodName, @Nullable Object[] parameters) {
		FileChannel.MapMode mapMode = extractMapMode(parameters);
		if (mapMode != null) {
			return List.of(Map.entry(mapMode == FileChannel.MapMode.READ_ONLY ? "read" : "overwrite", false));
		}
		String methodKey = declaringTypeName + "." + methodName;
		Set<StandardOpenOption> options = extractStandardOpenOptions(parameters);
		if (WRITE_OPENING_METHODS.contains(methodKey) || CHANNEL_OPENING_METHODS.contains(methodKey)) {
			return openOptionActions(effectiveOpenOptions(methodKey, options));
		}
		List<Map.Entry<String, Boolean>> optionActions = openOptionActions(options);
		if (!optionActions.isEmpty()) {
			return optionActions;
		}
		if ("java.io.RandomAccessFile".equals(declaringTypeName)) {
			String modeAction = getRandomAccessFileModeAction(parameters);
			if (modeAction != null) {
				return List.of(Map.entry(modeAction, !"read".equals(modeAction)));
			}
		} else if ("<init>".equals(methodName) && WRITE_OPENING_CONSTRUCTORS.contains(declaringTypeName)) {
			return List.of(Map.entry(WRITE_ACTION, true));
		}
		return List.of(Map.entry(defaultAction, shouldAllowNonExistingByDefault(defaultAction)));
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
		return "create".equals(action) || "delete".equals(action);
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
	 * Extracts a {@link FileChannel.MapMode} from method parameters if present.
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
	 * Tells whether a read is of a {@code .jar} inside the Maven repository or the
	 * Java installation the JVM started with, which is allowed whoever reads it. The
	 * repository is matched as a directory, so a student's own
	 * {@code .m2/repository} folder elsewhere does not count.
	 *
	 * @param path the already-resolved path under inspection
	 * @return true for such a {@code .jar} read
	 * @throws SecurityException if the JVM's start-up values cannot be read
	 */
	private static boolean isSystemJarReadExempt(@Nonnull String path) {
		if (!path.endsWith(".jar")) {
			return false;
		}
		requireStartUpProperties();
		return isPathWithin(path, TRUSTED_MAVEN_REPOSITORY) || isPathWithin(path, TRUSTED_JAVA_HOME);
	}

	/**
	 * Resolves the Maven local repository root from the start-up value of
	 * {@code maven.repo.local}, with the conventional {@code ~/.m2/repository}
	 * fallback. Returns {@code null} when neither can be determined.
	 *
	 * @return the Maven repository root, or {@code null}
	 */
	@Nullable
	private static String resolveTrustedMavenRepository() {
		String mavenRepository = startUpProperty("maven.repo.local");
		if (mavenRepository == null || mavenRepository.isEmpty()) {
			String userHome = startUpProperty("user.home");
			if (userHome == null) {
				return null;
			}
			mavenRepository = userHome + File.separator + ".m2" + File.separator + "repository";
		}
		return mavenRepository;
	}

	/**
	 * Reads the system properties as this JVM started with them, or returns
	 * {@code null} if this JVM does not let Ares read them.
	 *
	 * @return the start-up properties, or {@code null}
	 */
	@Nullable
	private static Map<String, String> readStartUpPropertiesOrNull() {
		try {
			return readStartUpProperties();
		} catch (ReflectiveOperationException | RuntimeException | LinkageError unreadable) {
			return null;
		}
	}

	/**
	 * Copies the system properties as this JVM started with them from the JDK's own
	 * record, which {@code System.setProperty} does not change. The JDK hands out a
	 * view of a mutable map, so only a copy stays fixed.
	 *
	 * @return a copy of the start-up properties
	 * @throws ReflectiveOperationException if the JDK does not let Ares read them
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	private static Map<String, String> readStartUpProperties() throws ReflectiveOperationException {
		return Map.copyOf((Map<String, String>) Map.class.cast(
				Class.forName(START_UP_PROPERTIES_CLASS).getMethod("getSavedProperties").invoke(null)));
	}

	/**
	 * Returns one system property as this JVM started with it, or {@code null} if
	 * it was not set or the start-up properties cannot be read.
	 *
	 * @param key the property name
	 * @return the start-up value, or {@code null}
	 */
	@Nullable
	private static String startUpProperty(@Nonnull String key) {
		return START_UP_PROPERTIES == null ? null : START_UP_PROPERTIES.get(key);
	}

	/**
	 * Refuses the current check when it depends on the JVM's start-up values and
	 * Ares cannot read them, naming the JVM argument that would let it, with the
	 * JDK's own reason attached.
	 *
	 * @throws SecurityException if the start-up properties cannot be read
	 */
	private static void requireStartUpProperties() {
		if (START_UP_PROPERTIES != null) {
			return;
		}
		try {
			readStartUpProperties();
		} catch (ReflectiveOperationException | RuntimeException | LinkageError unreadable) {
			throw new SecurityException(localize("security.advice.startup.properties.unavailable"), unreadable);
		}
		throw new SecurityException(localize("security.advice.startup.properties.unavailable"));
	}

	/**
	 * Resolves a directory to its real location with links followed, or returns
	 * {@code null} if there is no directory name or it does not resolve.
	 *
	 * @param directory the directory name, or {@code null}
	 * @return the real directory, or {@code null}
	 */
	@Nullable
	private static Path resolveRealDirectory(@Nullable String directory) {
		if (directory == null) {
			return null;
		}
		try {
			return Path.of(directory).toRealPath();
		} catch (IOException | InvalidPathException | SecurityException unresolvable) {
			return null;
		}
	}

	/**
	 * Returns {@code true} when {@code path} resolves to a location at or below
	 * {@code directory}, using path-element boundaries rather than a raw string
	 * prefix so that a sibling such as {@code /opt/jdk-17-evil} is not treated as
	 * being under {@code /opt/jdk-17}. Comparison is purely lexical (no filesystem
	 * access) to avoid re-entering the interceptors.
	 *
	 * @param path      the already-resolved path string under inspection
	 * @param directory the trusted directory root, or {@code null}
	 * @return true if {@code path} is within {@code directory}
	 */
	private static boolean isPathWithin(@Nonnull String path, @Nullable String directory) {
		if (directory == null || directory.isBlank()) {
			return false;
		}
		try {
			Path base = Path.of(directory).toAbsolutePath().normalize();
			Path candidate = Path.of(path).toAbsolutePath().normalize();
			return candidate.startsWith(base);
		} catch (InvalidPathException ignored) {
			return false;
		}
	}

	/**
	 * Tells whether an access is the JVM's own: a read under the Java installation
	 * the JVM started with, or a load of a native library there, by its
	 * path. Other actions are never exempt.
	 *
	 * @param action the file-system action under inspection
	 * @param path   the already-resolved path under inspection
	 * @return true if the access is exempt
	 * @throws SecurityException if the JVM's start-up values cannot be read
	 */
	private static boolean isExemptSystemFileAccess(@Nonnull String action, @Nonnull String path) {
		if (!"read".equals(action) && !"execute".equals(action)) {
			return false;
		}
		requireStartUpProperties();
		if (!isPathWithin(path, TRUSTED_JAVA_HOME)) {
			return false;
		}
		if ("read".equals(action)) {
			return true;
		}
		if ("execute".equals(action)) {
			for (String suffix : NATIVE_LIBRARY_SUFFIXES) {
				if (path.endsWith(suffix)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Validates a file system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (read, overwrite, create,
	 * execute, delete) complies with allowed paths and call stack criteria. Throws
	 * SecurityException if a policy violation is detected.
	 *
	 * @param action        the file system action being performed
	 * @param thisJoinPoint the join point of the method being executed
	 * @throws SecurityException if unauthorised access is detected
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public void checkFileSystemInteraction(@Nonnull String action, @Nonnull JoinPoint thisJoinPoint) {
		// Re-entrancy guard: under load-time weaving the advice body's own file-system
		// and stack-walk work is itself woven and re-enters this advice on the same
		// thread, causing unbounded recursion (and ClassCircularityError during class
		// loading). Skip nested invocations (trusted Ares internals); enforce only the
		// outermost one.
		if (isAdviceInProgress()) {
			return;
		}
		initialiseJdkFileTempDirectoryBeforeTheGuard(thisJoinPoint);
		if (!enterAdvice()) {
			return;
		}
		try {
			checkFileSystemInteractionImpl(action, thisJoinPoint);
		} finally {
			exitAdvice();
		}
	}

	/**
	 * Accepts archives loaded by a trusted JDK loader. A student subclass could
	 * override {@code size()} and run code while the advice guard is active.
	 *
	 * @param instance          the receiver
	 * @param declaringTypeName the intercepted method's owner
	 * @return whether the receiver fits the archive method's owner
	 * @throws SecurityException if a student loader owns the receiver
	 */
	private static boolean isJdkArchiveReceiver(@Nullable Object instance, @Nonnull String declaringTypeName) {
		if (!(instance instanceof ZipFile)
				|| !("java.util.jar.JarFile".equals(declaringTypeName) && instance instanceof JarFile
						|| "java.util.zip.ZipFile".equals(declaringTypeName))) {
			return false;
		}
		requireTrustedRuntimeType(instance);
		return true;
	}

	/**
	 * Tells whether the call reads an entry of a {@code JarFile} or
	 * {@code ZipFile} that is already open. Opening the archive is still checked
	 * against the policy, and the entry call names no file of its own, so the
	 * entry read is allowed, exactly as on the instrumentation backend.
	 *
	 * @param action            the file-system action being checked
	 * @param declaringTypeName the class of the intercepted method
	 * @param thisJoinPoint     the intercepted call
	 * @return {@code true} for a read of an entry, never for opening the archive
	 */
	private static boolean isEntryReadOnOpenArchive(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull JoinPoint thisJoinPoint) {
		if (!"read".equals(action) || !(thisJoinPoint.getSignature() instanceof MethodSignature signature)
				|| !(thisJoinPoint.getTarget() instanceof ZipFile archive)
				|| !isJdkArchiveReceiver(archive, declaringTypeName)) {
			return false;
		}
		Object[] arguments = thisJoinPoint.getArgs();
		Class<?>[] parameterTypes = signature.getParameterTypes();
		boolean entryCall = "entries".equals(signature.getName())
				&& parameterTypes != null && parameterTypes.length == 0 && arguments != null && arguments.length == 0;
		boolean streamCall = "getInputStream".equals(signature.getName())
				&& parameterTypes != null && parameterTypes.length == 1 && ZipEntry.class.equals(parameterTypes[0]) && arguments != null
				&& arguments.length == 1 && arguments[0] instanceof ZipEntry;
		if (!entryCall && !streamCall) {
			return false;
		}
		try {
			archive.size();
			return true;
		} catch (IllegalStateException closed) {
			return false;
		}
	}

	/**
	 * Performs the file check behind {@link #checkFileSystemInteraction}, which calls it
	 * only for the outermost advice on a thread. Throws a {@link SecurityException}
	 * naming the blocked file operation when the policy forbids it.
	 *
	 * @param action the raw action name, which drives every decision
	 * @param thisJoinPoint the join point of the intercepted call
	 */
	private void checkFileSystemInteractionImpl(@Nonnull String action, @Nonnull JoinPoint thisJoinPoint) {
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (aopMode == null || !aopMode.equals("ASPECTJ")) {
			return;
		}
		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		if (isProjectSourcesFinderInProgress()) {
			return;
		}
		@Nullable
		final String[] allowedClasses = getValueFromSettings("allowedListedClasses");
		@Nonnull
		Object[] parameters = thisJoinPoint.getArgs();
		@Nullable
		Object instance = thisJoinPoint.getTarget();
		@Nonnull
		String methodName;
		@Nonnull
		String declaringTypeName;
		@Nonnull
		String fullMethodSignature;
		try {
			methodName = thisJoinPoint.getSignature().getName();
			declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
			fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
		} catch (RuntimeException malformed) {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", "malformed method"), malformed);
		}
		if (methodName == null || methodName.isBlank() || declaringTypeName == null || declaringTypeName.isBlank()) {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", "malformed method"));
		}
		@Nonnull
		Object[] attributes = new Object[0];
		if (instance != null) {
			try {
				@Nonnull
				java.lang.reflect.Field[] fields = instance.getClass().getDeclaredFields();
				attributes = new Object[fields.length];
				for (int i = 0; i < fields.length; i++) {
					if (java.lang.reflect.Modifier.isStatic(fields[i].getModifiers())) {
						continue;
					}
					try {
						fields[i].setAccessible(true);
						attributes[i] = fields[i].get(instance);
					} catch (InaccessibleObjectException | IllegalAccessException | SecurityException
							| IllegalArgumentException | NullPointerException | ExceptionInInitializerError e) {
						continue;
					}
				}
			} catch (SecurityException e) {
				throw e;
			}
		}
		@Nullable
		String systemMethodToCheck = (restrictedPackage == null) ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (systemMethodToCheck == null) {
			return;
		}
		if (("java.util.jar.JarFile".equals(declaringTypeName) || "java.util.zip.ZipFile".equals(declaringTypeName))
				&& ("entries".equals(methodName) || "getInputStream".equals(methodName))
				&& !isJdkArchiveReceiver(instance, declaringTypeName)) {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", "archive receiver"));
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		if ("getResourceAsStream".equals(methodName)) {
			@Nullable
			Object[] resourceTargets = resourceReadTargets(instance, parameters);
			if (resourceTargets != null) {
				parameters = resourceTargets;
				instance = null;
				attributes = new Object[0];
			}
		}
		if (checkCopyOrTransferSpecialCase(action, declaringTypeName, methodName, parameters, instance,
				systemMethodToCheck, studentCalledMethod, fullMethodSignature, thisJoinPoint)) {
			return;
		}
		if (checkTempFileCreationSpecialCase(action, declaringTypeName, methodName, parameters, systemMethodToCheck,
				studentCalledMethod, fullMethodSignature, thisJoinPoint)) {
			return;
		}
		if (isPreferencesBackingStore(declaringTypeName)) {
			@Nullable
			final String[] allowedPreferencePaths = getValueFromSettings(switch (action) {
			case "read" -> "pathsAllowedToBeRead";
			case "overwrite" -> "pathsAllowedToBeOverwritten";
			case "create" -> "pathsAllowedToBeCreated";
			case "execute" -> "pathsAllowedToBeExecuted";
			case "delete" -> "pathsAllowedToBeDeleted";
			default -> throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
			});
			if (!allowsAllPaths(allowedPreferencePaths)) {
				throw new SecurityException(localize("security.advice.illegal.file.execution", systemMethodToCheck,
						localizeAction(action), PREFERENCES_BACKING_STORE,
						describeDeniedCall(thisJoinPoint, fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", action, allowedPreferencePaths == null || allowedPreferencePaths.length == 0)));
			}
			return;
		}
		if (isEntryReadOnOpenArchive(action, declaringTypeName, thisJoinPoint)) {
			return;
		}
		List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, declaringTypeName, methodName,
				parameters);
		@Nonnull
		IgnoreValues ignoredParameters = parameterIgnoreValues(extractMethodNameWithoutModifiers(fullMethodSignature),
				methodName, instance);
		for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
			String actionToCheck = WRITE_ACTION.equals(actionCheck.getKey())
					? resolveWriteAction(parameters, ignoredParameters)
					: actionCheck.getKey();
			boolean allowNonExistingPaths = Boolean.TRUE.equals(actionCheck.getValue());
			String messageAction = actionToCheck;
			@Nullable
			final String[] allowedPaths = isWriteThroughOpenFile(actionToCheck, instance)
					? pathsAllowedToBeOverwrittenOrCreated()
					: getValueFromSettings(switch (actionToCheck) {
					case "read" -> "pathsAllowedToBeRead";
					case "overwrite" -> "pathsAllowedToBeOverwritten";
					case "create" -> "pathsAllowedToBeCreated";
					case "execute" -> "pathsAllowedToBeExecuted";
					case "delete" -> "pathsAllowedToBeDeleted";
					default -> throw new SecurityException(
							localize("security.advice.file.system.unknown.action", actionToCheck));
					});
			boolean noAllowRuleConfigured = allowedPaths == null || allowedPaths.length == 0;
			@Nullable
			String illegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null
					: checkIfVariableCriteriaIsViolated(parameters, allowedPaths, ignoredParameters,
							allowNonExistingPaths);
			if (illegallyInteractedThroughParameter != null) {
				boolean isClassLoaderAccess = illegallyInteractedThroughParameter.endsWith(".class")
						&& (isClassLoadingInProgress()
								|| (studentCalledMethod != null
										&& (studentCalledMethod.startsWith("java.lang.Class.forName")
												|| studentCalledMethod.startsWith("java.lang.ClassLoader")
												|| studentCalledMethod.startsWith("jdk.internal.loader"))));
				boolean isSystemJarRead = !isClassLoaderAccess && "read".equals(actionToCheck)
						&& isSystemJarReadExempt(illegallyInteractedThroughParameter);
				boolean isExemptSystemFileAccess = !isClassLoaderAccess && !isSystemJarRead
						&& isExemptSystemFileAccess(actionToCheck, illegallyInteractedThroughParameter);
				if (!isClassLoaderAccess && !isSystemJarRead && !isExemptSystemFileAccess) {
					throw new SecurityException(localize(
							"security.advice.illegal.file.execution", systemMethodToCheck, localizeAction(messageAction),
							illegallyInteractedThroughParameter,
							describeDeniedCall(thisJoinPoint, fullMethodSignature)
									+ describeCaller(studentCalledMethod),
									buildDenialReason("file", actionToCheck, noAllowRuleConfigured)));
				}
			}
			@Nullable
			String illegallyInteractedThroughReceiver = instance == null ? null
					: checkIfVariableCriteriaIsViolated(new Object[] { instance }, allowedPaths, IgnoreValues.NONE,
							allowNonExistingPaths);
			if (illegallyInteractedThroughReceiver != null) {
				boolean isInternalAllowed = illegallyInteractedThroughReceiver.endsWith(".class")
						&& isClassLoadingInProgress();

				if (!isInternalAllowed && "read".equals(actionToCheck)
						&& isSystemJarReadExempt(illegallyInteractedThroughReceiver)) {
					isInternalAllowed = true;
				}
				if (!isInternalAllowed && isExemptSystemFileAccess(actionToCheck, illegallyInteractedThroughReceiver)) {
					isInternalAllowed = true;
				}

				if (!isInternalAllowed) {
					throw new SecurityException(localize(
							"security.advice.illegal.file.execution", systemMethodToCheck, localizeAction(messageAction),
							illegallyInteractedThroughReceiver,
							describeDeniedCall(thisJoinPoint, fullMethodSignature)
									+ describeCaller(studentCalledMethod),
									buildDenialReason("file", actionToCheck, noAllowRuleConfigured)));
				}
			}
			@Nullable
			String illegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null
					: checkIfVariableCriteriaIsViolated(attributes, allowedPaths,
							FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT.getOrDefault(
									extractMethodNameWithoutModifiers(fullMethodSignature), IgnoreValues.NONE),
							allowNonExistingPaths);
			if (illegallyInteractedThroughAttribute != null) {
				boolean isInternalAllowed = illegallyInteractedThroughAttribute.equals("/");
				if (!isInternalAllowed && illegallyInteractedThroughAttribute.endsWith(".class")
						&& (isClassLoadingInProgress()
								|| (studentCalledMethod != null
										&& (studentCalledMethod.startsWith("java.lang.Class.forName")
												|| studentCalledMethod.startsWith("java.lang.ClassLoader")
												|| studentCalledMethod.startsWith("jdk.internal.loader"))))) {
					isInternalAllowed = true;
				}

				if (!isInternalAllowed && "read".equals(actionToCheck)
						&& isSystemJarReadExempt(illegallyInteractedThroughAttribute)) {
					isInternalAllowed = true;
				}
				if (!isInternalAllowed && isExemptSystemFileAccess(actionToCheck, illegallyInteractedThroughAttribute)) {
					isInternalAllowed = true;
				}

				if (!isInternalAllowed) {
					throw new SecurityException(localize(
							"security.advice.illegal.file.execution", systemMethodToCheck, localizeAction(messageAction),
							illegallyInteractedThroughAttribute,
							describeDeniedCall(thisJoinPoint, fullMethodSignature)
									+ describeCaller(studentCalledMethod),
									buildDenialReason("file", actionToCheck, noAllowRuleConfigured)));
				}
			}
		}
	}

	before():
			de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.scannerInitMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.processBuilderRedirectReadMethods() {
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
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.processBuilderRedirectWriteMethods() {
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
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
		checkFileSystemInteraction("execute", thisJoinPoint);
	}

	before():
			de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
		checkFileSystemInteraction("delete", thisJoinPoint);
	}

}
