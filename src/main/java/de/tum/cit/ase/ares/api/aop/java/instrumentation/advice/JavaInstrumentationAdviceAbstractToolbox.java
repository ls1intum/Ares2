package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

//<editor-fold desc="imports">

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
//</editor-fold>

public abstract class JavaInstrumentationAdviceAbstractToolbox {

	JavaInstrumentationAdviceAbstractToolbox() {
		throw new SecurityException(localize("security.instrumentation.utility.initialization",
				"JavaInstrumentationAdviceAbstractToolbox"));
	}

	// <editor-fold desc="Constants">
	@Nonnull
	public static final List<String> IGNORE_CALLSTACK = List.of("java.lang.ClassLoader", "de.tum.cit.ase.ares.api.",
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
	 * A stack walker that also reports each frame's class, so a trusted-looking
	 * frame can be checked for having been loaded by the JVM itself. Kept apart
	 * from {@link #STACK_WALKER} because this is slower and needed only for the
	 * random-seed, temp-file and Windows shell-folder exemptions.
	 */
	@Nonnull
	private static final StackWalker STACK_WALKER_WITH_CLASS_REFERENCE = StackWalker
			.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));

	/**
	 * The feature release of the running JDK, such as 21, read once at start-up.
	 */
	private static final int RUNNING_FEATURE_RELEASE = Runtime.version().feature();

	/**
	 * The JDK feature releases whose Windows shell-folder code was audited for the
	 * hand-off exemption, together with {@link #AUDITED_SHELL_FOLDER_PRODUCERS}.
	 */
	@Nonnull
	private static final Set<Integer> SHELL_FOLDER_AUDITED_FEATURE_RELEASES = Set.of(17, 21, 25);

	/**
	 * The JDK's private executor that runs Windows shell-folder tasks on its own
	 * COM thread and waits for the result.
	 */
	@Nonnull
	private static final String SHELL_FOLDER_COM_INVOKER = "sun.awt.shell.Win32ShellFolderManager2$ComInvoker";

	/**
	 * The JDK class whose {@code invoke} methods pass a shell-folder task on to
	 * {@link #SHELL_FOLDER_COM_INVOKER}.
	 */
	@Nonnull
	private static final String SHELL_FOLDER = "sun.awt.shell.ShellFolder";

	/**
	 * The JDK's Windows shell-folder class, which builds the tasks handed to the
	 * COM thread.
	 */
	@Nonnull
	private static final String WIN32_SHELL_FOLDER = "sun.awt.shell.Win32ShellFolder2";

	/**
	 * The methods of {@link #WIN32_SHELL_FOLDER} whose tasks were audited to use
	 * only JDK objects, strings and numbers. The sorting methods are left out on
	 * purpose: their tasks call methods of files a student can hand in.
	 */
	@Nonnull
	private static final Set<String> AUDITED_SHELL_FOLDER_PRODUCERS = Set.of("<init>", "getFileSystemPath",
			"parseDisplayName", "createShellFolder", "getIShellFolder", "hasAttribute", "pidlsEqual", "listFiles",
			"getChildByPath", "getLinkLocation", "getLibraries");

	/**
	 * The JDK class whose file methods in {@link #CHECKED_FILE_SYSTEM_VIEW_ENTRIES}
	 * Ares already checks as a read of their folder.
	 */
	@Nonnull
	private static final String FILE_SYSTEM_VIEW = "javax.swing.filechooser.FileSystemView";

	/**
	 * The methods of {@link #FILE_SYSTEM_VIEW} whose folder argument Ares checks
	 * before the JDK reaches its shell folders.
	 */
	@Nonnull
	private static final Set<String> CHECKED_FILE_SYSTEM_VIEW_ENTRIES = Set.of("getFiles", "getChild", "isParent");

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
	 * The policy rule lists of each resource type that has more than one action,
	 * read only to choose the reason a denial message gives. Commands and threads
	 * are missing on purpose because each has one rule list.
	 */
	@Nonnull
	private static final Map<String, List<String>> RULE_SETTINGS_BY_RESOURCE_TYPE = Map.of("file",
			List.of("pathsAllowedToBeRead", "pathsAllowedToBeOverwritten", "pathsAllowedToBeCreated",
					"pathsAllowedToBeExecuted", "pathsAllowedToBeDeleted"),
			"network",
			List.of("hostsAllowedToBeConnectedTo", "hostsAllowedToBeSentTo", "hostsAllowedToBeReceivedFrom"));

	/**
	 * The primitive element types of JVM array names, such as {@code I} in
	 * {@code [I}, mapped to their Java source names for denial messages.
	 */
	@Nonnull
	private static final Map<String, String> PRIMITIVE_DESCRIPTORS = Map.of("Z", "boolean", "B", "byte", "C", "char",
			"S", "short", "I", "int", "J", "long", "F", "float", "D", "double");

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
			throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
		} catch (NullPointerException e) {
			throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
		}
	}

	/**
	 * Resolves the static settings Field for the given name once and caches the
	 * resulting Field handle plus the owning Class&lt;?&gt; reference, so the
	 * reflective lookup (Class.forName + getDeclaredField + setAccessible) runs
	 * once per JVM rather than on every advice call. The cached field stays
	 * accessible for the JVM lifetime.
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
			// Use false to avoid class initialisation which could trigger file system
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
	// Private (was public): the only caller is checkAndDecrementSettingsArrayValue
	// in
	// this class, so the write entry point into the security settings is not
	// reachable
	// from any other class. (Defence in depth only; the authoritative protection
	// against student tampering is the architecture-analysis layer, which forbids
	// student references to these classes.)
	private static <T> void setValueToSettings(@Nonnull String fieldName, @Nullable T newValue) {
		try {
			Objects.requireNonNull(fieldName, "fieldName must not be null");
			@Nonnull
			Field field = resolveSettingsField(fieldName);
			field.set(null, newValue);
		} catch (LinkageError e) {
			throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
		} catch (NoSuchFieldException e) {
			throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
		} catch (NullPointerException e) {
			throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
		} catch (InaccessibleObjectException e) {
			throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
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
			// Use null as ClassLoader to explicitly load from Bootstrap ClassLoader
			// Use false to avoid class initialisation which could trigger file system
			// operations
			@Nonnull
			Class<?> adviceSettingsClass = Objects.requireNonNull(
					Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false, null),
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
			throw new SecurityException(
					localize("security.advice.class.not.found.exception", "JavaAOPTestCaseSettings"), e);
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
	 * Description: Attempts to fetch a localised string from the Messages class
	 * using reflection. Falls back to the key if localisation fails.
	 *
	 * @param key  the localisation key identifying the message
	 * @param args optional arguments to format the localised message
	 * @return the localised message string, or the key itself if localisation fails
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
			// Fallback: Return the key if localisation fails
			return key;
		}
	}

	/**
	 * Says why an access was denied, as a lower-case clause in the active language
	 * that the denial sentence ends with. It tells apart three cases: the policy
	 * has no rule of this resource type at all, it has rules of this type but none
	 * for this action, or it has a rule for this action that does not cover the
	 * target. Only the message uses this; the decision is already made.
	 *
	 * @param resourceType         {@code file}, {@code command}, {@code network} or
	 *                             {@code thread}
	 * @param action               the raw action name that was checked
	 * @param noAllowRuleForAction {@code true} if the policy allows this action for
	 *                             no target at all
	 * @return the reason clause, without a final full stop
	 */
	@Nonnull
	static String buildDenialReason(@Nonnull String resourceType, @Nonnull String action,
			boolean noAllowRuleForAction) {
		if (getValueFromSettings("supervisionEndedOnThread") != null) {
			return localize("security.advice.denial.reason.test.ended");
		}
		if (!noAllowRuleForAction) {
			return localize("security.advice.denial.reason.target.not.allowed." + resourceType);
		}
		if (hasAnyRuleForResourceType(resourceType)) {
			return localize("security.advice.denial.reason.no.action.rule." + resourceType, localizeAction(action));
		}
		return localize("security.advice.denial.reason.no.rule." + resourceType);
	}

	/**
	 * Tells whether the policy allows any action at all for a resource type that
	 * has more than one action, files and network access. Commands and threads have
	 * one rule list each, so for them this is always {@code false}, which makes "no
	 * rule for this action" read as "no rule for this type".
	 *
	 * @param resourceType the resource type of the denied access
	 * @return {@code true} if at least one rule list of that type is not empty
	 */
	private static boolean hasAnyRuleForResourceType(@Nonnull String resourceType) {
		for (@Nonnull
		String settingsField : RULE_SETTINGS_BY_RESOURCE_TYPE.getOrDefault(resourceType, List.of())) {
			@Nullable
			String[] rules = getValueFromSettings(settingsField);
			if (rules != null && rules.length > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Writes a method signature for a denial message as Java source would: array
	 * types such as {@code [Ljava.lang.String;} become {@code java.lang.String[]},
	 * and the parameter types are separated by a comma and one space. A text with
	 * no parameter list is returned unchanged. Only the message uses this.
	 *
	 * @param signature the signature as the advice sees it
	 * @return the signature in Java source notation
	 */
	@Nonnull
	static String describeSignature(@Nonnull String signature) {
		int open = signature.indexOf('(');
		int close = signature.lastIndexOf(')');
		if (open < 0 || close < open) {
			return signature;
		}
		@Nonnull
		String parameters = signature.substring(open + 1, close);
		@Nonnull
		StringJoiner sourceParameters = new StringJoiner(", ");
		if (!parameters.isBlank()) {
			for (@Nonnull
			String parameter : parameters.split(",")) {
				sourceParameters.add(describeTypeName(parameter.trim()));
			}
		}
		return signature.substring(0, open + 1) + sourceParameters + signature.substring(close);
	}

	/**
	 * Writes one type name as Java source would, turning a JVM array name such as
	 * {@code [[I} into {@code int[][]}. Any other name is returned unchanged.
	 *
	 * @param typeName the type name as {@link Class#getName()} returns it
	 * @return the type name in Java source notation
	 */
	@Nonnull
	static String describeTypeName(@Nonnull String typeName) {
		int dimensions = 0;
		while (dimensions < typeName.length() && typeName.charAt(dimensions) == '[') {
			dimensions++;
		}
		if (dimensions == 0) {
			return typeName;
		}
		@Nonnull
		String element = typeName.substring(dimensions);
		@Nullable
		String elementType = element.length() > 2 && element.charAt(0) == 'L' && element.endsWith(";")
				? element.substring(1, element.length() - 1)
				: PRIMITIVE_DESCRIPTORS.get(element);
		return elementType == null ? typeName : elementType + "[]".repeat(dimensions);
	}

	/**
	 * Translates an action name such as "read" into the active language, for a
	 * violation message only. Enforcement keeps comparing the raw name, so this
	 * must never feed a decision. An unknown action is returned unchanged.
	 *
	 * @param action the raw action name used by enforcement
	 * @return the translated action, or the raw name when no translation exists
	 */
	@Nonnull
	static String localizeAction(@Nonnull String action) {
		return switch (action) {
		case "read", "overwrite", "create", "delete", "execute", "connect", "send", "receive", "manipulate" -> localize(
				"security.advice.action." + action);
		default -> action;
		};
	}

	/**
	 * Names the student method that called the blocked operation, in the active
	 * language, for appending to a violation message.
	 *
	 * @param studentCalledMethod the calling student method, or {@code null} if
	 *                            unknown
	 * @return the caller note with a leading space, or an empty text when unknown
	 */
	@Nonnull
	static String describeCaller(@Nullable String studentCalledMethod) {
		if (studentCalledMethod == null) {
			return "";
		}
		return " " + localize("security.advice.called.by", studentCalledMethod);
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
				if (className.startsWith("jdk.internal.loader.") || "java.lang.ClassLoader".equals(className)
						|| "java.security.SecureClassLoader".equals(className)
						|| "java.net.URLClassLoader".equals(className)) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Tells whether a JDK seed implementation is acting without an intervening
	 * student callback. Other security providers can invoke student code, so their
	 * frames cannot grant the entropy-device exemption.
	 *
	 * @return {@code true} if the JDK is seeding a random-number generator
	 */
	static boolean isSecureRandomSeedingInProgress() {
		String restrictedPackage = getValueFromSettings("restrictedPackage");
		return STACK_WALKER_WITH_CLASS_REFERENCE.walk(frames -> {
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				StackWalker.StackFrame frame = iterator.next();
				String className = frame.getClassName();
				if (isStudentFrame(className, restrictedPackage)) {
					return Boolean.FALSE;
				}
				if ((className.startsWith("sun.security.provider.NativePRNG$")
						|| className.startsWith("sun.security.provider.SeedGenerator$"))
						&& frame.getDeclaringClass().getClassLoader() == null) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Tells whether the JDK's own temp-file code
	 * ({@code java.nio.file.TempFileHelper}, loaded by the JVM itself) is on the
	 * call stack with no student code between it and this check. Student code
	 * further down, the caller of the temp-file method, is expected; student code
	 * above it means a callback is acting.
	 *
	 * @param restrictedPackage the package holding the student code
	 * @return {@code true} if the JDK's temp-file code itself is acting
	 */
	static boolean isJdkTempFileHelperCallingWithoutStudentCode(@Nullable String restrictedPackage) {
		return STACK_WALKER_WITH_CLASS_REFERENCE.walk(frames -> {
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				StackWalker.StackFrame frame = iterator.next();
				String className = frame.getClassName();
				if ("java.nio.file.TempFileHelper".equals(className)
						&& frame.getDeclaringClass().getClassLoader() == null) {
					return Boolean.TRUE;
				}
				if (isStudentFrame(className, restrictedPackage)) {
					return Boolean.FALSE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Names the JDK module of a class the JVM's own class loaders defined.
	 *
	 * @param type the class of a frame
	 * @return the module name, or {@code null} for a class outside the JDK
	 */
	@Nullable
	static String jdkModuleName(@Nonnull Class<?> type) {
		ClassLoader loader = type.getClassLoader();
		String moduleName = type.getModule().getName();
		boolean jdkLoader = loader == null || loader == ClassLoader.getPlatformClassLoader();
		return jdkLoader && moduleName != null && (moduleName.startsWith("java.") || moduleName.startsWith("jdk."))
				? moduleName
				: null;
	}

	/**
	 * Tells whether the JDK on Windows is handing one of its own shell-folder tasks
	 * to its COM thread, for a {@code FileSystemView} method Ares already checks as
	 * a file read, with only JDK code on the way. The thread check lets exactly
	 * that hand-off pass, because the student starts no thread of their own.
	 *
	 * @return {@code true} for such a hand-off
	 */
	static boolean isJdkShellFolderHandOffWithoutStudentCode() {
		return STACK_WALKER_WITH_CLASS_REFERENCE
				.walk(frames -> Boolean.valueOf(isJdkShellFolderHandOff(frames.iterator(), RUNNING_FEATURE_RELEASE)));
	}

	/**
	 * Reads a call stack, newest frame first, as that hand-off: JDK frames to
	 * {@code ComInvoker.invoke}, then {@code ShellFolder.invoke}, one audited
	 * {@code Win32ShellFolder2} method and JDK frames to a checked
	 * {@code FileSystemView} method. A frame before it that the JVM did not load
	 * itself means {@code false}.
	 *
	 * @param frames         the stack, newest first
	 * @param featureRelease the JDK's feature release
	 * @return {@code true} for exactly that shape
	 */
	static boolean isJdkShellFolderHandOff(@Nonnull Iterator<StackWalker.StackFrame> frames, int featureRelease) {
		if (!SHELL_FOLDER_AUDITED_FEATURE_RELEASES.contains(featureRelease)
				|| nextJvmFrameMatching(frames, frame -> isMethod(frame, SHELL_FOLDER_COM_INVOKER, "invoke")) == null) {
			return false;
		}
		StackWalker.StackFrame forwarder = nextJvmFrame(frames);
		if (forwarder == null || !isMethod(forwarder, SHELL_FOLDER, "invoke")) {
			return false;
		}
		StackWalker.StackFrame producer = nextJvmFrameMatching(frames,
				frame -> !isMethod(frame, SHELL_FOLDER, "invoke"));
		if (producer == null || !WIN32_SHELL_FOLDER.equals(producer.getClassName())
				|| !AUDITED_SHELL_FOLDER_PRODUCERS.contains(producer.getMethodName())) {
			return false;
		}
		return nextJvmFrameMatching(frames, frame -> FILE_SYSTEM_VIEW.equals(frame.getClassName())
				&& CHECKED_FILE_SYSTEM_VIEW_ENTRIES.contains(frame.getMethodName())) != null;
	}

	/**
	 * Takes the next frame of a call stack if the JVM itself loaded its class.
	 *
	 * @param frames the rest of the call stack
	 * @return that frame, or {@code null} at the end of the stack or for a frame of
	 *         any other class loader
	 */
	@Nullable
	private static StackWalker.StackFrame nextJvmFrame(@Nonnull Iterator<StackWalker.StackFrame> frames) {
		if (!frames.hasNext()) {
			return null;
		}
		StackWalker.StackFrame frame = frames.next();
		return frame.getDeclaringClass().getClassLoader() == null ? frame : null;
	}

	/**
	 * Takes frames loaded by the JVM itself until one is wanted.
	 *
	 * @param frames the rest of the call stack
	 * @param wanted the frame to stop at
	 * @return the wanted frame, or {@code null} if the stack ends or a frame of
	 *         another class loader comes first
	 */
	@Nullable
	private static StackWalker.StackFrame nextJvmFrameMatching(@Nonnull Iterator<StackWalker.StackFrame> frames,
			@Nonnull Predicate<StackWalker.StackFrame> wanted) {
		StackWalker.StackFrame frame = nextJvmFrame(frames);
		while (frame != null && !wanted.test(frame)) {
			frame = nextJvmFrame(frames);
		}
		return frame;
	}

	/**
	 * Tells whether a frame runs a given method of a given class.
	 *
	 * @param frame      the frame
	 * @param className  the class, by its binary name
	 * @param methodName the method
	 * @return {@code true} for exactly that method
	 */
	private static boolean isMethod(@Nonnull StackWalker.StackFrame frame, @Nonnull String className,
			@Nonnull String methodName) {
		return className.equals(frame.getClassName()) && methodName.equals(frame.getMethodName());
	}

	/**
	 * Tells whether a frame's class belongs to the student code: inside the
	 * restricted package and not one of the trusted prefixes in
	 * {@link #IGNORE_CALLSTACK}.
	 *
	 * @param className         the frame's class
	 * @param restrictedPackage the package holding the student code
	 * @return {@code true} for a student frame
	 */
	private static boolean isStudentFrame(@Nonnull String className, @Nullable String restrictedPackage) {
		if (restrictedPackage == null || restrictedPackage.isBlank() || !className.startsWith(restrictedPackage)) {
			return false;
		}
		for (String trustedPrefix : IGNORE_CALLSTACK) {
			if (className.startsWith(trustedPrefix)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether Ares itself reads one of its support files through a trusted utility
	 * such as {@code FileTools}, for structural and architecture setup. See
	 * {@link #isTrustedUtilityReadForAres} for the rule.
	 *
	 * @return {@code true} if an Ares utility performs the access for Ares
	 */
	static boolean isProjectSourcesFinderInProgress() {
		@Nullable
		String restrictedPackage = getValueFromSettings("restrictedPackage");
		return STACK_WALKER.walk(frames -> Boolean.valueOf(isTrustedUtilityReadForAres(
				frames.map(StackWalker.StackFrame::getClassName).iterator(), restrictedPackage)));
	}

	/**
	 * Reads a call stack, newest frame first, as a read by a trusted Ares utility
	 * that Ares code called: the first frame after the utility frames must be Ares
	 * code. A student frame before any utility frame means the student is reading,
	 * so the walk stops there and its cost does not grow with the depth of a
	 * student recursion.
	 *
	 * @param classNames        the class of each frame, newest first
	 * @param restrictedPackage the package holding the student code
	 * @return {@code true} for such a read
	 */
	static boolean isTrustedUtilityReadForAres(@Nonnull Iterator<String> classNames,
			@Nullable String restrictedPackage) {
		boolean trustedSeen = false;
		while (classNames.hasNext()) {
			String className = classNames.next();
			if (isTrustedSetupUtility(className)) {
				trustedSeen = true;
			} else if (trustedSeen) {
				return className.startsWith("de.tum.cit.ase.ares.api.");
			} else if (isStudentFrame(className, restrictedPackage)) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Returns whether the current thread is resolving bytecode for an active Byte
	 * Buddy class transformation.
	 * <p>
	 * Byte Buddy's class-file locator obtains application bytecode through
	 * {@code ClassLoader.getResourceAsStream}, which reaches
	 * {@code URL.openStream}. Treating that trusted lookup as a student network
	 * connection aborts the transformer and lets the original, unprotected class
	 * load. The executing transformer frame is a stronger signal than a generic
	 * class-loader frame: it cannot occur around an ordinary student-initiated URL
	 * access.
	 *
	 * @return {@code true} while Byte Buddy is executing a class transformer
	 */
	static boolean isByteBuddyTransformationInProgress() {
		return STACK_WALKER
				.walk(frames -> frames.map(StackWalker.StackFrame::getClassName).anyMatch(className -> className
						.startsWith("net.bytebuddy.agent.builder.AgentBuilder$Default$ExecutingTransformer")));
	}

	/**
	 * Returns true when {@code className} is one of Ares' trusted setup utilities
	 * that legitimately read framework support files during structural and
	 * architecture test setup.
	 */
	private static boolean isTrustedSetupUtility(@Nonnull String className) {
		return "de.tum.cit.ase.ares.api.util.ProjectSourcesFinder".equals(className)
				|| "de.tum.cit.ase.ares.api.structural.testutils.ClassNameScanner".equals(className)
				|| "de.tum.cit.ase.ares.api.util.FileTools".equals(className)
				|| "de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder".equals(className);
	}
	// </editor-fold>

	// <editor-fold desc="Callstack criteria methods">

	/**
	 * Finds the first student frame on the call stack that is not an allowed class,
	 * skipping the frames in {@link #IGNORE_CALLSTACK}. Nothing is checked on a
	 * thread {@link #isUncheckedOutsideASupervisedTest} lets pass.
	 *
	 * @param restrictedPackage the package holding the student code
	 * @param allowedClasses    the classes allowed by exact name
	 * @param declaringTypeName the class of the intercepted method
	 * @param methodName        the intercepted method
	 * @return the student method found, or {@code null} for none
	 */
	@Nullable
	static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses,
			String declaringTypeName, String methodName) {
		if (isUncheckedOutsideASupervisedTest()) {
			CALLSTACK_INSPECTION_CACHE.remove();
			return null;
		}
		String[] allowedPackages = getValueFromSettings("allowedListedPackages");
		String[] inspection = inspectCallstackOnce(restrictedPackage, allowedPackages, allowedClasses);
		if (inspection != null) {
			CALLSTACK_INSPECTION_CACHE.set(inspection);
		} else {
			CALLSTACK_INSPECTION_CACHE.remove();
		}
		return inspection == null ? null : inspection[0];
	}

	/**
	 * Whether the current thread goes unchecked outside a supervised test: every
	 * thread while Ares guards nothing, such as during a deactivated policy, and
	 * the thread that ended the last test, which runs JUnit and Ares, until the
	 * next test is armed. Every other thread stays checked.
	 *
	 * @return {@code true} if the current thread is not checked
	 */
	static boolean isUncheckedOutsideASupervisedTest() {
		return Boolean.TRUE.equals(getValueFromSettings("supervisionSuspended"))
				|| getValueFromSettings("supervisionEndedOnThread") == Thread.currentThread();
	}

	/**
	 * Walks the stack once and returns both the first restricted-package frame that
	 * violates the allowed-class list (the violation) and the first non-ignored
	 * caller directly above the first restricted-package frame (the student-called
	 * method). The caller is computed relative to the first restricted frame,
	 * independently of the allowed-class check, so it matches the result of
	 * findFirstMethodOutsideOfRestrictedPackage's standalone walk even when an
	 * allow-listed class lies inside the restricted package. Returns null when no
	 * restricted frame is found, otherwise a two-element array {violationFqn,
	 * callerAboveFqn} (callerAboveFqn may be null when no non-ignored caller exists
	 * above the first restricted frame). Combining the two walks halves the
	 * per-interception stack-walk cost on hot paths.
	 */
	@Nullable
	private static String[] inspectCallstackOnce(String restrictedPackage, String[] allowedPackages,
			String[] allowedClasses) {
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
					// allowedClasses may be null when the per-test settings are only
					// partially armed: the volatile fields in JavaAOPTestCaseSettings are
					// written one-by-one, so a class woven on another thread can observe
					// aopMode/restrictedPackage already set while allowedListedClasses is
					// still null. Treat null as "nothing allow-listed" (identical to an
					// empty array), mirroring the null-tolerant allowedPaths handling in
					// JavaInstrumentationAdviceFileSystemToolbox#checkFileSystemInteractionForAction.
					if (allowedClasses != null) {
						for (@Nonnull
						String allowedClass : allowedClasses) {
							if (className.equals(allowedClass)) {
								allowed = true;
								break;
							}
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
			// '.'-boundary match: an allowed package "com.foo" must not also match the
			// unrelated sibling package "com.foobar" (I-105). Allowed classes,
			// by contrast, match by exact name only, as ClassPermission.isAllowedClass
			// does against the trusted nest listing.
			if (prefix != null && (className.equals(prefix) || className.startsWith(prefix + "."))) {
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
	 * Per-thread re-entrancy guard for the instrumentation advice. The advice's own
	 * body performs file-system work (reading settings, localisation bundles,
	 * building messages) and walks the stack, which can lazily load JDK classes
	 * such as {@code sun.nio.fs.UnixException}. Loading those classes re-enters the
	 * file-system advice on the same thread; without a guard that re-entry runs the
	 * full check again while the class is still being defined, producing a
	 * {@link ClassCircularityError}. {@link #enterAdvice()} returns {@code false}
	 * when the advice is already executing on this thread so the nested invocation
	 * returns immediately; the outermost invocation always clears the flag in a
	 * {@code finally} via {@link #exitAdvice()}. Skipping nested invocations is
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
	static boolean enterAdvice() {
		if (Boolean.TRUE.equals(ADVICE_IN_PROGRESS.get())) {
			return false;
		}
		ADVICE_IN_PROGRESS.set(Boolean.TRUE);
		return true;
	}

	/**
	 * Clears the per-thread advice re-entrancy flag. Must be called from a
	 * {@code finally} block paired with a {@link #enterAdvice()} that returned
	 * {@code true}.
	 */
	static void exitAdvice() {
		ADVICE_IN_PROGRESS.set(Boolean.FALSE);
	}

	/**
	 * Verifies that {@code value} originates from the JDK before the advice invokes
	 * any overridable method on it. The check is loader-based: a class loaded by
	 * the bootstrap or platform class loader can only be a JDK class, and
	 * {@link Class#getClassLoader()} is {@code final} and cannot be spoofed, so
	 * this is a reliable trust check. It therefore also trusts legitimate JDK
	 * subclasses, which an exact-class comparison would wrongly reject.
	 * <p>
	 * The advice runs inside the re-entrancy guard ({@link #enterAdvice()}); if it
	 * invoked an overridable method (e.g. {@code toString()}, {@code intValue()})
	 * on an attacker-supplied subclass, that student code would execute while the
	 * guard is active and any forbidden operation it performs would be silently
	 * skipped. To keep that impossible, a value from any other class loader is
	 * treated as a violation (fail-closed) and blocked, because Ares cannot inspect
	 * it without running untrusted code.
	 *
	 * @param value the attacker-controlled argument about to be inspected
	 */
	static void requireTrustedRuntimeType(@Nonnull Object value) {
		ClassLoader loader = value.getClass().getClassLoader();
		// Bootstrap (null) and platform class loaders only ever load JDK classes;
		// user and student code is loaded by the application/system class loader,
		// or by a child of it. A JDK-origin instance is safe to call overridable
		// methods on, and this also trusts legitimate JDK subclasses that an
		// exact-class check would wrongly reject, such as SSLSocketImpl extending
		// java.net.Socket or the zipfs paths. Class.getClassLoader() is final and
		// cannot be spoofed.
		if (loader != null && loader != ClassLoader.getPlatformClassLoader()) {
			throw new SecurityException(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): The supervised code passed an"
							+ " argument of the type " + describeTypeName(value.getClass().getName())
							+ ", which is not a JDK type, so Ares cannot inspect it safely and blocked the call.");
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
			String callerAboveRestrictedPackage = null;
			Iterator<StackWalker.StackFrame> iterator = frames.iterator();
			while (callerAboveRestrictedPackage == null && iterator.hasNext()) {
				StackWalker.StackFrame frame = iterator.next();
				String className = frame.getClassName();
				boolean ignorable = false;
				for (String ignore : IGNORE_CALLSTACK) {
					if (className.startsWith(ignore)) {
						ignorable = true;
						break;
					}
				}
				if (!ignorable) {
					if (restrictedSeen) {
						callerAboveRestrictedPackage = className + "." + frame.getMethodName();
					} else if (className.startsWith(restrictedPackage)
							&& !className.startsWith("de.tum.cit.ase.ares.api.aop.java.instrumentation")) {
						restrictedSeen = true;
					}
				}
			}
			return callerAboveRestrictedPackage;
		});
	}
	// </editor-fold>

	// <editor-fold desc="Field index lookup (Instrumentation-only)">

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
	static int findFieldIndex(@Nonnull Class<?> clazz, @Nonnull String fieldName) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fieldName.equals(fields[i].getName())) {
				return i;
			}
		}
		throw new SecurityException(localize("security.instrumentation.field.not.found", fieldName, clazz.getName()));
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
				throw new SecurityException(localize("security.instrumentation.ignore.values.index.invalid",
						ignoreVariables.getIndex(), newVariables.size()));
			}
			@Nonnull
			Object toKeep = newVariables.get(ignoreVariables.getIndex());
			newVariables.clear();
			newVariables.add(toKeep);
			break;
		case "NONE_EXCEPT":
			if (ignoreVariables.getIndex() < 0 || ignoreVariables.getIndex() >= newVariables.size()) {
				throw new SecurityException(localize("security.instrumentation.ignore.values.index.invalid",
						ignoreVariables.getIndex(), newVariables.size()));
			}
			newVariables.remove(ignoreVariables.getIndex());
			break;
		default:
			throw new IllegalArgumentException(localize("aop.ignore.unknown.type", ignoreVariables.getType()));
		}
		return newVariables.toArray();
	}
	// </editor-fold>
}
