package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

//<editor-fold desc="imports">

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import org.aspectj.lang.JoinPoint;
//</editor-fold>

@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJThreadSystemAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

	// <editor-fold desc="Constants">



	/**
	 * Sentinel class name used when a thread-task carrier exists but its task class
	 * cannot be read (reflection and Unsafe both failed). It matches no allow-list
	 * entry, so the creation is denied (fail closed) instead of silently allowed.
	 */
	@Nonnull
	private static final String UNRESOLVED_THREAD_CLASS = "<unresolved-thread-class>";
	// </editor-fold>

	// <editor-fold desc="Thread system methods">

	// <editor-fold desc="Command execution bypass">

	/**
	 * Checks if the current thread creation is triggered by ProcessBuilder or
	 * Runtime.exec.
	 * <p>
	 * Description: When ProcessBuilder.start() or Runtime.exec() is called, they
	 * internally create threads for process I/O handling. These thread creations
	 * should not be blocked by the Thread subsystem because the Command/Execute
	 * subsystem handles these operations. This method scans the call stack to
	 * detect if the thread creation originates from these command execution
	 * methods.
	 *
	 * @return true if thread creation is from ProcessBuilder or Runtime.exec, false
	 *         otherwise
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private static boolean isThreadCreationFromCommandExecution() {
		// Use StackWalker for the fast lazy path; the helper short-circuits as soon as
		// a ProcessBuilder.start/startPipeline or Runtime.exec frame is observed and
		// avoids materializing the entire StackTraceElement[] array per intercepted
		// thread creation. Mirrors JavaInstrumentationAdviceThreadSystemToolbox.
		return java.lang.StackWalker.getInstance().walk(frames -> frames.anyMatch(frame -> {
			String className = frame.getClassName();
			String methodName = frame.getMethodName();
			// Check for ProcessBuilder.start() and ProcessBuilder.startPipeline()
			if ("java.lang.ProcessBuilder".equals(className)
					&& ("start".equals(methodName) || "startPipeline".equals(methodName))) {
				return true;
			}
			// Check for Runtime.exec()
			return "java.lang.Runtime".equals(className) && "exec".equals(methodName);
		}));
	}

	/**
	 * Returns true when the intercepted thread creation is owned by Ares's own
	 * {@code @StrictTimeout} machinery ({@code TimeoutUtils.executeWithTimeout}
	 * submits each timed test invocation to an executor) rather than by student
	 * code. Walking from the top of the stack, a {@code TimeoutUtils} frame reached
	 * before any restricted-package (student) frame means the timeout machinery
	 * created the thread (exempt); a student frame seen first means the student
	 * created it (still blocked). The student's test body runs on a separate worker
	 * thread whose stack does not contain {@code TimeoutUtils}, so a student thread
	 * is never exempted.
	 */
	private static boolean isThreadCreationFromAresTimeout(String restrictedPackage) {
		return java.lang.StackWalker.getInstance().walk(frames -> {
			java.util.Iterator<java.lang.StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next().getClassName();
				if ("de.tum.cit.ase.ares.api.internal.TimeoutUtils".equals(className)) {
					return Boolean.TRUE;
				}
				// Ares's own infrastructure frames (this advice, internals) are never student
				// code, even when restrictedPackage is a broad prefix that nominally covers them
				// (e.g. the self-test fallback "de.tum.cit.ase"). Skipping them lets the walk
				// reach the TimeoutUtils frame that legitimately owns this @StrictTimeout worker.
				if (className.startsWith("de.tum.cit.ase.ares.api.")) {
					continue;
				}
				if (restrictedPackage != null && !restrictedPackage.isEmpty()
						&& className.startsWith(restrictedPackage)) {
					return Boolean.FALSE;
				}
			}
			return Boolean.FALSE;
		});
	}

	/**
	 * Prefix marking the synthetic thread-class token of an implicit thread
	 * operation (one that performs thread-system work without passing an explicit
	 * thread-task class). Such tokens are governed by the normal allow-list/quota
	 * check, but are never matched by the {@code "*"} wildcard, so a broad
	 * allow-list cannot silently permit them.
	 */
	private static final String IMPLICIT_THREAD_OPERATION_PREFIX = "<implicit-thread-op:";

	/**
	 * Returns the per-operation sentinel token for an implicit thread operation
	 * intercepted with no resolvable thread-task class
	 * ({@code Collection.parallelStream}, {@code BaseStream.parallel},
	 * {@code Thread.sleep}, {@code SubmissionPublisher.submit/offer}), or
	 * {@code null} if the intercepted method is not such an operation.
	 *
	 * @param declaringTypeName the declaring type of the intercepted method
	 * @param methodName the intercepted method name
	 * @return the sentinel token, or {@code null}
	 */
	@Nullable
	private static String implicitThreadOperationToken(@Nullable String declaringTypeName, @Nullable String methodName) {
		if (methodName == null) {
			return null;
		}
		switch (methodName) {
		case "parallelStream":
		case "parallel":
			return IMPLICIT_THREAD_OPERATION_PREFIX + methodName + ">";
		case "submit":
		case "offer":
			return "java.util.concurrent.SubmissionPublisher".equals(declaringTypeName)
					? IMPLICIT_THREAD_OPERATION_PREFIX + "SubmissionPublisher." + methodName + ">"
					: null;
		default:
			return null;
		}
	}

	/**
	 * Returns {@code true} when the first non-infrastructure frame on the current
	 * stack is restricted-package (student) code. Used to attribute an implicit
	 * thread operation to the student only when the student invoked it directly: a
	 * woven harness or JDK helper that itself performs the operation while running
	 * under student code is the immediate caller and is therefore not blamed on the
	 * student. JDK, Ares-infrastructure and ignored frames are skipped because
	 * student code can never declare those namespaces.
	 *
	 * @param restrictedPackage the configured restricted (student) package prefix
	 * @return {@code true} if the immediate non-infrastructure caller is student code
	 */
	private static boolean isImmediateCallerWithinRestrictedPackage(@Nullable String restrictedPackage) {
		if (restrictedPackage == null || restrictedPackage.isEmpty()) {
			return false;
		}
		return java.lang.StackWalker.getInstance().walk(frames -> {
			java.util.Iterator<java.lang.StackWalker.StackFrame> iterator = frames.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next().getClassName();
				if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("jdk.")
						|| className.startsWith("sun.") || className.startsWith("com.sun.")
						|| className.startsWith("de.tum.cit.ase.ares.api.")) {
					continue;
				}
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
				return className.startsWith(restrictedPackage);
			}
			return Boolean.FALSE;
		});
	}
	// </editor-fold>

	// <editor-fold desc="Variable criteria methods">

	// <editor-fold desc="Forbidden handling">

	/**
	 * Checks whether the thread limit for the class at the specified index is still
	 * available. If the count at that index is greater than zero, this method
	 * atomically decrements it and returns <code>false</code> (indicating it was
	 * allowed). If the count is zero or below, it returns <code>true</code>
	 * (indicating the class is disallowed because the quota is exhausted).
	 * <p>
	 * This method uses atomic check-and-decrement to prevent race conditions where
	 * multiple threads could pass the check simultaneously and exceed the
	 * configured thread limit.
	 *
	 * @param allowedThreadNumbers An array of permissible thread counts, parallel
	 *                             to the class array that determines which classes
	 *                             can create threads.
	 * @param index                The index corresponding to the class being
	 *                             checked.
	 * @return <code>true</code> if no more threads can be created (disallowed), or
	 *         <code>false</code> if the class is allowed to create another thread
	 *         and the count was decremented.
	 */
	private static boolean handleFoundClassIsForbidden(@Nullable int[] allowedThreadNumbers, int index) {
		if (allowedThreadNumbers == null) {
			return true;
		}
		// Use atomic check-and-decrement to prevent race conditions
		boolean successfullyDecremented = checkAndDecrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
		return !successfullyDecremented;
	}

	/**
	 * Checks if a class name is outside of the allowed paths whitelist.
	 * <p>
	 * Description: Returns true if allowedPaths not null or if the given path does
	 * not match one of the allowedPatterns.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param actualClassname      the class name of the thread being requested
	 * @param allowedThreadClasses the thread classes that are allowed to be created
	 * @param allowedThreadNumbers the number of threads allowed to be created
	 * @return true if path is forbidden; false otherwise
	 */
	private static boolean checkIfThreadIsForbidden(@Nullable ThreadTarget target, @Nullable String[] allowedThreadClasses, @Nullable int[] allowedThreadNumbers) {
		if (target == null || target.className() == null) {
			return false;
		}
		String actualClassname = target.className();
		if (allowedThreadClasses == null || allowedThreadClasses.length == 0 || allowedThreadNumbers == null || allowedThreadNumbers.length == 0) {
			return true;
		}
		// Implicit thread operations (parallelStream/parallel/Thread.sleep/SubmissionPublisher.submit) are
		// represented by a sentinel token. The "*" wildcard never covers them and class-hierarchy matching
		// does not apply; only an exact sentinel entry in the allow-list permits one, so a broad "*" cannot
		// silently allow them.
		boolean isImplicitOperation = actualClassname.startsWith(IMPLICIT_THREAD_OPERATION_PREFIX);
		int starIndex = -1;
		for (int i = 0; i < allowedThreadClasses.length; i++) {
			@Nonnull
			String allowedClassName = allowedThreadClasses[i];
			if ("*".equals(allowedClassName)) {
				if (!isImplicitOperation) {
					starIndex = i;
				}
				continue;
			}
			boolean matches = isImplicitOperation ? actualClassname.equals(allowedClassName)
					: threadClassMatches(actualClassname, allowedClassName);
			if (matches) {
				return handleFoundClassIsForbidden(allowedThreadNumbers, i);
			}
		}
		if (starIndex != -1) {
			return handleFoundClassIsForbidden(allowedThreadNumbers, starIndex);
		}
		return true;
	}

	/**
	 * Checks whether an actual thread class name matches an allowed class name.
	 * <p>
	 * Description: Handles lambda expressions as a special case (both must be
	 * lambdas to match). For regular classes, uses {@code Class.forName} and
	 * {@code isAssignableFrom} to support class hierarchy matching. Falls back
	 * to exact string comparison when classes cannot be loaded.
	 *
	 * @param actualClassname  the class name from the intercepted call
	 * @param allowedClassName the class name from the security policy
	 * @return {@code true} if the actual class is covered by the allowed class
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private static boolean threadClassMatches(@Nonnull String actualClassname, @Nonnull String allowedClassName) {
		boolean actualIsLambda = "Lambda-Expression".equals(actualClassname);
		boolean allowedIsLambda = "Lambda-Expression".equals(allowedClassName);

		if (allowedIsLambda && actualIsLambda) {
			return true;
		}
		if (allowedIsLambda || actualIsLambda) {
			return false;
		}

		try {
			// initialize=false: an assignability check does not require running the
			// classes' static initialisers, and initialising a student-controlled class
			// during a security decision would execute arbitrary code at an unexpected
			// point (and risk advice re-entrancy).
			Class<?> allowedClass = Class.forName(allowedClassName, false, ClassLoader.getSystemClassLoader());
			Class<?> actualClass = Class.forName(actualClassname, false, ClassLoader.getSystemClassLoader());
			return allowedClass.isAssignableFrom(actualClass);
		} catch (ClassNotFoundException e) {
			return allowedClassName.equals(actualClassname);
		} catch (IllegalStateException | NullPointerException e) {
			return false;
		}
	}
	// </editor-fold>

	// <editor-fold desc="Conversion handling">

	/**
	 * Checks if a variable is a lambda expression.
	 * <p>
	 * Description: Determines if the provided variable is a lambda expression by
	 * checking if its class is synthetic and has a writeReplace() method that
	 * returns a SerializedLambda.
	 *
	 * @param variableClass the class of the variable to check
	 * @return true if the variable is a lambda expression, false otherwise
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private static boolean isReallyLambda(@Nonnull Class<?> variableClass) {
		// Step 1: check if the class is synthetic
		if (!variableClass.isSynthetic()) {
			return false;
		}
		String className = variableClass.getName();

		// Check for common lambda patterns
		return className.contains("$$Lambda") ||
				className.contains("$Lambda$") ||
				className.matches(".*\\$\\$Lambda\\$.*");
	}

	/**
	 * Checks if a variable is an instance of Thread.FieldHolder using reflection.
	 * <p>
	 * Description: Uses reflection to safely check if the variable is an instance
	 * of Thread.FieldHolder, avoiding direct instanceof checks that might fail in
	 * instrumentation contexts.
	 *
	 * @param variableValue the variable to check
	 * @return true if the variable is a Thread.FieldHolder instance, false
	 *         otherwise
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private static boolean isThreadFieldHolder(@Nullable Object variableValue) {
		if (variableValue == null) {
			return false;
		}

		try {
			@Nonnull
			Class<?> variableClass = variableValue.getClass();
			@Nonnull
			String className = variableClass.getName();

			// Check if the class name matches Thread.FieldHolder pattern
			return className.equals("java.lang.Thread$FieldHolder") ||
					className.endsWith("$FieldHolder") && className.startsWith("java.lang.Thread");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Extracts the task field value from a Thread.FieldHolder instance using
	 * reflection.
	 * <p>
	 * Description: Uses reflection to safely access the task field from a
	 * Thread.FieldHolder instance, handling potential access restrictions.
	 *
	 * @param threadFieldHolder the Thread.FieldHolder instance
	 * @return the task class name, or {@code null} when the task field is genuinely
	 *         null (a Thread subclass with an overridden {@code run()} and no
	 *         Runnable)
	 * @throws SecurityException if the task field exists but cannot be read
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nullable
	private static String getTaskFromThreadFieldHolder(@Nonnull Object threadFieldHolder) {
		@Nonnull
		Class<?> fieldHolderClass = threadFieldHolder.getClass();

		// First, try standard reflection (preferred; avoids Unsafe where possible).
		try {
			Field taskField = fieldHolderClass.getDeclaredField("task");
			taskField.setAccessible(true);
			@Nullable
			Object taskValue = taskField.get(threadFieldHolder);
			if (taskValue == null) {
				// Genuinely no Runnable task: not a read failure. Return null so the caller
				// can fall back to the thread's own class instead of denying outright.
				return null;
			}
			@Nonnull
			Class<?> taskClass = taskValue.getClass();
			return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
		} catch (NoSuchFieldException e) {
			throw new SecurityException(localize("security.advice.transform.path.exception.detail", threadFieldHolder), e);
		} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
			// Standard reflection failed (access restriction, or SecurityException from
			// setAccessible); try the Unsafe fallback instead of letting it escape to the
			// sentinel path and over-block an otherwise allowed thread.
		}

		// Fallback to sun.misc.Unsafe for runtimes with strong encapsulation.
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			Object unsafe = unsafeField.get(null);
			Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
			Field taskField = fieldHolderClass.getDeclaredField("task");
			long offset = (Long) objectFieldOffsetMethod.invoke(unsafe, taskField);
			Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
			@Nullable
			Object taskValue = getObjectMethod.invoke(unsafe, threadFieldHolder, offset);
			if (taskValue == null) {
				return null;
			}
			@Nonnull
			Class<?> taskClass = taskValue.getClass();
			return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
		} catch (NoSuchFieldException | IllegalAccessException | NullPointerException
				| InaccessibleObjectException e) {
			throw new SecurityException(localize("security.advice.transform.path.exception.detail", threadFieldHolder), e);
		} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException e) {
			throw new SecurityException(localize("security.advice.transform.path.unexpected.error"), e);
		}
	}

	/**
	 * Returns the class name of the Runnable task carried by a {@link Thread}, or
	 * {@code null} when the thread has no task (an overridden {@code run()}) or the
	 * task cannot be read. Lambda tasks resolve to {@code "Lambda-Expression"}.
	 * Reading the task directly from the receiver makes the thread check
	 * self-sufficient, so it does not depend on the FieldHolder attribute being
	 * separately readable (that field read may be skipped under strong
	 * encapsulation).
	 *
	 * @param thread the thread receiver to inspect
	 * @return the task class name, or {@code null} if there is no readable task
	 */
	@Nullable
	private static String threadTaskClassName(@Nonnull Thread thread) {
		Object task = readThreadTask(thread);
		if (task == null) {
			return null;
		}
		Class<?> taskClass = task.getClass();
		return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
	}

	/**
	 * Reads the Runnable task object from a {@link Thread}, trying the modern
	 * {@code holder.task} layout first and the legacy {@code target} field as a
	 * fallback, each via reflection then Unsafe. Returns {@code null} if no task is
	 * set or it cannot be read.
	 *
	 * @param thread the thread receiver to inspect
	 * @return the task object, or {@code null}
	 */
	@Nullable
	private static Object readThreadTask(@Nonnull Thread thread) {
		// Modern layout: Thread.holder.task.
		try {
			Field holderField = Thread.class.getDeclaredField("holder");
			Object holder = readField(holderField, thread);
			if (holder != null) {
				Field taskField = holder.getClass().getDeclaredField("task");
				return readField(taskField, holder);
			}
		} catch (NoSuchFieldException ignored) {
			// Not the modern layout; try the legacy field below.
		}
		// Legacy layout: Thread.target.
		try {
			Field targetField = Thread.class.getDeclaredField("target");
			return readField(targetField, thread);
		} catch (NoSuchFieldException ignored) {
			return null;
		}
	}

	/**
	 * Reads an object field via standard reflection, falling back to
	 * {@code sun.misc.Unsafe} when strong encapsulation blocks reflective access, so
	 * the overridden-run() detection still works on a locked-down runtime. Returns
	 * {@code null} if the value is null or cannot be read by either means.
	 *
	 * @param field the field to read
	 * @param owner the instance to read it from
	 * @return the field value, or {@code null}
	 */
	@Nullable
	private static Object readField(@Nonnull Field field, @Nonnull Object owner) {
		try {
			field.setAccessible(true);
			return field.get(owner);
		} catch (IllegalAccessException | InaccessibleObjectException | SecurityException ignored) {
			// Fall back to Unsafe below (a SecurityException from setAccessible must not
			// escape and turn into an over-block of an otherwise allowed thread).
		}
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Object unsafe = theUnsafe.get(null);
			Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
			long offset = (Long) objectFieldOffset.invoke(unsafe, field);
			Method getObject = unsafeClass.getMethod("getObject", Object.class, long.class);
			return getObject.invoke(unsafe, owner, offset);
		} catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException
				| InvocationTargetException | InaccessibleObjectException | NullPointerException ignored) {
			return null;
		}
	}

	/**
	 * Converts a variable value to its class name.
	 * <p>
	 * Description: Returns null when the value is null or cannot be resolved. If
	 * the variable is a lambda expression, returns "Lambda-Expression". Otherwise,
	 * returns the class name of the variable.
	 */
	@Nullable
	private static String variableToClassname(@Nullable Object variableValue) {
		try {
			if (variableValue == null) {
				return null;
			} else if (isThreadFieldHolder(variableValue)) {
				return getTaskFromThreadFieldHolder(variableValue);
			} else if (variableValue instanceof Thread thread) {
				// Resolve the thread's effective class from the receiver itself so the check
				// does not depend on the FieldHolder attribute being separately readable. When
				// the thread carries a Runnable task, that task's class identifies it;
				// otherwise the thread's own class does (an overridden run()). The FieldHolder
				// attribute resolves to the same task class and is de-duplicated, so the quota
				// is consumed once. Must precede the Runnable branch because Thread implements
				// Runnable.
				String taskClassName = threadTaskClassName(thread);
				return taskClassName != null ? taskClassName : thread.getClass().getName();
			} else if (variableValue instanceof Runnable || variableValue instanceof Callable<?> || variableValue instanceof ForkJoinTask<?> || variableValue instanceof CompletableFuture<?> || variableValue instanceof Supplier<?> || variableValue instanceof Function<?, ?> || variableValue instanceof BiFunction<?, ?, ?> || variableValue instanceof CompletionStage<?>) {
				@Nonnull
				Class<?> variableClass = variableValue.getClass();
				return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
			} else {
				return null;
			}
		} catch (SecurityException ignored) {
			// A thread-task carrier whose task class could not be read must not pass
			// silently; return a sentinel that matches no allow-list entry so the creation
			// is denied (fail closed).
			return UNRESOLVED_THREAD_CLASS;
		}
	}

	// </editor-fold>

	// <editor-fold desc="Violation analysis">

	/**
	 * Collects the resolved thread-class names from a variable, recursing into
	 * arrays and Lists. Used to de-duplicate candidates across parameters, the
	 * receiver, and attributes so the thread-number quota is consumed at most once
	 * per distinct class per interception.
	 *
	 * @param observedVariable the variable to resolve
	 * @param out              the accumulator of distinct class names
	 */
	private static void collectThreadClassNames(@Nullable Object observedVariable, @Nonnull Set<String> out) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return;
		} else if (observedVariable.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(observedVariable); i++) {
				collectThreadClassNames(Array.get(observedVariable, i), out);
			}
		} else if (observedVariable instanceof List<?>) {
			for (Object element : (List<?>) observedVariable) {
				collectThreadClassNames(element, out);
			}
		} else {
			String className = variableToClassname(observedVariable);
			if (className != null) {
				out.add(className);
			}
		}
	}
	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Check methods">

	/**
	 * Validates a thread system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (create)
	 * complies with allowed threads and call stack criteria. Throws SecurityException
	 * if a policy violation is detected.
	 *
	 * @param action the thread system action being performed
	 * @param thisJoinPoint the join point representing the method call
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public void checkThreadSystemInteraction(
			@Nonnull String action,
			@Nonnull JoinPoint thisJoinPoint
	) {
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
		String[] allowedThreadClasses = getValueFromSettings("threadClassAllowedToBeCreated");
		int allowedThreadClassesSize = allowedThreadClasses == null ? 0 : allowedThreadClasses.length;
		@Nullable
		int[] allowedThreadNumbers = getValueFromSettings("threadNumberAllowedToBeCreated");
		int allowedThreadNumbersSize = allowedThreadNumbers == null ? 0 : allowedThreadNumbers.length;
		if (allowedThreadNumbersSize != allowedThreadClassesSize) {
			throw new SecurityException(localize("security.advice.thread.allowed.size", allowedThreadNumbersSize, allowedThreadClassesSize));
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
		// access to a field (e.g. a JDK-internal field such as the Cleaner.Cleanable in
		// Executors$AutoShutdownDelegatedExecutorService reached via Ares's own timeout
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
				} catch (InaccessibleObjectException
						| IllegalAccessException
						| SecurityException
						| IllegalArgumentException
						| NullPointerException
						| ExceptionInInitializerError ignored) {
					attributes[i] = null;
				}
			}
		}
		// </editor-fold>
		// <editor-fold desc="Skip if thread creation is triggered by ProcessBuilder or Runtime.exec">
		// When ProcessBuilder.start() or Runtime.exec() is called, they internally create threads
		// for process I/O handling. These threads should not be blocked by the Thread subsystem
		// because the Command/Execute subsystem will check these operations.
		if (isThreadCreationFromCommandExecution() || isThreadCreationFromAresTimeout(restrictedPackage)) {
			return;
		}
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String systemMethodToCheck = (restrictedPackage == null) ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (systemMethodToCheck == null) {
			return;
		}
		// </editor-fold>
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		boolean noAllowRuleConfigured = allowedThreadClasses == null || allowedThreadClasses.length == 0 || allowedThreadNumbers == null || allowedThreadNumbers.length == 0;
		// <editor-fold desc="Check thread targets (quota counted once per distinct class)">
		// Resolve every thread-class candidate from the parameters, the receiver, and the
		// receiver's attributes into a single de-duplicated set, then check each distinct
		// class exactly once. De-duplication keeps the thread-number quota correct: the
		// previous design scanned parameters and attributes separately, so a task reached
		// through both decremented the quota twice. A call that creates no thread resolves
		// no class and therefore consumes nothing. Both thread ignore maps are empty, so no
		// per-method ignore filtering applies here.
		@Nonnull
		Set<String> threadClassNames = new LinkedHashSet<>();
		if (parameters != null) {
			for (Object parameter : parameters) {
				collectThreadClassNames(parameter, threadClassNames);
			}
		}
		collectThreadClassNames(instance, threadClassNames);
		if (attributes != null) {
			for (Object attribute : attributes) {
				collectThreadClassNames(attribute, threadClassNames);
			}
		}
		// An implicit thread operation (parallelStream/parallel/Thread.sleep/SubmissionPublisher.submit/
		// offer) carries no thread-task class, so the resolution above found nothing. Represent it by a
		// per-operation sentinel so the existing allow-list/quota check governs it, but only when the
		// student invoked it directly (the first non-infrastructure caller is restricted-package code), so
		// a woven harness or JDK helper that performs the operation under student code is not blamed on the
		// student.
		if (threadClassNames.isEmpty()) {
			@Nullable
			String implicitOperationToken = implicitThreadOperationToken(declaringTypeName, methodName);
			if (implicitOperationToken != null && isImmediateCallerWithinRestrictedPackage(restrictedPackage)) {
				threadClassNames.add(implicitOperationToken);
			}
		}
		for (String threadClassName : threadClassNames) {
			if (checkIfThreadIsForbidden(new ThreadTarget(threadClassName), allowedThreadClasses, allowedThreadNumbers)) {
				throw new SecurityException(localize(
						"security.advice.illegal.thread.execution", systemMethodToCheck, action, threadClassName,
						fullMethodSignature + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
								+ " | " + buildDenialReason(noAllowRuleConfigured)));
			}
		}
		// </editor-fold>
	}
	// </editor-fold>

	// </editor-fold>

	before():
			de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJThreadSystemPointcutDefinitions.threadCreateMethodsWithParameters() ||
					de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJThreadSystemPointcutDefinitions.threadCreateMethodsWithoutParameters() {
		checkThreadSystemInteraction("create", thisJoinPoint);
	}

}
