package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
//</editor-fold>

public abstract aspect JavaAspectJAbstractAdviceDefinitions {

	// <editor-fold desc="Constants">
	@Nonnull
	protected static final List<String> IGNORE_CALLSTACK = List.of("java.lang.ClassLoader", "de.tum.cit.ase.ares.api.",
			"com.intellij.rt.debugger.", "jdk.internal.loader.", "jdk.internal.reflect.");

	/**
	 * Cached StackWalker shared by the call-stack inspectors below. The default
	 * options (no RETAIN_CLASS_REFERENCE) are deliberate: the inspectors only read
	 * className/methodName strings, so materialising Class objects would add JNI
	 * work on every walked frame. The walker also avoids the Throwable allocation
	 * and full StackTraceElement[] materialisation that
	 * Thread.currentThread().getStackTrace() pays on every intercepted call, and it
	 * stops walking as soon as a matching frame is found.
	 */
	@Nonnull
	private static final StackWalker STACK_WALKER = StackWalker.getInstance();

	/**
	 * Lazily resolved Class&lt;?&gt; reference for the AOP settings holder, cached
	 * so the reflective lookup runs once per JVM rather than on every advice call.
	 */
	@Nullable
	private static volatile Class<?> SETTINGS_CLASS_CACHE = null;

	/**
	 * Cache of resolved Field handles keyed by field name so getDeclaredField and
	 * setAccessible run once per JVM, not per advice call.
	 */
	@Nonnull
	private static final ConcurrentHashMap<String, Field> SETTINGS_FIELD_CACHE = new ConcurrentHashMap<>();

	/**
	 * Single canonical settings lock, cached once so every caller synchronises on
	 * the same monitor.
	 */
	@Nullable
	private static volatile Object SETTINGS_LOCK_CACHE = null;
	// </editor-fold>

	// <editor-fold desc="Tools methods">

	/**
	 * Retrieves the value of a specified static field from the settings class.
	 * <p>
	 * Description: Uses reflection to access a static field in
	 * JavaAOPTestCaseSettings, allowing retrieval of security-related configuration
	 * values for instrumentation and tests.
	 *
	 * @param fieldName the name of the field to retrieve
	 * @param <T>       the type of the field's value
	 * @return the value of the specified field
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	static <T> T getValueFromSettings(@Nonnull String fieldName) {
		try {
			Objects.requireNonNull(fieldName, "fieldName must not be null");
			@Nonnull
			Field field = resolveSettingsField(fieldName);
			@Nullable
			T value = (T) field.get(null);
			return value;
		} catch (LinkageError e) {
			throw new SecurityException(
					localize("security.advice.linkage.exception", fieldName), e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(
					localize("security.advice.class.not.found.exception", fieldName),
					e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(
					localize("security.advice.no.such.field.exception", fieldName),
					e);
		} catch (NullPointerException e) {
			throw new SecurityException(
					localize("security.advice.null.pointer.exception", fieldName),
					e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(
					localize("security.advice.illegal.access.exception", fieldName),
					e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
		}
	}

	/**
	 * Resolves the static settings Field for the given name once and caches the
	 * resulting Field handle plus the owning Class&lt;?&gt; reference, so the
	 * reflective lookup (Class.forName + getDeclaredField + setAccessible) runs once
	 * per JVM rather than on every advice call. The cached field stays accessible
	 * for the JVM lifetime.
	 *
	 * @param fieldName the name of the JavaAOPTestCaseSettings field to resolve
	 * @return the resolved accessible Field
	 * @throws ClassNotFoundException if the settings class cannot be located
	 * @throws NoSuchFieldException   if the requested field does not exist
	 */
	@Nonnull
	private static Field resolveSettingsField(@Nonnull String fieldName)
			throws ClassNotFoundException, NoSuchFieldException {
		Field cachedField = SETTINGS_FIELD_CACHE.get(fieldName);
		if (cachedField != null) {
			return cachedField;
		}
		Class<?> settingsClass = SETTINGS_CLASS_CACHE;
		if (settingsClass == null) {
			// Take standard class loader as class loader in order to get the
			// JavaAOPTestCaseSettings class at compile time for aspectj
			settingsClass = Objects.requireNonNull(
					Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"),
					"adviceSettingsClass must not be null");
			SETTINGS_CLASS_CACHE = settingsClass;
		}
		Field field = Objects.requireNonNull(settingsClass.getDeclaredField(fieldName), "field must not be null");
		field.setAccessible(true);
		Field existing = SETTINGS_FIELD_CACHE.putIfAbsent(fieldName, field);
		return existing != null ? existing : field;
	}

	/**
	 * Sets the value of a specified static field in the settings class.
	 * <p>
	 * Description: Uses reflection to modify a static field in
	 * JavaAOPTestCaseSettings, allowing updates to security-related configuration
	 * values for instrumentation and tests.
	 *
	 * @param fieldName the name of the field to modify
	 * @param newValue  the new value to assign to the field
	 * @param <T>       the type of the field's value
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	// Package-private (was protected): the only caller is checkAndDecrementSettingsArrayValue
	// in this aspect. Narrowing the visibility removes a trivially-reachable write entry
	// point into the security settings. (Defence in depth only; the authoritative
	// protection against student tampering is the architecture-analysis layer.)
	private static <T> void setValueToSettings(@Nonnull String fieldName, @Nullable T newValue) {
		try {
			Objects.requireNonNull(fieldName, "fieldName must not be null");
			@Nonnull
			Field field = resolveSettingsField(fieldName);
			field.set(null, newValue);
		} catch (LinkageError e) {
			throw new SecurityException(
					localize("security.advice.linkage.exception", fieldName), e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(
					localize("security.advice.class.not.found.exception", fieldName),
					e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(
					localize("security.advice.no.such.field.exception", fieldName),
					e);
		} catch (NullPointerException e) {
			throw new SecurityException(
					localize("security.advice.null.pointer.exception", fieldName),
					e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(
					localize("security.advice.illegal.access.exception", fieldName),
					e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
		}
	}

	/**
	 * Retrieves the settings lock object for synchronization.
	 * <p>
	 * Description: Uses reflection to get the SETTINGS_LOCK from
	 * JavaAOPTestCaseSettings, allowing synchronized access to settings for
	 * thread-safe operations.
	 *
	 * @return the settings lock object
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	private static Object getSettingsLock() {
		@Nullable
		Object cached = SETTINGS_LOCK_CACHE;
		if (cached != null) {
			return cached;
		}
		try {
			// Take standard class loader as class loader in order to get the
			// JavaAOPTestCaseSettings class at compile time for aspectj
			@Nonnull
			Class<?> adviceSettingsClass = Objects.requireNonNull(
					Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"),
					"adviceSettingsClass must not be null");
			@Nonnull
			Method getLockMethod = adviceSettingsClass.getDeclaredMethod("getSettingsLock");
			@Nonnull
			Object lock = Objects.requireNonNull(getLockMethod.invoke(null), "lock must not be null");
			SETTINGS_LOCK_CACHE = lock;
			return lock;
		} catch (Exception e) {
			// Fail closed: never fall back to a DIFFERENT monitor (e.g. the Class object).
			// Two threads synchronising on different monitors would make the quota
			// check-and-decrement non-atomic, so a lock-resolution failure must abort.
			throw new SecurityException(localize("security.advice.class.not.found.exception", "JavaAOPTestCaseSettings"), e);
		}
	}

	/**
	 * Atomically checks if the value at a specified index in an integer array
	 * setting is positive, and if so, decrements it.
	 * <p>
	 * Description: This method combines the check and decrement into a single
	 * atomic operation to prevent race conditions where multiple threads could pass
	 * the check simultaneously.
	 *
	 * @param settingsArray the name of the array field in settings
	 * @param position      the index position of the value to check and decrement
	 * @return true if the value was positive and successfully decremented, false if
	 *         the quota is exhausted
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	static boolean checkAndDecrementSettingsArrayValue(@Nonnull String settingsArray, int position) {
		synchronized (getSettingsLock()) {
			@Nullable
			int[] array = getValueFromSettings(settingsArray);
			if (array == null || position < 0 || position >= array.length) {
				return false;
			}
			if (array[position] <= 0) {
				return false;
			}
			@Nonnull
			int[] clone = array.clone();
			clone[position]--;
			setValueToSettings(settingsArray, clone);
			return true;
		}
	}

	/**
	 * Retrieves a localised message based on a key and optional arguments.
	 * <p>
	 * Description: Attempts to fetch a localized string from the Messages class
	 * using reflection. Falls back to the key if localization fails.
	 *
	 * @param key  the localization key identifying the message
	 * @param args optional arguments to format the localized message
	 * @return the localized message string, or the key itself if localization fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	public static String localize(@Nonnull String key, @Nullable Object... args) {
		try {
			@Nonnull
			Class<?> messagesClass = Class.forName("de.tum.cit.ase.ares.api.localization.Messages", true,
					Thread.currentThread().getContextClassLoader());
			@Nonnull
			Method localized = messagesClass.getDeclaredMethod("localized", String.class, Object[].class);
			@Nullable
			Object result = localized.invoke(null, key, args);
			if (result instanceof String str) {
				return str;
			} else {
				// Do not recurse through localize() here: if the Messages method returns a
				// non-String, a nested localize() call could hit this same branch and recurse
				// until the stack overflows. Return the raw key instead.
				return "security.localization.method.return.type";
			}
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
				| IllegalAccessException e) {
			// Fallback: Return the key if localization fails
			return key;
		}
	}

	/**
	 * Returns a human-readable denial reason that explains why a resource access
	 * was blocked, distinguishing between "no allow rule configured at all" and "an
	 * allow rule exists but does not permit this access".
	 *
	 * @param noAllowRuleConfigured {@code true} if no allow rule was configured for
	 *                              the resource type at all; {@code false} if a
	 *                              rule exists but does not permit this particular
	 *                              access
	 * @return a non-null reason string suitable for appending to SecurityException
	 *         messages
	 */
	@Nonnull
	static String buildDenialReason(boolean noAllowRuleConfigured) {
		if (noAllowRuleConfigured) {
			return localize("security.advice.denial.reason.no.allowlist");
		}
		return localize("security.advice.denial.reason.not.in.allowlist");
	}

	/**
	 * Returns {@code true} when the current call stack is inside the JVM's
	 * class-loading machinery, i.e. a class loader is reading a {@code .class} file
	 * to define a class. Such a read is the JVM loading bytecode in order to
	 * execute it, not student code reading a file as data, so it must not be
	 * treated as an illegal file-system access. The signal is precise and cannot be
	 * spoofed: a student opening a {@code .class} file directly (e.g. via
	 * {@code new FileInputStream("Secret.class")}) has no class-loader frame
	 * between their own code and the read, so such an access stays blocked. An
	 * explicit iterator is used (rather than {@code Stream.anyMatch}) to mirror the
	 * existing call-stack inspectors.
	 *
	 * @return {@code true} if a class-loader frame is present on the current stack
	 */
	static boolean isClassLoadingInProgress() {
		return STACK_WALKER.walk(frames -> {
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next().getClassName();
				if (className.startsWith("jdk.internal.loader.") || className.equals("java.lang.ClassLoader")
						|| className.equals("java.security.SecureClassLoader")
						|| className.equals("java.net.URLClassLoader")) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Returns {@code true} only while Ares itself is reading framework support
	 * files for structural and architecture test setup through one of its trusted
	 * utilities ({@code ProjectSourcesFinder}, {@code ClassNameScanner},
	 * {@code FileTools}, {@code CustomCallgraphBuilder}).
	 * <p>
	 * The exemption is granted only when the trusted utility was invoked by Ares
	 * internal code: walking towards the callers, all consecutive trusted-utility
	 * frames are skipped and the first real initiator above them must be an Ares
	 * ({@code de.tum.cit.ase.ares.api.*}) frame. Any other initiator (student code,
	 * an external helper, or a reflection trampoline) is not exempt, which closes
	 * the bypass where user code could route forbidden file access through e.g.
	 * {@code FileTools.readFile(...)}, including via the utilities' own internal
	 * call chains.
	 *
	 * @return {@code true} if an internal Ares utility is performing the access
	 */
	static boolean isProjectSourcesFinderInProgress() {
		return STACK_WALKER.walk(frames -> {
			boolean trustedSeen = false;
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next().getClassName();
				if (isTrustedSetupUtility(className)) {
					trustedSeen = true;
					continue;
				}
				if (trustedSeen) {
					// First non-utility frame above the trusted utilities is the initiator.
					return className.startsWith("de.tum.cit.ase.ares.api.") ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Returns true when {@code className} is one of Ares' trusted setup utilities
	 * that legitimately read framework support files during structural and
	 * architecture test setup.
	 */
	private static boolean isTrustedSetupUtility(@Nonnull String className) {
		return className.equals("de.tum.cit.ase.ares.api.util.ProjectSourcesFinder")
				|| className.equals("de.tum.cit.ase.ares.api.structural.testutils.ClassNameScanner")
				|| className.equals("de.tum.cit.ase.ares.api.util.FileTools")
				|| className.equals("de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder");
	}
	// </editor-fold>

	// <editor-fold desc="Callstack criteria methods">

	/**
	 * Checks the current call stack for violations of restricted packages.
	 * <p>
	 * Description: First checks if the direct caller of the intercepted method is
	 * in IGNORE_CALLSTACK (e.g., ClassLoader). If so, the operation is allowed.
	 * Otherwise, examines the stack trace to find the first element whose class
	 * name starts with the restricted package but is not in the allowed classes
	 * list, skipping any classes in the ignore list.
	 *
	 * @param restrictedPackage the prefix of restricted package names
	 * @param allowedClasses    the array of allowed class name prefixes
	 * @param declaringTypeName the class name of the intercepted method
	 * @param methodName        the name of the intercepted method
	 * @return the fully qualified method name that violates criteria, or null if
	 *         none
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nullable
	static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses,
			String declaringTypeName, String methodName) {
		// Stream the stack lazily so the walk stops once both the violation and the
		// caller above the first restricted frame are known (or the stack is exhausted).
		// Skip ignorable frames (Ares internals, class loading, reflection trampolines)
		// to prevent reflective dispatch from bypassing the check simply because
		// jdk.internal.reflect.* appears as an intermediate caller.
		// Inspect once and cache the matching frame plus the first non-ignored caller
		// directly above it so the companion findFirstMethodOutsideOfRestrictedPackage
		// call that immediately follows can read from the thread-local cache instead
		// of walking the stack a second time.
		String[] allowedPackages = getValueFromSettings("allowedListedPackages");
		String[] inspection = inspectCallstackOnce(restrictedPackage, allowedPackages, allowedClasses);
		if (inspection != null) {
			CALLSTACK_INSPECTION_CACHE.set(inspection);
		} else {
			// Avoid leaving a stale per-thread entry behind when there is no violation.
			CALLSTACK_INSPECTION_CACHE.remove();
		}
		return inspection == null ? null : inspection[0];
	}

	/**
	 * Walks the stack once and returns both the first restricted-package frame that
	 * violates the allowed-class list (the violation) and the first non-ignored caller
	 * directly above the first restricted-package frame (the student-called method).
	 * The caller is computed relative to the first restricted frame, independently of
	 * the allowed-class check, so it matches the result of
	 * findFirstMethodOutsideOfRestrictedPackage's standalone walk even when an
	 * allow-listed class lies inside the restricted package. Returns null when no
	 * restricted frame is found, otherwise a two-element array {violationFqn,
	 * callerAboveFqn} (callerAboveFqn may be null when no non-ignored caller exists
	 * above the first restricted frame). Combining the two walks halves the
	 * per-interception stack-walk cost on hot paths.
	 */
	@Nullable
	private static String[] inspectCallstackOnce(String restrictedPackage, String[] allowedPackages, String[] allowedClasses) {
		// Normalise a null settings-derived allow-list to empty so the per-frame
		// allowed-class check below cannot throw an NPE before the security decision.
		@Nonnull
		String[] safeAllowedClasses = allowedClasses == null ? new String[0] : allowedClasses;
		return STACK_WALKER.walk(frames -> {
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			String violation = null;
			boolean restrictedSeen = false;
			String callerAbove = null;
			while (iterator.hasNext()) {
				StackWalker.StackFrame frame = iterator.next();
				String className = frame.getClassName();
				boolean ignorable = false;
				for (@Nonnull
				String ignore : IGNORE_CALLSTACK) {
					if (className.startsWith(ignore)) {
						ignorable = true;
						break;
					}
				}
				if (ignorable) {
					continue;
				}
				boolean inRestricted = className.startsWith(restrictedPackage);
				// First non-ignored frame above the first restricted frame is the
				// student-called method; computed independently of the allowed-class
				// check so it matches findFirstMethodOutsideOfRestrictedPackage.
				if (restrictedSeen && callerAbove == null) {
					callerAbove = className + "." + frame.getMethodName();
				}
				if (!restrictedSeen && inRestricted) {
					restrictedSeen = true;
				}
				// First restricted, non-allowed frame is the violation.
				if (violation == null && inRestricted) {
					boolean allowed = startsWithAny(className, allowedPackages);
						for (@Nonnull
						String allowedClass : safeAllowedClasses) {
							if (className.startsWith(allowedClass)) {
								allowed = true;
								break;
							}
						}
					if (!allowed) {
						violation = className + "." + frame.getMethodName();
					}
				}
				if (violation != null && callerAbove != null) {
					break;
				}
			}
			return violation == null ? null : new String[] { violation, callerAbove };
		});
	}

	private static boolean startsWithAny(String className, String[] prefixes) {
		if (prefixes == null) {
			return false;
		}
		for (String prefix : prefixes) {
			if (prefix != null && className.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Per-thread one-shot cache of the last inspectCallstackOnce result. Populated
	 * by {@link #checkIfCallstackCriteriaIsViolated} and consumed by the
	 * immediately following {@link #findFirstMethodOutsideOfRestrictedPackage} call
	 * so the combined check+caller-lookup costs only one walk instead of two.
	 */
	@Nonnull
	private static final ThreadLocal<String[]> CALLSTACK_INSPECTION_CACHE = new ThreadLocal<>();

	/**
	 * Per-thread re-entrancy guard for the AspectJ advice. The advice body performs
	 * file-system work (path canonicalisation, settings/localisation reads) and
	 * stack walks; under load-time weaving those operations are themselves woven and
	 * re-enter the advice on the same thread. Without a guard the advice recurses on
	 * its own internal operations, producing unbounded redundant work (and, during
	 * JDK class loading, a {@link ClassCircularityError}). {@link #enterAdvice()}
	 * returns {@code false} when the advice is already executing on this thread so the
	 * nested invocation returns immediately; the outermost invocation clears the flag
	 * in a {@code finally} via {@link #exitAdvice()}. Skipping nested invocations is
	 * correct because the advice itself is trusted Ares code, not student code.
	 */
	@Nonnull
	private static final ThreadLocal<Boolean> ADVICE_IN_PROGRESS = ThreadLocal.withInitial(() -> Boolean.FALSE);

	/**
	 * Marks entry into an advice body for the current thread.
	 *
	 * @return {@code true} if this is the outermost (non-re-entrant) invocation and
	 *         the caller should proceed; {@code false} if the advice is already
	 *         executing on this thread and the caller must return immediately.
	 */
	protected static boolean enterAdvice() {
		if (Boolean.TRUE.equals(ADVICE_IN_PROGRESS.get())) {
			return false;
		}
		ADVICE_IN_PROGRESS.set(Boolean.TRUE);
		return true;
	}

	/**
	 * Clears the per-thread advice re-entrancy flag. Must be called from a
	 * {@code finally} paired with an {@link #enterAdvice()} that returned
	 * {@code true}.
	 */
	protected static void exitAdvice() {
		ADVICE_IN_PROGRESS.set(Boolean.FALSE);
	}

	/**
	 * Verifies that {@code value}'s runtime class is JDK-origin before the advice
	 * invokes any overridable method on it. The advice runs inside the re-entrancy
	 * guard ({@link #enterAdvice()}); if it called an overridable method (e.g.
	 * {@code toString()}, {@code intValue()}, {@code iterator()}) on an
	 * attacker-supplied subclass, that student code would execute while the guard is
	 * active and any forbidden operation it performs would be silently skipped. Only
	 * the bootstrap and platform class loaders load JDK classes; student/user code is
	 * loaded by the application class loader (or a child). A JDK-origin instance is
	 * safe to call overridable methods on, and trusting the loader correctly allows
	 * legitimate JDK subclasses (e.g. {@code sun.security.ssl.SSLSocketImpl extends
	 * java.net.Socket}) that an exact-class check would wrongly reject. An untrusted
	 * subtype is treated as a violation (fail-closed) and blocked, because Ares
	 * cannot inspect it without running untrusted code. {@link Class#getClassLoader()}
	 * is final and cannot be spoofed.
	 *
	 * @param value the attacker-controlled argument about to be inspected
	 */
	protected static void requireTrustedRuntimeType(@Nonnull Object value) {
		ClassLoader loader = value.getClass().getClassLoader();
		if (loader != null && loader != ClassLoader.getPlatformClassLoader()) {
			throw new SecurityException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): Ares cannot safely inspect an"
							+ " argument of untrusted type " + value.getClass().getName()
							+ " (not a JDK type) but was blocked by Ares.");
		}
	}

	/**
	 * Finds the caller directly above the first method on the current call stack
	 * that belongs to the given restricted package.
	 * <p>
	 * Description: Iterates the stack trace, skipping frames in
	 * {@link #IGNORE_CALLSTACK}. When the first frame whose class starts with the
	 * provided restricted package is found, this method returns the fully qualified
	 * method name (className.methodName) of the next non-ignored frame above it
	 * (i.e., its caller). Returns null if none is found or if
	 * {@code restrictedPackage} is null.
	 *
	 * @param restrictedPackage the package prefix to search for
	 * @return the fully qualified method name of the caller above the first
	 *         restricted frame, or null if none
	 * @since 2.0.2
	 */
	@Nullable
	static String findFirstMethodOutsideOfRestrictedPackage(@Nullable String restrictedPackage) {
		if (restrictedPackage == null) {
			return null;
		}
		// Fast path: read the cached inspection populated by the preceding
		// checkIfCallstackCriteriaIsViolated call on this thread; clear it after
		// consumption so it cannot leak across unrelated advice invocations.
		String[] cached = CALLSTACK_INSPECTION_CACHE.get();
		if (cached != null) {
			CALLSTACK_INSPECTION_CACHE.remove();
			return cached[1];
		}
		// Slow path: callsite did not run checkIfCallstackCriteriaIsViolated first.
		// Materialise only the non-ignorable frames lazily; stop iterating as soon as
		// we have located the first restricted frame and the first non-ignored caller
		// above it.
		return STACK_WALKER.walk(frames -> {
			boolean restrictedSeen = false;
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				StackWalker.StackFrame frame = iterator.next();
				String className = frame.getClassName();
				boolean ignorable = false;
				for (String ignore : IGNORE_CALLSTACK) {
					if (className.startsWith(ignore)) {
						ignorable = true;
						break;
					}
				}
				if (ignorable) {
					continue;
				}
				if (!restrictedSeen) {
					if (className.startsWith(restrictedPackage)
							&& !className.startsWith("de.tum.cit.ase.ares.api.aop.java.aspectj")) {
						restrictedSeen = true;
					}
					continue;
				}
				return className + "." + frame.getMethodName();
			}
			return null;
		});
	}
	// </editor-fold>

	// <editor-fold desc="Signature handling (AspectJ-only)">

	/**
	 * Formats a join-point signature in the same shape produced by the Byte Buddy /
	 * Instrumentation advice:
	 * {@code <declaringType>.<methodName>(<paramType1>,<paramType2>,...)}.
	 * <p>
	 * AspectJ's {@link org.aspectj.lang.Signature#toLongString()} prepends Java
	 * modifiers (e.g. {@code "public transient "}) and, for constructors, omits
	 * {@code .<init>} altogether
	 * ({@code "java.lang.ProcessBuilder(java.lang.String[])"}). The Instrumentation
	 * side and the JSON expectation files use {@code <init>}-style signatures, so
	 * this helper converts AspectJ's join-point shape to that form.
	 *
	 * @param sig the AspectJ {@link org.aspectj.lang.Signature} from the join point
	 * @return normalized signature string with no leading modifiers and an explicit
	 *         {@code <init>} marker for constructors
	 */
	@Nonnull
	protected static String formatSignature(@Nonnull Signature sig) {
		@Nonnull
		String declaring = sig.getDeclaringTypeName();
		@Nonnull
		String name = sig.getName();
		@Nonnull
		StringBuilder params = new StringBuilder("(");
		if (sig instanceof CodeSignature codeSig) {
			@Nullable
			Class<?>[] parameterTypes = codeSig.getParameterTypes();
			if (parameterTypes != null) {
				for (int i = 0; i < parameterTypes.length; i++) {
					if (i > 0) {
						params.append(",");
					}
					params.append(parameterTypes[i].getName());
				}
			}
		}
		params.append(")");
		return declaring + "." + name + params;
	}

	/**
	 * Extracts the method name without access modifiers from a full method
	 * signature.
	 * <p>
	 * Description: Removes access modifiers (public, private, protected, static,
	 * final, etc.) from the method signature and extracts only the
	 * package.class.method part.
	 *
	 * @param fullMethodSignature the complete method signature from toLongString()
	 * @return the method name without access modifiers (e.g.,
	 *         "java.lang.Runtime.exec")
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	protected static String extractMethodNameWithoutModifiers(@Nonnull String fullMethodSignature) {
		// Split by opening parenthesis to get method declaration part
		String methodDeclaration = fullMethodSignature.split("\\(")[0];

		// Find the last space before the method name to remove access modifiers
		int lastSpaceIndex = methodDeclaration.lastIndexOf(' ');
		if (lastSpaceIndex != -1) {
			return methodDeclaration.substring(lastSpaceIndex + 1);
		}

		// If no space found, return the whole declaration (shouldn't happen in normal
		// cases)
		return methodDeclaration;
	}
	// </editor-fold>

	// <editor-fold desc="Filter variables">

	/**
	 * Filters variables based on the IgnoreValues criteria.
	 * <p>
	 * Description: Returns a new array of variables, excluding those that match the
	 * ignore criteria. If all variables are ignored, returns an empty array. If all
	 * except one variable is ignored, returns an array with only that variable.
	 *
	 * @param variables       the original array of variables
	 * @param ignoreVariables criteria determining which variables to skip
	 * @return a filtered array of variables
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	static Object[] filterVariables(@Nonnull Object[] variables, @Nonnull IgnoreValues ignoreVariables) {
		@Nonnull
		ArrayList<Object> newVariables = new ArrayList<>(Arrays.asList(variables.clone()));
		switch (ignoreVariables.getType()) {
		// No variable is ignored
		case "NONE":
			break;
		// All variables are ignored
		case "ALL":
			newVariables.clear();
			break;
		// All variables except the one at the given index are ignored
		case "ALL_EXCEPT":
			if (ignoreVariables.getIndex() < 0 || ignoreVariables.getIndex() >= newVariables.size()) {
				throw new SecurityException(localize(
						"security.instrumentation.ignore.values.index.invalid", ignoreVariables.getIndex(),
						newVariables.size()));
			}
			@Nonnull
			Object toKeep = newVariables.get(ignoreVariables.getIndex());
			newVariables.clear();
			newVariables.add(toKeep);
			break;
		case "NONE_EXCEPT":
			if (ignoreVariables.getIndex() < 0 || ignoreVariables.getIndex() >= newVariables.size()) {
				throw new SecurityException(localize(
						"security.instrumentation.ignore.values.index.invalid", ignoreVariables.getIndex(),
						newVariables.size()));
			}
			newVariables.remove(ignoreVariables.getIndex());
			break;
		default:
			throw new IllegalArgumentException(
					localize("aop.ignore.unknown.type", ignoreVariables.getType()));
		}
		return newVariables.toArray();
	}
	// </editor-fold>
}
