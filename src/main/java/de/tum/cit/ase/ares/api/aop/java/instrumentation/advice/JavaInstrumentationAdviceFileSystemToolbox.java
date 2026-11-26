package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.NetworkChannel;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation file system security advice.
 * <p>
 * Description: Provides static methods to enforce file system security policies
 * at runtime by checking file system interactions (read, create, overwrite,
 * execute, delete) against allowed paths, call stack criteria, and variable
 * criteria. Uses reflection to interact with test case settings and
 * localisation utilities. Designed to prevent unauthorised file system
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
					IgnoreValues.allExcept(findFieldIndex(ProcessBuilder.class, "command"))),
			// ProcessBuilder.redirectOutput/redirectError/redirectInput(File) - only the
			// File argument names the file; the command strings are not paths.
			Map.entry("java.lang.ProcessBuilder.redirectOutput", IgnoreValues.ALL),
			Map.entry("java.lang.ProcessBuilder.redirectError", IgnoreValues.ALL),
			Map.entry("java.lang.ProcessBuilder.redirectInput", IgnoreValues.ALL));

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
			// (index 0); the mode string ("r"/"rw"/...) is not a path and must not be
			// treated as one.
			Map.entry("java.io.RandomAccessFile.<init>", IgnoreValues.allExcept(0)),
			// DataOutputStream.writeUTF/writeChars/writeBytes(String) - the argument is
			// payload written to the wrapped stream, never a path. The underlying file is
			// validated where the FileOutputStream is opened, so ignore all arguments here.
			Map.entry("java.io.DataOutputStream.writeUTF", IgnoreValues.ALL),
			Map.entry("java.io.DataOutputStream.writeChars", IgnoreValues.ALL),
			Map.entry("java.io.DataOutputStream.writeBytes", IgnoreValues.ALL),
			// The same three on a RandomAccessFile write to the file it has open.
			Map.entry("java.io.RandomAccessFile.writeUTF", IgnoreValues.ALL),
			Map.entry("java.io.RandomAccessFile.writeChars", IgnoreValues.ALL),
			Map.entry("java.io.RandomAccessFile.writeBytes", IgnoreValues.ALL),
			// Scanner(String) scans that text, Scanner(InputStream) a stream. A File or
			// Path is checked where the Scanner opens the stream underneath, so no
			// argument here is read as a path.
			Map.entry("java.util.Scanner.<init>", IgnoreValues.ALL),
			// StringReader(String) - the argument is the text it reads, never a path.
			Map.entry("java.io.StringReader.<init>", IgnoreValues.ALL),
			// PrintStream(OutputStream|File|String, [boolean], [String charset]) - keep
			// only index 0 (the sink); a charset name like "UTF-8" passed when wrapping a
			// socket/stream is not a path. File/String-named PrintStreams still validate
			// their path at index 0.
			Map.entry("java.io.PrintStream.<init>", IgnoreValues.allExcept(0)),
			// PrintWriter(String|File, String charset) and Formatter(String|File, String
			// charset, [Locale]) - only the file (index 0); the charset name is not a path.
			Map.entry("java.io.PrintWriter.<init>", IgnoreValues.allExcept(0)),
			Map.entry("java.util.Formatter.<init>", IgnoreValues.allExcept(0)),
			// FileSystemView.getChild(File dir, String name) and isParent(File dir, File
			// file) list dir; only dir is checked, the name is not a path and the file is
			// not listed.
			Map.entry("javax.swing.filechooser.FileSystemView.getChild", IgnoreValues.allExcept(0)),
			Map.entry("javax.swing.filechooser.FileSystemView.isParent", IgnoreValues.allExcept(0)));

	/**
	 * The JDK class whose subclasses share its argument rules. A call on a subclass
	 * of it, a student's own included, that has no rule of its own is read with the
	 * rule of the method it inherits, so a child name is never taken for a path.
	 */
	@Nonnull
	private static final String FILE_SYSTEM_VIEW = "javax.swing.filechooser.FileSystemView";

	/**
	 * The prefix of the JDK's own nested readers, such as the one
	 * {@code Reader.of(CharSequence)} returns and Java 25 builds a StringReader on.
	 * Their constructors take text or a stream, never a path.
	 */
	@Nonnull
	private static final String JDK_NESTED_READER_PREFIX = "java.io.Reader$";

	/**
	 * Returns which arguments of a call are checked as paths. The exact rule wins;
	 * no argument of a JDK nested reader's constructor is a path; a receiver whose
	 * class extends FileSystemView falls back to that class's rule, found by
	 * comparing class names, so no desktop class is loaded.
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
		if (methodKey.startsWith(JDK_NESTED_READER_PREFIX) && methodKey.endsWith(".<init>")) {
			return IgnoreValues.ALL;
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

	private static final Set<String> NATIVE_LIBRARY_SUFFIXES = Set.of(".dylib", ".jnilib", ".so", ".dll");

	/**
	 * Stand-in shown in the denial message for a
	 * {@code java.util.prefs.Preferences} operation, which has no path to report
	 * because the OS fixes the store's location.
	 */
	@Nonnull
	private static final String PREFERENCES_BACKING_STORE = "<java.util.prefs backing store>";

	/**
	 * The devices the JDK reads random seed bytes from. A read of one is allowed
	 * only while the JDK's own random-number code is doing it, never because of the
	 * name alone.
	 */
	@Nonnull
	private static final Set<String> ENTROPY_SOURCE_PATHS = Set.of("/dev/urandom", "/dev/random");

	/**
	 * The {@code Files} methods the JDK calls to create a temp file:
	 * {@code createFile}, which opens it through {@code newByteChannel} of
	 * {@code Files} and then of the provider; on Linux the provider goes on to its
	 * {@code newFileChannel}. Creating a temp directory goes through
	 * {@code createDirectory} and is not listed.
	 */
	@Nonnull
	private static final Set<String> JDK_TEMP_FILE_STEPS = Set.of("createFile", "newByteChannel", "newFileChannel");

	/**
	 * Trusted JVM home captured at class-initialisation time, before student code
	 * runs, so a later {@code System.setProperty("java.home", ...)} cannot widen
	 * the system-file access exemption into a fail-open read/execute bypass.
	 */
	@Nullable
	private static final String TRUSTED_JAVA_HOME = System.getProperty("java.home");

	/**
	 * Trusted Maven repository root captured at class-initialisation time for the
	 * same reason, resolved from {@code maven.repo.local} with the conventional
	 * {@code ~/.m2/repository} fallback.
	 */
	@Nullable
	private static final String TRUSTED_MAVEN_REPOSITORY = resolveTrustedMavenRepository();

	/**
	 * The folder the JVM loads its own native libraries from, read once at start-up
	 * before student code runs. It is {@code null} unless that value names exactly
	 * one absolute folder inside {@link #TRUSTED_JAVA_HOME}, and then no library
	 * load counts as the JDK's own.
	 */
	@Nullable
	private static final Path TRUSTED_BOOT_LIBRARY_DIRECTORY = trustedBootLibraryDirectory(TRUSTED_JAVA_HOME,
			System.getProperty("sun.boot.library.path"), File.pathSeparator);

	/**
	 * The feature release of the running JDK, such as 21, read once at start-up.
	 */
	private static final int RUNNING_FEATURE_RELEASE = Runtime.version().feature();

	/**
	 * The JDK feature releases whose public {@code loadLibrary} methods were
	 * checked to pass every call on to {@code Runtime.loadLibrary0}.
	 */
	@Nonnull
	private static final Set<Integer> AUDITED_FEATURE_RELEASES = Set.of(17, 21, 25);

	/**
	 * The JVM descriptor of {@code System.loadLibrary} and
	 * {@code Runtime.loadLibrary}: one {@code String}, no result.
	 */
	@Nonnull
	private static final String LOAD_LIBRARY_DESCRIPTOR = "(Ljava/lang/String;)V";

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
		throw new SecurityException(localize("security.instrumentation.utility.initialization",
				"JavaInstrumentationAdviceFileSystemToolbox"));
	}
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
	 * prevents a symlinked parent directory from redirecting a create/overwrite of
	 * a not-yet-existing leaf outside the allow-list. Falls back to the input path
	 * if no ancestor exists or the real path cannot be resolved.
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
	// allowNonExistingPathsToBeConsidered is retained for call-site signature
	// symmetry
	// with variableToTarget; existence is now decided uniformly in
	// checkIfPathIsForbidden.
	@SuppressWarnings("unused")
	@Nullable
	private static Path variableToPath(@Nullable Object variableValue, boolean allowNonExistingPathsToBeConsidered) {
		try {
			if (variableValue == null) {
				return null;
			} else if (variableValue instanceof Path) {
				// Path is an interface; a student implementation could run code in
				// normalize()/toAbsolutePath(). Only call into trusted JDK path impls.
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
				if ("".equals(variableValue)) {
					throw new SecurityException(localize("security.instrumentation.invalid.path", variableValue));
				}
				// "/" is the root directory and is a valid path - let it be processed normally
				return Path.of((String) variableValue).normalize().toAbsolutePath();
			} else if (variableValue instanceof File) {
				// File is not final; a student subclass could run code in toURI().
				requireTrustedRuntimeType(variableValue);
				return Path.of(((File) variableValue).toURI()).normalize().toAbsolutePath();
			} else if (variableValue instanceof FileChannel fileChannel) {
				requireTrustedRuntimeType(variableValue);
				return fileChannelPath(fileChannel);
			} else {
				return null;
			}
		} catch (InvalidPathException | URISyntaxException ignored) {
			return null;
		}
	}

	@Nullable
	private static Path fileChannelPath(@Nonnull FileChannel fileChannel) {
		Class<?> currentType = fileChannel.getClass();
		while (currentType != null) {
			try {
				Field pathField = currentType.getDeclaredField("path");
				pathField.setAccessible(true);
				Object path = pathField.get(fileChannel);
				return path instanceof String ? Path.of((String) path).normalize().toAbsolutePath() : null;
			} catch (NoSuchFieldException missing) {
				currentType = currentType.getSuperclass();
			} catch (IllegalAccessException | InaccessibleObjectException inaccessible) {
				return null;
			}
		}
		return null;
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
			// List is an interface; a student impl could run code in iterator(). Only
			// iterate trusted JDK collections (JDK-origin classes only).
			requireTrustedRuntimeType(observedVariable);
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
			// List is an interface; a student impl could run code in iterator(). Only
			// iterate trusted JDK collections (JDK-origin classes only).
			requireTrustedRuntimeType(observedVariable);
			for (Object element : (List<?>) observedVariable) {
				String violationPath = extractViolationPath(element, allowedPaths, allowNonExistingPathsToBeConsidered);
				if (violationPath != null) {
					return violationPath;
				}
			}
			return null;
		} else {
			FileTarget target = variableToTarget(observedVariable, allowNonExistingPathsToBeConsidered);
			if (target != null && checkIfPathIsForbidden(target, allowedPaths, allowNonExistingPathsToBeConsidered)) {
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
			"java.nio.file.Files.newBufferedWriter", "java.nio.file.spi.FileSystemProvider.newOutputStream");

	/**
	 * The methods that open a file for reading, writing or both, decided by the
	 * options alone. Without {@code WRITE} or {@code APPEND} the JDK opens the file
	 * for reading, so an option such as {@code DELETE_ON_CLOSE} never replaces the
	 * read check.
	 */
	@Nonnull
	private static final Set<String> CHANNEL_OPENING_METHODS = Set.of("java.nio.file.Files.newByteChannel",
			"java.nio.file.Files.newInputStream", "java.nio.channels.FileChannel.open",
			"java.nio.channels.AsynchronousFileChannel.open", "java.nio.file.spi.FileSystemProvider.newInputStream",
			"java.nio.file.spi.FileSystemProvider.newByteChannel",
			"java.nio.file.spi.FileSystemProvider.newFileChannel",
			"java.nio.file.spi.FileSystemProvider.newAsynchronousFileChannel",
			"java.nio.file.SecureDirectoryStream.newByteChannel");

	/**
	 * The class every file system provider extends. Its methods open, copy and move
	 * files directly, without going through {@code Files}.
	 */
	@Nonnull
	private static final String FILE_SYSTEM_PROVIDER = "java.nio.file.spi.FileSystemProvider";

	/**
	 * Tells whether a call is made on a file system provider. The instrumented
	 * method belongs to the provider class that runs it, so the receiver decides.
	 *
	 * @param declaringTypeName the called method's class
	 * @param instance          the receiver, or {@code null} for a static call
	 * @return whether the call is a provider method
	 */
	private static boolean isFileSystemProviderCall(@Nonnull String declaringTypeName, @Nullable Object instance) {
		return FILE_SYSTEM_PROVIDER.equals(declaringTypeName)
				|| instance instanceof java.nio.file.spi.FileSystemProvider;
	}

	/**
	 * Returns the called method as class and name, naming a provider method after
	 * {@code FileSystemProvider} whichever provider class runs it, so the opening
	 * rules above apply to every provider.
	 *
	 * @param declaringTypeName the called method's class
	 * @param methodName        the called method
	 * @param instance          the receiver, or {@code null} for a static call
	 * @return the key the opening rules are looked up by
	 */
	@Nonnull
	private static String openingMethodKey(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nullable Object instance) {
		if (isFileSystemProviderCall(declaringTypeName, instance)) {
			return FILE_SYSTEM_PROVIDER + "." + methodName;
		}
		if (instance instanceof java.nio.file.SecureDirectoryStream) {
			return SECURE_DIRECTORY_STREAM + "." + methodName;
		}
		return declaringTypeName + "." + methodName;
	}

	/**
	 * The interface of a directory stream whose methods open, move and delete files
	 * by a path relative to its directory.
	 */
	@Nonnull
	private static final String SECURE_DIRECTORY_STREAM = "java.nio.file.SecureDirectoryStream";

	/**
	 * The methods that open the file they are given for reading and writing, as
	 * {@code RandomAccessFile} in mode {@code "rw"} does.
	 */
	@Nonnull
	private static final Set<String> READ_WRITE_OPENING_METHODS = Set
			.of("javax.imageio.ImageIO.createImageOutputStream", "javax.imageio.stream.FileImageOutputStream.<init>");

	/**
	 * Returns the actions of opening a file for reading and writing: the write
	 * action, and a read when the file exists already, since a new file holds
	 * nothing to read.
	 *
	 * @param file the file being opened
	 * @return each action with whether a missing path is checked
	 */
	@Nonnull
	private static List<Map.Entry<String, Boolean>> readWriteOpeningActions(@Nullable Object file) {
		if ("overwrite".equals(resolveWriteAction(new Object[] { file }, IgnoreValues.NONE))) {
			return List.of(Map.entry(WRITE_ACTION, true), Map.entry("read", false));
		}
		return List.of(Map.entry(WRITE_ACTION, true));
	}

	/**
	 * The process redirects that send a process's output to a file, creating it if
	 * it is missing. They are checked like any other opening for writing; the JDK
	 * opens the file only when the process starts, natively on Windows.
	 */
	@Nonnull
	private static final Set<String> REDIRECT_TARGET_METHODS = Set.of("java.lang.ProcessBuilder.redirectOutput",
			"java.lang.ProcessBuilder.redirectError", "java.lang.ProcessBuilder$Redirect.to",
			"java.lang.ProcessBuilder$Redirect.appendTo", "java.lang.ProcessBuilder.Redirect.to",
			"java.lang.ProcessBuilder.Redirect.appendTo");

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
	 * Returns the actions a {@code RandomAccessFile} constructor stands for: a read
	 * for mode {@code "r"}, and for the {@code "rw"} modes an opening for writing
	 * plus a read when the file exists, see {@link #readWriteOpeningActions}.
	 *
	 * @param parameters the constructor arguments, the mode second
	 * @return each action with whether a missing path is checked, or {@code null}
	 *         without a known mode
	 */
	@Nullable
	private static List<Map.Entry<String, Boolean>> getRandomAccessFileModeActions(@Nullable Object[] parameters) {
		if (parameters == null || parameters.length < 2 || !(parameters[1] instanceof String mode)) {
			return null;
		}
		if ("r".equals(mode)) {
			return List.of(Map.entry("read", false));
		}
		return "rw".equals(mode) || "rws".equals(mode) || "rwd".equals(mode) ? readWriteOpeningActions(parameters[0])
				: null;
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
	 * create. A call without arguments, such as {@code new Formatter()}, names no
	 * path.
	 *
	 * @param parameters      the call arguments
	 * @param ignoreVariables which arguments are not paths
	 * @return {@code overwrite} or {@code create}
	 */
	@Nonnull
	private static String resolveWriteAction(@Nullable Object[] parameters, @Nonnull IgnoreValues ignoreVariables) {
		if (parameters == null || parameters.length == 0) {
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
		return "overwrite".equals(action)
				&& (instance instanceof FileChannel || instance instanceof java.nio.channels.AsynchronousFileChannel
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
	 * @return the values to check as paths, or {@code null} to check the call as it
	 *         is
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
	 * Checks a copy, a move or a channel transfer side by side, since each side
	 * needs its own permission: the source is read, and deleted by a move, and the
	 * target is written. Each firing checks the sides its action stands for, see
	 * {@link #checkMoveRole} and {@link #checkTransferRole}.
	 *
	 * @return {@code true} if the call was one of these methods and is fully
	 *         handled
	 */
	private static boolean checkCopyOrTransferSpecialCase(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object[] parameters, @Nullable Object instance,
			@Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		boolean filesOrProvider = "java.nio.file.Files".equals(declaringTypeName)
				|| isFileSystemProviderCall(declaringTypeName, instance);
		boolean secureDirectory = instance instanceof java.nio.file.SecureDirectoryStream;
		if ((filesOrProvider || secureDirectory) && "move".equals(methodName)) {
			checkMoveRole(action, parameters, secureDirectory ? 2 : 1, secureDirectory, fileSystemMethodToCheck,
					studentCalledMethod, fullMethodSignature);
			return true;
		}
		if (filesOrProvider && "copy".equals(methodName)) {
			if ("read".equals(action)) {
				// source (index 0). The other overloads' index-0 argument (InputStream) is
				// not Path-shaped and resolves to no target, so this is a safe no-op for them.
				checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"),
						isolateParameter(parameters, 0), false, fileSystemMethodToCheck, studentCalledMethod,
						fullMethodSignature);
			} else if ("overwrite".equals(action)) {
				// destination (index 1): an existing file is overwritten only with
				// REPLACE_EXISTING; any other destination is created.
				Object[] destination = isolateParameter(parameters, 1);
				boolean overwritesExisting = hasReplaceExisting(parameters)
						&& "overwrite".equals(resolveWriteAction(destination, IgnoreValues.NONE));
				String resolvedAction = overwritesExisting ? "overwrite" : "create";
				String settingKey = overwritesExisting ? "pathsAllowedToBeOverwritten" : "pathsAllowedToBeCreated";
				checkSinglePathRole(resolvedAction, getValueFromSettings(settingKey), destination, true,
						fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			}
			return true;
		}
		if (("java.nio.channels.FileChannel".equals(declaringTypeName) || instance instanceof FileChannel)
				&& ("transferTo".equals(methodName) || "transferFrom".equals(methodName))) {
			checkTransferRole(action, "transferTo".equals(methodName), declaringTypeName, methodName, parameters,
					instance, fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			return true;
		}
		return false;
	}

	@Nullable
	private static Object parameterAt(@Nullable Object[] parameters, int index) {
		return parameters == null || index < 0 || index >= parameters.length ? null : parameters[index];
	}

	/**
	 * Decides a {@code createTempFile} call on its own, since its overloads put a
	 * directory or a name prefix at the same position. Without a directory it is
	 * allowed once Ares fixed the default temp directory at start-up; with one it
	 * needs that directory or a {@code pathsAllowedToBeCreated} entry.
	 *
	 * @return {@code true} if the call was {@code createTempFile} and is allowed,
	 *         {@code false} for any other call
	 * @throws SecurityException if the call is not allowed or fits no overload
	 */
	private static boolean checkTempFileCreationSpecialCase(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object[] parameters, @Nonnull String fileSystemMethodToCheck,
			@Nullable String studentCalledMethod, @Nonnull String fullMethodSignature) {
		if (!isTempFileCreation(declaringTypeName, methodName)) {
			return false;
		}
		if (!"create".equals(action)) {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
		}
		@Nullable
		Object explicitDirectory = resolveExplicitTempDirectory(declaringTypeName, parameters, fileSystemMethodToCheck,
				fullMethodSignature);
		if (explicitDirectory == null) {
			requireFrozenDefaultTempDirectory();
		} else {
			requireDirectoryAllowedForTempFiles(explicitDirectory, fileSystemMethodToCheck, studentCalledMethod,
					fullMethodSignature);
		}
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
	private static Object resolveExplicitTempDirectory(@Nonnull String declaringTypeName, @Nullable Object[] parameters,
			@Nonnull String fileSystemMethodToCheck, @Nonnull String fullMethodSignature) {
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
				fileSystemMethodToCheck, fullMethodSignature));
	}

	/**
	 * Allows a {@code createTempFile} call without a directory only if the trusted
	 * start-up fixed the default temp directory. Otherwise Ares cannot tell which
	 * directory the JDK will write to, so the call is refused whatever the policy
	 * lists, with the same message as any access without trusted start-up.
	 *
	 * @throws SecurityException if no default temp directory was fixed
	 */
	private static void requireFrozenDefaultTempDirectory() {
		if (frozenDefaultTempDirectory() == null) {
			throw new SecurityException(localize("security.advice.trusted.startup.missing"));
		}
	}

	/**
	 * Allows a {@code createTempFile} directory that is exactly the default temp
	 * directory fixed at start-up, or that {@code pathsAllowedToBeCreated} covers.
	 * Ares's own internal file names grant nothing here, since a student can name a
	 * directory after them.
	 *
	 * @throws SecurityException if neither holds
	 */
	private static void requireDirectoryAllowedForTempFiles(@Nonnull Object explicitDirectory,
			@Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		@Nullable
		String[] allowedPaths = getValueFromSettings("pathsAllowedToBeCreated");
		@Nullable
		String violation = checkIfVariableCriteriaIsViolated(new Object[] { explicitDirectory }, allowedPaths,
				IgnoreValues.NONE, true);
		if (violation == null || isFrozenDefaultTempDirectory(explicitDirectory)) {
			return;
		}
		boolean noAllowRuleConfigured = allowedPaths == null || allowedPaths.length == 0;
		throw new SecurityException(
				localize("security.advice.illegal.file.execution", fileSystemMethodToCheck, localizeAction("create"),
						violation, describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", "create", noAllowRuleConfigured)));
	}

	/**
	 * Tells whether a directory is exactly the default temp directory fixed at
	 * start-up once symbolic links are followed, so a subdirectory or a link
	 * leading elsewhere does not count. Following links reads the file system,
	 * which is safe because this runs inside the advice's guard against calling
	 * itself. Any failure counts as "not the same directory".
	 *
	 * @param explicitDirectory the directory argument, a {@code Path} or a
	 *                          {@code File}
	 * @return {@code true} only for the fixed default temp directory itself
	 */
	private static boolean isFrozenDefaultTempDirectory(@Nonnull Object explicitDirectory) {
		@Nullable
		String frozenDirectory = frozenDefaultTempDirectory();
		if (frozenDirectory == null) {
			return false;
		}
		try {
			@Nullable
			Path candidate = variableToPath(explicitDirectory, true);
			return candidate != null && candidate.toRealPath().equals(Path.of(frozenDirectory));
		} catch (IOException | InvalidPathException unresolvable) {
			return false;
		}
	}

	/**
	 * Returns the default temp directory that trusted Ares code fixed at start-up,
	 * already resolved to its real location, or {@code null} if nothing fixed it.
	 *
	 * @return the fixed directory, or {@code null}
	 */
	@Nullable
	private static String frozenDefaultTempDirectory() {
		return getValueFromSettings("frozenDefaultTempDirectory");
	}

	/**
	 * Checks the side of a move that the firing action stands for: reading and
	 * deleting the source, and writing the target, which is an overwrite when the
	 * target exists and a create when it does not.
	 *
	 * @param action                  the firing action: read, delete or overwrite
	 * @param parameters              the move's arguments, source first
	 * @param targetIndex             where the target is among them
	 * @param secureDirectory         whether a secure directory stream moves, whose
	 *                                relative paths are refused
	 * @param fileSystemMethodToCheck the method named in a refusal
	 * @param studentCalledMethod     the student method that made the call
	 * @param fullMethodSignature     the called method's signature
	 * @throws SecurityException if the policy forbids that side
	 */
	private static void checkMoveRole(@Nonnull String action, @Nullable Object[] parameters, int targetIndex,
			boolean secureDirectory, @Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		Object[] source = isolateParameter(parameters, 0);
		if (secureDirectory) {
			refuseRelativeSecureDirectoryPaths(action, parameters, fileSystemMethodToCheck, studentCalledMethod,
					fullMethodSignature);
		}
		switch (action) {
		case "read" -> checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"), source, false,
				fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
		case "delete" -> checkSinglePathRole("delete", getValueFromSettings("pathsAllowedToBeDeleted"), source, true,
				fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
		case "overwrite" -> {
			Object[] target = isolateParameter(parameters, targetIndex);
			String targetAction = resolveWriteAction(target, IgnoreValues.NONE);
			String settingKey = "overwrite".equals(targetAction) ? "pathsAllowedToBeOverwritten"
					: "pathsAllowedToBeCreated";
			checkSinglePathRole(targetAction, getValueFromSettings(settingKey), target, true, fileSystemMethodToCheck,
					studentCalledMethod, fullMethodSignature);
		}
		default -> throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
		}
	}

	/**
	 * Checks the sides of a channel transfer that the firing action stands for.
	 * {@code transferTo} reads its own file and writes the target channel: the read
	 * firing checks the read and a network target, the overwrite firing a file
	 * target. {@code transferFrom} fires as an overwrite only and checks its own
	 * file, a file source for reading and a network source.
	 *
	 * @param action                  the firing action: read or overwrite
	 * @param transferTo              whether the call is {@code transferTo}
	 * @param declaringTypeName       the intercepted method's class
	 * @param methodName              the intercepted method
	 * @param parameters              the call's arguments
	 * @param instance                the file channel the call is made on
	 * @param fileSystemMethodToCheck the method named in a refusal
	 * @param studentCalledMethod     the student method that made the call
	 * @param fullMethodSignature     the called method's signature
	 * @throws SecurityException if the policy forbids a side
	 */
	private static void checkTransferRole(@Nonnull String action, boolean transferTo, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object[] parameters, @Nullable Object instance,
			@Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		Object[] receiver = instance == null ? new Object[0] : new Object[] { instance };
		Object other = parameterAt(parameters, transferTo ? 2 : 0);
		Object[] otherFile = other instanceof FileChannel ? new Object[] { other } : new Object[0];
		if (transferTo && "read".equals(action)) {
			checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"), receiver, false,
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			checkNetworkPeer("send", other, declaringTypeName, methodName, fullMethodSignature);
		} else if (transferTo && "overwrite".equals(action)) {
			checkSinglePathRole("overwrite", pathsAllowedToBeOverwrittenOrCreated(), otherFile, false,
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
		} else if (!transferTo && "overwrite".equals(action)) {
			checkSinglePathRole("overwrite", pathsAllowedToBeOverwrittenOrCreated(), receiver, false,
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			checkSinglePathRole("read", getValueFromSettings("pathsAllowedToBeRead"), otherFile, false,
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			checkNetworkPeer("receive", other, declaringTypeName, methodName, fullMethodSignature);
		} else {
			throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
		}
	}

	/**
	 * Checks a network channel on the other side of a transfer against the network
	 * permissions. Anything else is left alone.
	 *
	 * @param action              {@code send} or {@code receive}
	 * @param other               the other side of the transfer
	 * @param declaringTypeName   the intercepted method's class
	 * @param methodName          the intercepted method
	 * @param fullMethodSignature the called method's signature
	 * @throws SecurityException if the policy forbids that peer
	 */
	private static void checkNetworkPeer(@Nonnull String action, @Nullable Object other,
			@Nonnull String declaringTypeName, @Nonnull String methodName, @Nonnull String fullMethodSignature) {
		if (other instanceof NetworkChannel) {
			JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteractionWithinAdvice(action,
					declaringTypeName, methodName, fullMethodSignature, null, null, other);
		}
	}

	/**
	 * Refuses a relative path given to a secure directory stream, unless the action
	 * is allowed for every path. Such a path names a file inside the stream's
	 * directory, which Ares cannot see, so it cannot be checked.
	 *
	 * @param action                  the action being checked
	 * @param parameters              the call's arguments
	 * @param fileSystemMethodToCheck the method named in a refusal
	 * @param studentCalledMethod     the student method that made the call
	 * @param fullMethodSignature     the called method's signature
	 * @throws SecurityException for a relative path
	 */
	private static void refuseRelativeSecureDirectoryPaths(@Nonnull String action, @Nullable Object[] parameters,
			@Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		@Nullable
		String[] allowedPaths = getValueFromSettings(settingKeyOf(action));
		if (parameters == null || allowsAllPaths(allowedPaths)) {
			return;
		}
		for (Object parameter : parameters) {
			if (parameter instanceof Path path && !path.isAbsolute()) {
				throw new SecurityException(localize("security.advice.illegal.file.execution", fileSystemMethodToCheck,
						localizeAction(action), path.toString(),
						describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", action, allowedPaths == null || allowedPaths.length == 0)));
			}
		}
	}

	/**
	 * Returns the setting that lists the paths allowed for an action.
	 *
	 * @param action read, overwrite, create, execute or delete
	 * @return the setting's name
	 * @throws SecurityException for any other action
	 */
	@Nonnull
	private static String settingKeyOf(@Nonnull String action) {
		return switch (action) {
		case "read" -> "pathsAllowedToBeRead";
		case "overwrite" -> "pathsAllowedToBeOverwritten";
		case "create" -> "pathsAllowedToBeCreated";
		case "execute" -> "pathsAllowedToBeExecuted";
		case "delete" -> "pathsAllowedToBeDeleted";
		default -> throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
		};
	}

	/**
	 * Checks one side of a student's file copy or transfer against the given
	 * allow-list and throws a {@code SecurityException} on a violation. No
	 * exemption applies: the student names both paths, so neither is Ares or the
	 * JDK at work.
	 */
	private static void checkSinglePathRole(@Nonnull String actionLabel, @Nullable String[] allowedPaths,
			@Nonnull Object[] candidates, boolean allowNonExistingPaths, @Nonnull String fileSystemMethodToCheck,
			@Nullable String studentCalledMethod, @Nonnull String fullMethodSignature) {
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
		throw new SecurityException(
				localize("security.advice.illegal.file.execution", fileSystemMethodToCheck, localizeAction(actionLabel),
						violation, describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", actionLabel, noAllowRuleConfigured)));
	}

	/**
	 * Returns {@code true} if {@code parameters} contains
	 * {@link StandardCopyOption#REPLACE_EXISTING}, scanning into varargs arrays the
	 * same way {@link #collectStandardOpenOptions} does for
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
	 * @param instance          the receiver, or {@code null} for a static call
	 * @return each action with whether a missing path is checked
	 */
	private static List<Map.Entry<String, Boolean>> deriveActionChecks(@Nonnull String defaultAction,
			@Nonnull String declaringTypeName, @Nonnull String methodName, @Nullable Object[] parameters,
			@Nullable Object instance) {
		FileChannel.MapMode mapMode = extractMapMode(parameters);
		if (mapMode != null) {
			return List.of(Map.entry("READ_ONLY".equals(mapMode.toString()) ? "read" : "overwrite", false));
		}
		String methodKey = openingMethodKey(declaringTypeName, methodName, instance);
		Set<StandardOpenOption> options = extractStandardOpenOptions(parameters);
		if (WRITE_OPENING_METHODS.contains(methodKey) || CHANNEL_OPENING_METHODS.contains(methodKey)) {
			return openOptionActions(effectiveOpenOptions(methodKey, options));
		}
		List<Map.Entry<String, Boolean>> optionActions = openOptionActions(options);
		if (!optionActions.isEmpty()) {
			return optionActions;
		}
		if (REDIRECT_TARGET_METHODS.contains(methodKey)) {
			return List.of(Map.entry(WRITE_ACTION, true));
		}
		if (READ_WRITE_OPENING_METHODS.contains(methodKey) && parameters != null && parameters.length > 0
				&& parameters[0] instanceof File) {
			return readWriteOpeningActions(parameters[0]);
		}
		if ("java.io.RandomAccessFile".equals(declaringTypeName)) {
			List<Map.Entry<String, Boolean>> modeActions = getRandomAccessFileModeActions(parameters);
			if (modeActions != null) {
				return modeActions;
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
	 * Returns {@code true} when {@code path} points at a .jar inside the system
	 * infrastructure (the Maven local repository or the JDK installation). Such
	 * reads are triggered by JUnit / ServiceLoader during class loading, never by
	 * student code accessing project files, so they are exempt from the read
	 * policy. The Maven repository is anchored to {@code maven.repo.local} (or
	 * {@code ~/.m2/repository}) rather than matched as a loose substring, so a
	 * student cannot bypass the read policy by reading from a self-made
	 * ".m2/repository" directory.
	 *
	 * @param path the already-resolved path string under inspection
	 * @return true if the path is a system-infrastructure .jar read
	 */
	private static boolean isSystemJarReadExempt(@Nonnull String path) {
		if (!path.endsWith(".jar")) {
			return false;
		}
		return isPathWithin(path, TRUSTED_MAVEN_REPOSITORY) || isPathWithin(path, TRUSTED_JAVA_HOME);
	}

	/**
	 * Resolves the Maven local repository root from {@code maven.repo.local}, with
	 * the conventional {@code ~/.m2/repository} fallback. Returns {@code null} when
	 * neither can be determined.
	 *
	 * @return the Maven repository root, or {@code null}
	 */
	@Nullable
	private static String resolveTrustedMavenRepository() {
		String mavenRepository = System.getProperty("maven.repo.local");
		if (mavenRepository == null || mavenRepository.isEmpty()) {
			String userHome = System.getProperty("user.home");
			if (userHome == null) {
				return null;
			}
			mavenRepository = userHome + java.io.File.separator + ".m2" + java.io.File.separator + "repository";
		}
		return mavenRepository;
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
	 * Determines whether a file-system access targets JVM infrastructure under
	 * {@code java.home} that must never be treated as student file access.
	 * <p>
	 * Description: JDK-internal reads under {@code java.home} (e.g. the
	 * conf/security crypto-policy files read by {@code javax.crypto.JceSecurity}'s
	 * static initialiser) are JVM infrastructure, not student file access, and must
	 * not be blocked. Native-library loads ({@code .dylib}/{@code .jnilib}/
	 * {@code .so}/{@code .dll}) under {@code java.home} are exempt for the
	 * {@code execute} action for the same reason.
	 *
	 * @param action the concrete file-system action under inspection
	 * @param path   the already-resolved path string under inspection
	 * @return true if the access is exempt JVM-infrastructure access
	 */
	private static boolean isExemptSystemFileAccess(@Nonnull String action, @Nonnull String path) {
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
	 * Tells whether a read of a random-seed device such as {@code /dev/urandom} is
	 * done by the JDK's own random-number code rather than by student code opening
	 * the device itself.
	 *
	 * @param action the file-system action being checked
	 * @param path   the path being read
	 * @return {@code true} only for a seed device read by the JDK's own seeding
	 */
	private static boolean isEntropySourceRead(@Nonnull String action, @Nullable String path) {
		if (!"read".equals(action) || path == null || !ENTROPY_SOURCE_PATHS.contains(path)) {
			return false;
		}
		return isSecureRandomSeedingInProgress();
	}

	/**
	 * Tells whether the JDK's own temp-file code is creating a file directly in the
	 * default temp directory fixed at start-up, as the inner steps of
	 * {@code Files.createTempFile} without a directory, in {@code Files} or in the
	 * provider below it. A temp directory does not count, and any student code
	 * between that JDK code and this check, such as a file attribute's method,
	 * makes it student access.
	 *
	 * @param action            the file-system action being checked
	 * @param declaringTypeName the class of the intercepted method
	 * @param methodName        the intercepted method
	 * @param instance          the receiver, or {@code null} for a static call
	 * @param path              the path being created
	 * @return {@code true} only for that inner step
	 */
	private static boolean isJdkTempFileCreation(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nullable Object instance, @Nullable String path) {
		boolean filesOrProvider = "java.nio.file.Files".equals(declaringTypeName)
				|| isFileSystemProviderCall(declaringTypeName, instance);
		if (!("create".equals(action) || "overwrite".equals(action)) || path == null || !filesOrProvider
				|| !JDK_TEMP_FILE_STEPS.contains(methodName)) {
			return false;
		}
		@Nullable
		String frozenDirectory = frozenDefaultTempDirectory();
		if (frozenDirectory == null || !isDirectlyInside(path, frozenDirectory)) {
			return false;
		}
		return isJdkTempFileHelperCallingWithoutStudentCode(getValueFromSettings("restrictedPackage"));
	}

	/**
	 * Tells whether a path names an entry directly inside a directory, not deeper,
	 * once the directory part is resolved to its real location. Any failure counts
	 * as "not inside".
	 *
	 * @param path      the path to test
	 * @param directory the directory, already resolved to its real location
	 * @return {@code true} if the path's parent is exactly that directory
	 */
	private static boolean isDirectlyInside(@Nonnull String path, @Nonnull String directory) {
		try {
			@Nullable
			Path parent = Path.of(path).toAbsolutePath().normalize().getParent();
			return parent != null && parent.toRealPath().equals(Path.of(directory));
		} catch (IOException | InvalidPathException unresolvable) {
			return false;
		}
	}

	/**
	 * Returns whether the intercepted call targets the Java Preferences API. Its
	 * backing store sits where the operating system puts it, so the student passes
	 * no path and the key and value arguments must not be checked as paths. No
	 * policy can scope that store to a directory, so only an allow-list of all
	 * paths ("*") permits it.
	 *
	 * @param declaringTypeName the fully qualified type the called method belongs
	 *                          to
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
	 * Accepts archive receivers loaded by the JDK. A student subclass could
	 * override {@code size()} and run code while the advice guard is active.
	 *
	 * @param instance          the receiver
	 * @param declaringTypeName the intercepted method's owner
	 * @return whether the receiver fits the archive method's owner
	 * @throws SecurityException if a student loader owns the receiver
	 */
	private static boolean isJdkArchiveReceiver(@Nullable Object instance, @Nonnull String declaringTypeName) {
		if (!(instance instanceof java.util.zip.ZipFile)
				|| !("java.util.jar.JarFile".equals(declaringTypeName) && instance instanceof java.util.jar.JarFile
						|| "java.util.zip.ZipFile".equals(declaringTypeName))) {
			return false;
		}
		requireTrustedRuntimeType(instance);
		return true;
	}

	/**
	 * Tells whether the call reads an entry of a {@code JarFile} or {@code ZipFile}
	 * that is already open. Opening it was checked against the policy, and an entry
	 * names no file of its own.
	 *
	 * @param action            the file-system action
	 * @param declaringTypeName the called method's class
	 * @param methodName        the called method
	 * @param methodSignature   its parameter list, such as {@code ()}
	 * @param parameters        the call arguments
	 * @param instance          the archive
	 * @return whether the call is such an entry read
	 */
	private static boolean isEntryReadOnOpenArchive(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nullable String methodName, @Nullable String methodSignature, @Nullable Object[] parameters,
			@Nullable Object instance) {
		if (!"read".equals(action) || !(instance instanceof java.util.zip.ZipFile archive)
				|| !isJdkArchiveReceiver(archive, declaringTypeName) || parameters == null) {
			return false;
		}
		boolean entryCall = "entries".equals(methodName) && "()".equals(methodSignature) && parameters.length == 0;
		boolean streamCall = "getInputStream".equals(methodName) && "(java.util.zip.ZipEntry)".equals(methodSignature)
				&& parameters.length == 1 && parameters[0] instanceof java.util.zip.ZipEntry;
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
	 * Checks actual path parameters, receivers and attributes for one filesystem
	 * action.
	 */
	private static void checkFileSystemInteractionForAction(@Nonnull String action,
			boolean allowNonExistingPathsToBeConsidered, @Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nonnull String methodSignature, @Nullable Object[] attributes, @Nullable Object[] parameters,
			@Nullable Object instance, @Nonnull String fileSystemMethodToCheck, @Nullable String studentCalledMethod,
			@Nonnull String fullMethodSignature) {
		@Nullable
		final String[] allowedPaths = isWriteThroughOpenFile(action, instance) ? pathsAllowedToBeOverwrittenOrCreated()
				: getValueFromSettings(switch (action) {
				case "read" -> "pathsAllowedToBeRead";
				case "overwrite" -> "pathsAllowedToBeOverwritten";
				case "create" -> "pathsAllowedToBeCreated";
				case "execute" -> "pathsAllowedToBeExecuted";
				case "delete" -> "pathsAllowedToBeDeleted";
				default -> throw new SecurityException(localize("security.advice.file.system.unknown.action", action));
				});
		boolean noAllowRuleConfigured = allowedPaths == null || allowedPaths.length == 0;
		if (instance instanceof java.nio.file.SecureDirectoryStream) {
			refuseRelativeSecureDirectoryPaths(action, parameters, fileSystemMethodToCheck, studentCalledMethod,
					fullMethodSignature);
		}
		if (isPreferencesBackingStore(declaringTypeName) && !allowsAllPaths(allowedPaths)) {
			throw new SecurityException(localize("security.advice.illegal.file.execution", fileSystemMethodToCheck,
					localizeAction(action), PREFERENCES_BACKING_STORE,
					describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
					buildDenialReason("file", action, noAllowRuleConfigured)));
		}
		if (isPreferencesBackingStore(declaringTypeName)) {
			return;
		}
		@Nullable
		String pathIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(parameters, allowedPaths,
						parameterIgnoreValues(declaringTypeName + "." + methodName, methodName, instance),
						allowNonExistingPathsToBeConsidered);
		String messageAction = action;
		boolean isJarResourceRead = isEntryReadOnOpenArchive(action, declaringTypeName, methodName, methodSignature,
				parameters, instance);
		if (pathIllegallyInteractedThroughParameter != null) {
			boolean isClassLoaderAccess = pathIllegallyInteractedThroughParameter.endsWith(".class")
					&& (isClassLoadingInProgress() || (studentCalledMethod != null
							&& (studentCalledMethod.startsWith("java.lang.Class.forName")
									|| studentCalledMethod.startsWith("java.lang.ClassLoader")
									|| studentCalledMethod.startsWith("jdk.internal.loader"))));
			boolean isSystemJarRead = "read".equals(action)
					&& isSystemJarReadExempt(pathIllegallyInteractedThroughParameter);
			boolean isExemptSystemFileAccess = isExemptSystemFileAccess(action,
					pathIllegallyInteractedThroughParameter);
			boolean isEntropySourceRead = isEntropySourceRead(action, pathIllegallyInteractedThroughParameter);
			boolean isJdkTempFileCreation = isJdkTempFileCreation(action, declaringTypeName, methodName, instance,
					pathIllegallyInteractedThroughParameter);
			if (!isClassLoaderAccess && !isSystemJarRead && !isExemptSystemFileAccess && !isJarResourceRead
					&& !isEntropySourceRead && !isJdkTempFileCreation) {
				throw new SecurityException(localize("security.advice.illegal.file.execution", fileSystemMethodToCheck,
						localizeAction(messageAction), pathIllegallyInteractedThroughParameter,
						describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", action, noAllowRuleConfigured)));
			}
		}
		@Nullable
		String pathIllegallyInteractedThroughReceiver = instance == null ? null
				: checkIfVariableCriteriaIsViolated(new Object[] { instance }, allowedPaths, IgnoreValues.NONE,
						allowNonExistingPathsToBeConsidered);
		if (pathIllegallyInteractedThroughReceiver != null) {
			boolean isInternalAllowed = pathIllegallyInteractedThroughReceiver.endsWith(".class")
					&& isClassLoadingInProgress();

			if (!isInternalAllowed && "read".equals(action)
					&& isSystemJarReadExempt(pathIllegallyInteractedThroughReceiver)) {
				isInternalAllowed = true;
			}
			if (!isInternalAllowed && isExemptSystemFileAccess(action, pathIllegallyInteractedThroughReceiver)) {
				isInternalAllowed = true;
			}
			if (!isInternalAllowed && isJarResourceRead) {
				isInternalAllowed = true;
			}

			if (!isInternalAllowed && isEntropySourceRead(action, pathIllegallyInteractedThroughReceiver)) {
				isInternalAllowed = true;
			}

			if (!isInternalAllowed) {
				throw new SecurityException(localize("security.advice.illegal.file.execution", fileSystemMethodToCheck,
						localizeAction(messageAction), pathIllegallyInteractedThroughReceiver,
						describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", action, noAllowRuleConfigured)));
			}
		}
		@Nullable
		String pathIllegallyInteractedThroughAttribute = (attributes == null || attributes.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(
						attributes, allowedPaths, FILE_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT
								.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE),
						allowNonExistingPathsToBeConsidered);
		if (pathIllegallyInteractedThroughAttribute != null) {
			boolean isInternalAllowed = false;
			if ("/".equals(pathIllegallyInteractedThroughAttribute)) {
				isInternalAllowed = true;
			}
			if (pathIllegallyInteractedThroughAttribute.endsWith(".class") && (isClassLoadingInProgress()
					|| (studentCalledMethod != null && (studentCalledMethod.startsWith("java.lang.Class.forName")
							|| studentCalledMethod.startsWith("java.lang.ClassLoader")
							|| studentCalledMethod.startsWith("jdk.internal.loader"))))) {
				isInternalAllowed = true;
			}
			if (!isInternalAllowed && "read".equals(action)
					&& isSystemJarReadExempt(pathIllegallyInteractedThroughAttribute)) {
				isInternalAllowed = true;
			}
			if (!isInternalAllowed && isExemptSystemFileAccess(action, pathIllegallyInteractedThroughAttribute)) {
				isInternalAllowed = true;
			}
			if (!isInternalAllowed && isJarResourceRead) {
				isInternalAllowed = true;
			}

			if (!isInternalAllowed && isEntropySourceRead(action, pathIllegallyInteractedThroughAttribute)) {
				isInternalAllowed = true;
			}

			if (!isInternalAllowed) {
				throw new SecurityException(localize("security.advice.illegal.file.execution", fileSystemMethodToCheck,
						localizeAction(messageAction), pathIllegallyInteractedThroughAttribute,
						describeSignature(fullMethodSignature) + describeCaller(studentCalledMethod),
						buildDenialReason("file", action, noAllowRuleConfigured)));
			}
		}
	}

	/**
	 * Whether Byte Buddy is reading the bytecode of a class it is instrumenting,
	 * for instance from a JAR on the boot class path, the first time a student
	 * class of another package loads. Refusing that read fails the transformation
	 * and stops all enforcement, and no student code runs inside the transformer.
	 *
	 * @param action the file action being checked
	 * @return {@code true} for such a read
	 */
	private static boolean isTransformerReadingBytecode(@Nonnull String action) {
		return "read".equals(action) && isByteBuddyTransformationInProgress();
	}

	/**
	 * Checks one file-system call against the policy. A call without a method or
	 * class name is refused.
	 *
	 * @param action            the file-system action
	 * @param declaringTypeName the method's class
	 * @param methodName        the called method
	 * @param methodSignature   its parameter list, such as {@code ()}
	 * @param attributes        the receiver's fields
	 * @param parameters        the call arguments
	 * @param instance          the receiver, or {@code null}
	 * @throws SecurityException if the call is forbidden
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static void checkFileSystemInteraction(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		// Re-entrancy guard: the body below performs file-system work and stack walks
		// that can lazily load JDK classes (e.g. sun.nio.fs.UnixException). Loading
		// those re-enters this advice on the same thread; running the full check during
		// class definition triggers a ClassCircularityError. Skip nested invocations.
		if (!enterAdvice()) {
			return;
		}
		try {
			// <editor-fold desc="Check instrumentation mode early">
			@Nullable
			final String aopMode = getValueFromSettings("aopMode");
			if (aopMode == null || aopMode.isEmpty() || !"INSTRUMENTATION".equals(aopMode)) {
				return;
			}
			@Nullable
			final String restrictedPackage = getValueFromSettings("restrictedPackage");
			if (restrictedPackage == null || restrictedPackage.isEmpty()) {
				return;
			}
			if (isProjectSourcesFinderInProgress() || isTransformerReadingBytecode(action)) {
				return;
			}
			if (methodName == null || methodName.isBlank() || declaringTypeName == null
					|| declaringTypeName.isBlank()) {
				throw new SecurityException(localize("security.advice.file.system.unknown.action", "malformed method"));
			}
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
					: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName,
							methodName);
			if (fileSystemMethodToCheck == null) {
				return;
			}
			if (("java.util.jar.JarFile".equals(declaringTypeName) || "java.util.zip.ZipFile".equals(declaringTypeName))
					&& ("entries".equals(methodName) || "getInputStream".equals(methodName))
					&& !isJdkArchiveReceiver(instance, declaringTypeName)) {
				throw new SecurityException(localize("security.advice.file.system.unknown.action", "archive receiver"));
			}
			@Nullable
			String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
			// </editor-fold>

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
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature)) {
				return;
			}
			if (checkTempFileCreationSpecialCase(action, declaringTypeName, methodName, parameters,
					fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature)) {
				return;
			}
			List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, declaringTypeName,
					methodName, parameters, instance);
			for (Map.Entry<String, Boolean> actionCheck : actionsToValidate) {
				String actionToCheck = WRITE_ACTION.equals(actionCheck.getKey())
						? resolveWriteAction(parameters,
								parameterIgnoreValues(declaringTypeName + "." + methodName, methodName, instance))
						: actionCheck.getKey();
				checkFileSystemInteractionForAction(actionToCheck, Boolean.TRUE.equals(actionCheck.getValue()),
						declaringTypeName, methodName, methodSignature, attributes, parameters, instance,
						fileSystemMethodToCheck, studentCalledMethod, fullMethodSignature);
			}
		} finally {
			exitAdvice();
		}
	}
	// </editor-fold>

	// <editor-fold desc="Native library loads">

	/**
	 * Checks a call of an execute method. A public {@code loadLibrary} call may be
	 * left to the check in {@code Runtime.loadLibrary0}, see
	 * {@link #shouldDeferToLoadLibrary0}.
	 *
	 * @param declaringTypeName the class
	 * @param methodName        the method
	 * @param methodSignature   for messages
	 * @param methodDescriptor  JVM descriptor
	 * @param attributes        receiver fields
	 * @param parameters        arguments
	 * @param instance          receiver
	 * @throws SecurityException if forbidden
	 */
	public static void checkExecuteInteraction(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nonnull String methodSignature, @Nullable String methodDescriptor, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		if (isPublicLoadLibrary(declaringTypeName, methodName, methodDescriptor) && shouldDeferToLoadLibrary0(
				RUNNING_FEATURE_RELEASE, JavaInstrumentationLibraryLoadWeaveRecord.INSTALLATION.isWoven())) {
			return;
		}
		checkFileSystemInteraction("execute", declaringTypeName, methodName, methodSignature, attributes, parameters,
				instance);
	}

	/**
	 * Checks a load at {@code Runtime.loadLibrary0}. The JDK's own load passes; any
	 * other is checked as running that name. A name that is no path is refused only
	 * for student code under an instrumentation policy without a {@code *} execute
	 * rule.
	 *
	 * @param declaringTypeName the class
	 * @param methodName        the method
	 * @param methodSignature   for messages
	 * @param methodDescriptor  JVM descriptor
	 * @param fromClass         the caller the JDK names
	 * @param libraryName       the name
	 * @throws SecurityException if forbidden
	 */
	public static void checkLibraryLoad0(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nonnull String methodSignature, @Nullable String methodDescriptor, @Nullable Class<?> fromClass,
			@Nullable String libraryName) {
		if (isJdkOwnLibraryLoadWithinGuard(declaringTypeName, methodName, methodDescriptor, fromClass, libraryName)) {
			return;
		}
		if (isUnreadableAsPath(libraryName)) {
			refuseUnreadableLibraryName(declaringTypeName, methodName, methodSignature, libraryName);
			return;
		}
		checkFileSystemInteraction("execute", declaringTypeName, methodName, methodSignature, new Object[0],
				new Object[] { libraryName }, null);
	}

	/**
	 * Runs {@link #isJdkOwnLibraryLoad} inside the advice's guard against calling
	 * itself, since looking for the library file reads the file system. A nested
	 * call counts as not exempt; the check it then reaches skips nested calls too.
	 *
	 * @return {@code true} only for the JDK loading its own library
	 */
	private static boolean isJdkOwnLibraryLoadWithinGuard(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nullable String methodDescriptor, @Nullable Class<?> fromClass, @Nullable String libraryName) {
		if (!enterAdvice()) {
			return false;
		}
		try {
			return isJdkOwnLibraryLoad(declaringTypeName, methodName, methodDescriptor, fromClass, libraryName,
					TRUSTED_BOOT_LIBRARY_DIRECTORY);
		} finally {
			exitAdvice();
		}
	}

	/**
	 * Tells whether a call of {@code Runtime.loadLibrary0} is the JDK loading its
	 * own library. Sound because a JDK caller's loader skips its own search, so the
	 * JDK's folder is searched first. The public methods name the real caller; deep
	 * reflection on this one can name any class, but still only a plain name whose
	 * file lies directly in the JDK's library folder is exempt.
	 *
	 * @param bootLibraryDirectory the JDK's own library folder, or {@code null}
	 * @return {@code true} only if every condition holds
	 */
	static boolean isJdkOwnLibraryLoad(@Nullable String declaringTypeName, @Nullable String methodName,
			@Nullable String methodDescriptor, @Nullable Class<?> fromClass, @Nullable String libraryName,
			@Nullable Path bootLibraryDirectory) {
		return isLoadLibrary0(declaringTypeName, methodName, methodDescriptor) && isJdkCaller(fromClass)
				&& isPlainLibraryName(libraryName) && isBootLibraryFile(bootLibraryDirectory, libraryName);
	}

	/**
	 * Tells whether a method is exactly
	 * {@code java.lang.Runtime.loadLibrary0(Class, String)}, compared by its JVM
	 * descriptor.
	 *
	 * @return {@code true} only for that method
	 */
	static boolean isLoadLibrary0(@Nullable String declaringTypeName, @Nullable String methodName,
			@Nullable String methodDescriptor) {
		return JavaInstrumentationLibraryLoadWeaveRecord.DECLARING_TYPE.equals(declaringTypeName)
				&& JavaInstrumentationLibraryLoadWeaveRecord.METHOD_NAME.equals(methodName)
				&& JavaInstrumentationLibraryLoadWeaveRecord.METHOD_DESCRIPTOR.equals(methodDescriptor);
	}

	/**
	 * Tells whether the JVM's own loader or the platform loader defined a class, so
	 * that it is part of the JDK. {@code getClassLoader} cannot be overridden.
	 *
	 * @param fromClass the class the JDK says asked for the library
	 * @return {@code true} for a JDK class, {@code false} for any other or none
	 */
	static boolean isJdkCaller(@Nullable Class<?> fromClass) {
		if (fromClass == null) {
			return false;
		}
		ClassLoader loader = fromClass.getClassLoader();
		return loader == null || loader == ClassLoader.getPlatformClassLoader();
	}

	/**
	 * Tells whether a library name is a plain name: not empty, and without
	 * {@code /}, {@code \}, {@code .} or a NUL character. The name is checked as
	 * passed, without cutting anything off.
	 *
	 * @param libraryName the library name
	 * @return {@code true} for a plain name
	 */
	static boolean isPlainLibraryName(@Nullable String libraryName) {
		return libraryName != null && !libraryName.isEmpty() && libraryName.indexOf('/') < 0
				&& libraryName.indexOf('\\') < 0 && libraryName.indexOf('.') < 0 && libraryName.indexOf('\0') < 0;
	}

	/**
	 * Tells whether the JDK's library folder directly holds a regular file for the
	 * library, under the name the JVM gives it, such as {@code libnet.so}. A name
	 * that leads into another folder, such as a Windows drive-relative
	 * {@code C:evil}, never counts.
	 *
	 * @param bootLibraryDirectory the JDK's own library folder, or {@code null}
	 * @param libraryName          the library name
	 * @return {@code true} if that file exists directly there
	 */
	static boolean isBootLibraryFile(@Nullable Path bootLibraryDirectory, @Nonnull String libraryName) {
		if (bootLibraryDirectory == null) {
			return false;
		}
		try {
			Path candidate = bootLibraryDirectory.resolve(System.mapLibraryName(libraryName)).normalize();
			return bootLibraryDirectory.normalize().equals(candidate.getParent()) && Files.isRegularFile(candidate);
		} catch (InvalidPathException unreadable) {
			return false;
		}
	}

	/**
	 * Works out the JDK's own library folder from the value of
	 * {@code sun.boot.library.path}: {@code lib} on Linux and macOS, {@code bin} on
	 * Windows. It is accepted only as exactly one absolute folder inside the Java
	 * home.
	 *
	 * @param javaHome        the trusted Java home, or {@code null}
	 * @param bootLibraryPath the property's value, or {@code null}
	 * @param pathSeparator   the separator between several folders
	 * @return the folder, or {@code null} if the value is not accepted
	 */
	@Nullable
	static Path trustedBootLibraryDirectory(@Nullable String javaHome, @Nullable String bootLibraryPath,
			@Nonnull String pathSeparator) {
		if (javaHome == null || javaHome.isBlank() || bootLibraryPath == null || bootLibraryPath.isEmpty()
				|| bootLibraryPath.contains(pathSeparator)) {
			return null;
		}
		try {
			Path directory = Path.of(bootLibraryPath);
			if (!directory.isAbsolute()) {
				return null;
			}
			Path normalised = directory.normalize();
			return normalised.startsWith(Path.of(javaHome).toAbsolutePath().normalize()) ? normalised : null;
		} catch (InvalidPathException unreadable) {
			return null;
		}
	}

	/**
	 * Tells whether a method is one of the two public {@code loadLibrary} methods,
	 * compared by its JVM descriptor.
	 *
	 * @return {@code true} for {@code System.loadLibrary(String)} and
	 *         {@code Runtime.loadLibrary(String)}
	 */
	static boolean isPublicLoadLibrary(@Nullable String declaringTypeName, @Nullable String methodName,
			@Nullable String methodDescriptor) {
		return ("java.lang.System".equals(declaringTypeName) || "java.lang.Runtime".equals(declaringTypeName))
				&& "loadLibrary".equals(methodName) && LOAD_LIBRARY_DESCRIPTOR.equals(methodDescriptor);
	}

	/**
	 * Tells whether a JDK feature release is one whose public {@code loadLibrary}
	 * methods were checked to call {@code Runtime.loadLibrary0}.
	 *
	 * @param featureRelease the feature release, such as 21
	 * @return {@code true} for 17, 21 and 25 only
	 */
	static boolean isAuditedFeatureRelease(int featureRelease) {
		return AUDITED_FEATURE_RELEASES.contains(featureRelease);
	}

	/**
	 * Tells whether a public {@code loadLibrary} check may leave the call to the
	 * check in {@code Runtime.loadLibrary0}: only if that check was really woven in
	 * and the JDK is an audited release. Otherwise the public method is checked
	 * itself, so a missing check one step deeper never lets a load through.
	 *
	 * @param featureRelease the running JDK's feature release
	 * @param woven          whether the sealed record says the check was woven
	 * @return {@code true} if the public check may do nothing
	 */
	static boolean shouldDeferToLoadLibrary0(int featureRelease, boolean woven) {
		return woven && isAuditedFeatureRelease(featureRelease);
	}

	/**
	 * Tells whether the file check cannot read a name as a path, for example
	 * because it holds a NUL character. Such a name would pass that check unseen.
	 *
	 * @param libraryName the library name, or {@code null}
	 * @return {@code true} if the name is not {@code null} and not a valid path
	 */
	static boolean isUnreadableAsPath(@Nullable String libraryName) {
		if (libraryName == null) {
			return false;
		}
		try {
			Path.of(libraryName);
			return false;
		} catch (InvalidPathException unreadable) {
			return true;
		}
	}

	/**
	 * Refuses a library load whose name cannot be read as a path, in the cases the
	 * file check would refuse a readable one: instrumentation is active, student
	 * code is on the call stack and the execute allow-list does not allow every
	 * path. The message is the file check's own.
	 *
	 * @throws SecurityException in those cases
	 */
	private static void refuseUnreadableLibraryName(@Nonnull String declaringTypeName, @Nonnull String methodName,
			@Nonnull String methodSignature, @Nonnull String libraryName) {
		if (!enterAdvice()) {
			return;
		}
		try {
			@Nullable
			String restrictedPackage = getValueFromSettings("restrictedPackage");
			if (!"INSTRUMENTATION".equals(getValueFromSettings("aopMode")) || restrictedPackage == null
					|| restrictedPackage.isEmpty() || isProjectSourcesFinderInProgress()) {
				return;
			}
			@Nullable
			String violatingMethod = checkIfCallstackCriteriaIsViolated(restrictedPackage,
					getValueFromSettings("allowedListedClasses"), declaringTypeName, methodName);
			@Nullable
			String[] allowedPaths = getValueFromSettings("pathsAllowedToBeExecuted");
			if (violatingMethod == null || allowsEveryPath(allowedPaths)) {
				return;
			}
			@Nullable
			String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
			throw new SecurityException(localize("security.advice.illegal.file.execution", violatingMethod,
					localizeAction("execute"), libraryName.replace("\0", "\\0"),
					describeSignature(declaringTypeName + "." + methodName + methodSignature)
							+ describeCaller(studentCalledMethod),
					buildDenialReason("file", "execute", allowedPaths == null || allowedPaths.length == 0)));
		} finally {
			exitAdvice();
		}
	}

	/**
	 * Tells whether an allow-list holds the {@code *} entry, which allows every
	 * path.
	 *
	 * @param allowedPaths the allow-list, possibly {@code null}
	 * @return {@code true} if it allows every path
	 */
	private static boolean allowsEveryPath(@Nullable String[] allowedPaths) {
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
	// </editor-fold>
}
