package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
//</editor-fold>

public abstract class JavaInstrumentationAdviceAbstractToolbox {

	JavaInstrumentationAdviceAbstractToolbox() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationAdviceAbstractToolbox"));
	}

	// <editor-fold desc="Constants">
	@Nonnull
	public static final List<String> IGNORE_CALLSTACK = List.of("java.lang.ClassLoader", "de.tum.cit.ase.ares.api.",
			"com.intellij.rt.debugger.", "jdk.internal.loader.", "jdk.internal.reflect.");

	/**
	 * Cached StackWalker used by the call-stack inspectors below. The default options
	 * (no RETAIN_CLASS_REFERENCE) are deliberately chosen because the inspectors only
	 * read className/methodName strings — keeping Class objects materialized would
	 * pay extra JNI work on every walked frame. The walker avoids the Exception
	 * allocation and full StackTraceElement[] materialization that
	 * Thread.currentThread().getStackTrace() pays on every intercepted JDK call.
	 * Pointcuts on java.io.InputStream.read fire during ObjectInputStream
	 * deserialization of test-result events; the StackWalker path is roughly an
	 * order of magnitude cheaper for those high-frequency probes.
	 */
	@Nonnull
	private static final java.lang.StackWalker STACK_WALKER = java.lang.StackWalker.getInstance();

	/**
	 * Lazily resolved Class&lt;?&gt; reference for the AOP settings holder. Each
	 * intercepted JDK call (e.g. java.io.InputStream.read during ObjectInputStream
	 * deserialization) reads aopMode + restrictedPackage + allowedListedClasses.
	 * Resolving the class via Class.forName on every access (a native call) is
	 * the second-largest contributor to advice overhead after the stack walk; this
	 * cached reference reduces it to a single volatile load.
	 */
	@Nullable
	private static volatile Class<?> SETTINGS_CLASS_CACHE = null;

	/**
	 * Cache of resolved Field handles keyed by field name so getDeclaredField and
	 * setAccessible run once per JVM, not per advice call.
	 */
	@Nonnull
	private static final java.util.concurrent.ConcurrentHashMap<String, Field> SETTINGS_FIELD_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
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
	public static <T> T getValueFromSettings(@Nonnull String fieldName) {
		try {
			Objects.requireNonNull(fieldName, "fieldName must not be null");
			@Nonnull
			Field field = resolveSettingsField(fieldName);
			@Nullable
			T value = (T) field.get(null);
			return value;
		} catch (LinkageError e) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.linkage.exception", fieldName),
					e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.class.not.found.exception", fieldName), e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.no.such.field.exception", fieldName), e);
		} catch (NullPointerException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.null.pointer.exception", fieldName), e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.illegal.access.exception", fieldName), e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.illegal.argument.exception", fieldName), e);
		}
	}

	/**
	 * Resolves the static settings Field for the given name once and caches the
	 * resulting Field handle plus the owning Class&lt;?&gt; reference. The reflective
	 * lookup is the second-largest fixed cost on hot advice paths (called three
	 * times per intercepted JDK call before this cache existed), so caching it on
	 * the bootstrap classloader avoids a Class.forName native call and a
	 * getDeclaredField walk on every interception. The cached field has
	 * setAccessible(true) once and stays accessible for the JVM lifetime.
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
			// Use null as ClassLoader to explicitly load from Bootstrap ClassLoader
			// Use false to avoid class initialization which could trigger file system
			// operations.
			settingsClass = Objects.requireNonNull(
					Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false, null),
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
	public static <T> void setValueToSettings(@Nonnull String fieldName, @Nullable T newValue) {
		try {
			Objects.requireNonNull(fieldName, "fieldName must not be null");
			@Nonnull
			Field field = resolveSettingsField(fieldName);
			field.set(null, newValue);
		} catch (LinkageError e) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.linkage.exception", fieldName),
					e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.class.not.found.exception", fieldName), e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.no.such.field.exception", fieldName), e);
		} catch (NullPointerException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.null.pointer.exception", fieldName), e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.illegal.access.exception", fieldName), e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.illegal.argument.exception", fieldName), e);
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
	public static Object getSettingsLock() {
		try {
			// Use null as ClassLoader to explicitly load from Bootstrap ClassLoader
			// Use false to avoid class initialization which could trigger file system
			// operations
			@Nonnull
			Class<?> adviceSettingsClass = Objects.requireNonNull(
					Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false, null),
					"adviceSettingsClass must not be null");
			@Nonnull
			java.lang.reflect.Method getLockMethod = adviceSettingsClass.getDeclaredMethod("getSettingsLock");
			@Nonnull
			Object lock = Objects.requireNonNull(getLockMethod.invoke(null), "lock must not be null");
			return lock;
		} catch (Exception e) {
			// Fallback to class object if lock retrieval fails
			try {
				return Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false, null);
			} catch (ClassNotFoundException ex) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
						.localize("security.advice.class.not.found.exception", "JavaAOPTestCaseSettings"), ex);
			}
		}
	}

	/**
	 * Decrements the value at a specified index in an integer array setting.
	 * <p>
	 * Description: Retrieves an integer array from settings, decrements the value
	 * at the given position, and updates the array back to the settings class. This
	 * operation is synchronized to prevent race conditions.
	 *
	 * @param settingsArray the name of the array field in settings
	 * @param position      the index position of the value to decrement
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static void decrementSettingsArrayValue(@Nonnull String settingsArray, int position) {
		synchronized (getSettingsLock()) {
			@Nullable
			int[] array = getValueFromSettings(settingsArray);
			if (array != null && position >= 0 && position < array.length) {
				@Nonnull
				int[] clone = array.clone();
				clone[position]--;
				setValueToSettings(settingsArray, clone);
			}
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
	public static boolean checkAndDecrementSettingsArrayValue(@Nonnull String settingsArray, int position) {
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
	 * Retrieves a localized message based on a key and optional arguments.
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
				throw new SecurityException(
						JavaInstrumentationAdviceAbstractToolbox.localize("security.localization.method.return.type"));
			}
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
				| IllegalAccessException e) {
			// Fallback: Return the key if localization fails
			return key;
		}
	}
	// </editor-fold>

	// <editor-fold desc="Callstack criteria methods">

	/**
	 * Determines if a call stack element is in the allow list.
	 * <p>
	 * Description: Checks whether the class name of the provided stack trace
	 * element starts with any of the allowed class name prefixes.
	 *
	 * @param allowedClasses the array of allowed class name prefixes
	 * @param elementToCheck the stack trace element to check
	 * @return true if the element is allowed, false otherwise
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static boolean checkIfCallstackElementIsAllowed(@Nonnull String[] allowedClasses,
			@Nonnull StackTraceElement elementToCheck) {
		String className = elementToCheck.getClassName();
		for (@Nonnull
		String allowedClass : allowedClasses) {
			if (className.startsWith(allowedClass)) {
				return true;
			}
		}
		return false;
	}

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
	public static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses,
			String declaringTypeName, String methodName) {
		// Stream the stack lazily so the walk stops at the first restricted frame.
		// Skip ignorable frames (Ares internals, class loading, reflection trampolines)
		// to prevent reflective dispatch from bypassing the check simply because
		// jdk.internal.reflect.* appears as an intermediate caller.
		// Inspect once and cache the matching frame plus the first non-ignored caller
		// directly above it so the companion findFirstMethodOutsideOfRestrictedPackage
		// call that immediately follows can read from the thread-local cache instead
		// of walking the stack a second time.
		String[] inspection = inspectCallstackOnce(restrictedPackage, allowedClasses);
		CALLSTACK_INSPECTION_CACHE.set(inspection);
		return inspection == null ? null : inspection[0];
	}

	/**
	 * Walks the stack once and returns both the first restricted-package frame that
	 * violates the allowed-class list and the first non-ignored caller above it.
	 * Returns null when no restricted frame is found, otherwise a two-element array
	 * {violationFqn, callerAboveFqn} (callerAboveFqn may be null when the violating
	 * frame is at the top of the stack). Combining the two walks halves the
	 * per-interception stack-walk cost on hot paths such as ObjectInputStream
	 * deserialization that drive thousands of intercepted reads.
	 */
	@Nullable
	private static String[] inspectCallstackOnce(String restrictedPackage, String[] allowedClasses) {
		return STACK_WALKER.walk(frames -> {
			java.util.Iterator<java.lang.StackWalker.StackFrame> iterator = frames.iterator();
			String violation = null;
			while (iterator.hasNext()) {
				java.lang.StackWalker.StackFrame frame = iterator.next();
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
				if (violation == null) {
					if (!className.startsWith(restrictedPackage)) {
						continue;
					}
					boolean allowed = false;
					for (@Nonnull
					String allowedClass : allowedClasses) {
						if (className.startsWith(allowedClass)) {
							allowed = true;
							break;
						}
					}
					if (allowed) {
						continue;
					}
					violation = className + "." + frame.getMethodName();
					continue;
				}
				return new String[] { violation, className + "." + frame.getMethodName() };
			}
			return violation == null ? null : new String[] { violation, null };
		});
	}

	/**
	 * Per-thread one-shot cache of the last inspectCallstackOnce result. Populated
	 * by {@link #checkIfCallstackCriteriaIsViolated} and consumed by the immediately
	 * following {@link #findFirstMethodOutsideOfRestrictedPackage} call so the
	 * combined check+caller-lookup costs only one walk instead of two.
	 */
	@Nonnull
	private static final ThreadLocal<String[]> CALLSTACK_INSPECTION_CACHE = new ThreadLocal<>();

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
	public static String findFirstMethodOutsideOfRestrictedPackage(@Nullable String restrictedPackage) {
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
		// Slow path: callsite did not run checkIfCallstackCriteriaIsViolated first
		// (e.g. AspectJ definitions hit only this method). Materialize only the
		// non-ignorable frames lazily; stop iterating as soon as we have located the
		// first restricted frame and its caller above it.
		return STACK_WALKER.walk(frames -> {
			boolean restrictedSeen = false;
			java.util.Iterator<java.lang.StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				java.lang.StackWalker.StackFrame frame = iterator.next();
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
							&& !className.startsWith("de.tum.cit.ase.ares.api.aop.java.instrumentation")) {
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

	// <editor-fold desc="Field index lookup">

	/**
	 * Resolves the index of a named field within the given class.
	 * <p>
	 * Description: Returns the positional index of the first field whose name
	 * matches {@code fieldName}. Used to avoid hard-coding field indices that can
	 * shift across JDK versions (e.g. JDK 21 added a LOGGER field to
	 * {@link ProcessBuilder}).
	 *
	 * @param clazz     the class whose declared fields are inspected
	 * @param fieldName the name of the field to locate
	 * @return the zero-based index of the matching field
	 * @throws SecurityException if no field with that name exists
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static int findFieldIndex(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i].getName())) {
				return i;
			}
		}
		throw new SecurityException(
				JavaInstrumentationAdviceAbstractToolbox.localize("security.instrumentation.field.not.found", fieldName, clazz.getName()));
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
	public static Object[] filterVariables(@Nonnull Object[] variables, @Nonnull IgnoreValues ignoreVariables) {
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
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
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
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
						"security.instrumentation.ignore.values.index.invalid", ignoreVariables.getIndex(),
						newVariables.size()));
			}
			newVariables.remove(ignoreVariables.getIndex());
			break;
		default:
			throw new IllegalArgumentException(JavaInstrumentationAdviceAbstractToolbox
					.localize("aop.ignore.unknown.type", ignoreVariables.getType()));
		}
		return newVariables.toArray();
	}
	// </editor-fold>
}
