package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Java instrumentation thread system security advice.
 * <p>
 * Description: Provides static methods to enforce thread system security
 * policies at runtime by checking thread system interactions (create) against
 * allowed classes and thread counts, call stack criteria, and variable
 * criteria. Uses reflection to interact with test case settings and
 * localization utilities. Designed to prevent unauthorized thread system
 * operations during Java application execution, especially in test and
 * instrumentation scenarios.
 * <p>
 * Design Rationale: Centralizes thread system security checks for Java
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
public final class JavaInstrumentationAdviceThreadSystemToolbox extends JavaInstrumentationAdviceAbstractToolbox {

	// <editor-fold desc="Constants">

	/**
	 * Map of methods with attribute index exceptions for thread system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which attribute index should be
	 * exempted from ignore rules during thread system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> THREAD_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT = Map.ofEntries();

	/**
	 * Map of methods with parameter index exceptions for thread system ignore
	 * logic.
	 * <p>
	 * Description: Specifies for certain methods which parameter index should be
	 * exempted from ignore rules during thread system checks.
	 */
	@Nonnull
	private static final Map<String, IgnoreValues> THREAD_SYSTEM_IGNORE_PARAMETERS_EXCEPT = Map.ofEntries();
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
	private JavaInstrumentationAdviceThreadSystemToolbox() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationAdviceThreadSystemToolbox"));
	}
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
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			String className = element.getClassName();
			String methodName = element.getMethodName();
			// Check for ProcessBuilder.start() and ProcessBuilder.startPipeline()
			if ("java.lang.ProcessBuilder".equals(className)
					&& ("start".equals(methodName) || "startPipeline".equals(methodName))) {
				return true;
			}
			// Check for Runtime.exec()
			if ("java.lang.Runtime".equals(className) && "exec".equals(methodName)) {
				return true;
			}
		}
		return false;
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
	private static boolean checkIfThreadIsForbidden(@Nullable ThreadTarget target,
			@Nullable String[] allowedThreadClasses, @Nullable int[] allowedThreadNumbers) {
		if (target == null || target.className() == null) {
			return false;
		}
		String actualClassname = target.className();
		if (allowedThreadClasses == null || allowedThreadClasses.length == 0 || allowedThreadNumbers == null
				|| allowedThreadNumbers.length == 0) {
			return true;
		}
		int starIndex = -1;
		for (int i = 0; i < allowedThreadClasses.length; i++) {
			@Nonnull
			String allowedClassName = allowedThreadClasses[i];
			if ("*".equals(allowedClassName)) {
				starIndex = i;
				continue;
			}
			if (threadClassMatches(actualClassname, allowedClassName)) {
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
	 * to exact string comparison when classes cannot be loaded (e.g. JDK internal
	 * classes).
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
			Class<?> allowedClass = Class.forName(allowedClassName, true, ClassLoader.getSystemClassLoader());
			Class<?> actualClass = Class.forName(actualClassname, true, ClassLoader.getSystemClassLoader());
			return allowedClass.isAssignableFrom(actualClass);
		} catch (ClassNotFoundException e) {
			// Class not found via system classloader (e.g. JDK internal classes like
			// sun.nio.ch.AsynchronousChannelGroupImpl$1) - fall back to string comparison
			return allowedClassName.equals(actualClassname);
		} catch (IllegalStateException | NullPointerException e) {
			// Unexpected error during class loading - log and continue
			// Fail secure: if we can't verify the class, continue to next check
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
		return className.contains("$$Lambda") || className.contains("$Lambda$")
				|| className.matches(".*\\$\\$Lambda\\$.*");
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
			return className.equals("java.lang.Thread$FieldHolder")
					|| className.endsWith("$FieldHolder") && className.startsWith("java.lang.Thread");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Extracts the task field value from a Thread.FieldHolder instance using
	 * reflection.
	 * <p>
	 * Description: Uses reflection to safely access the task field from a
	 * Thread.FieldHolder instance. First attempts standard reflection, then falls
	 * back to sun.misc.Unsafe if necessary (e.g., when strong encapsulation
	 * prevents access).
	 *
	 * @param threadFieldHolder the Thread.FieldHolder instance
	 * @return the value of the task field as a String
	 * @throws SecurityException if extraction fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	private static String getTaskFromThreadFieldHolder(@Nonnull Object threadFieldHolder) {
		@Nonnull
		Class<?> fieldHolderClass = threadFieldHolder.getClass();

		// First, try standard reflection (preferred approach)
		try {
			Field taskField = fieldHolderClass.getDeclaredField("task");
			taskField.setAccessible(true);
			@Nullable
			Object taskValue = taskField.get(threadFieldHolder);
			if (taskValue == null) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
						.localize("security.advice.thread.task.null", threadFieldHolder));
			}
			@Nonnull
			Class<?> taskClass = taskValue.getClass();
			return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
		} catch (NoSuchFieldException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.thread.task.reflection.error", threadFieldHolder), e);
		} catch (IllegalAccessException | InaccessibleObjectException e) {
			// Standard reflection failed due to access restrictions, try Unsafe fallback
		}

		// Fallback to sun.misc.Unsafe for Java versions with strong encapsulation
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
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
						.localize("security.advice.thread.task.null", threadFieldHolder));
			}
			@Nonnull
			Class<?> taskClass = taskValue.getClass();
			return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
		} catch (ClassNotFoundException e) {
			// sun.misc.Unsafe not available (restricted JVM or future Java version)
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.thread.task.reflection.error", threadFieldHolder), e);
		} catch (NoSuchFieldException | IllegalAccessException | NullPointerException | InaccessibleObjectException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.thread.task.reflection.error", threadFieldHolder), e);
		} catch (InvocationTargetException | NoSuchMethodException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.advice.transform.path.unexpected.error"), e);
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
			} else if (variableValue instanceof Runnable || variableValue instanceof Callable<?>
					|| variableValue instanceof ForkJoinTask<?> || variableValue instanceof CompletableFuture<?>
					|| variableValue instanceof Supplier<?> || variableValue instanceof Function<?, ?>
					|| variableValue instanceof BiFunction<?, ?, ?> || variableValue instanceof CompletionStage<?>) {
				@Nonnull
				Class<?> variableClass = variableValue.getClass();
				return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
			} else {
				return null;
			}
		} catch (SecurityException ignored) {
			return null;
		}
	}

	/**
	 * Converts a variable value to a {@link ThreadTarget} if possible.
	 * <p>
	 * Description: Delegates to {@link #variableToClassname} and wraps the result.
	 *
	 * @param variableValue the variable to convert
	 * @return a {@link ThreadTarget}, or {@code null} if conversion fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nullable
	private static ThreadTarget variableToTarget(@Nullable Object variableValue) {
		String className = variableToClassname(variableValue);
		return className == null ? null : new ThreadTarget(className);
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
	 * @author Markus Paulsen
	 * @param observedVariable               the variable to analyze
	 * @param threadClassAllowedToBeCreated  whitelist of allowed thread classes
	 * @param threadNumberAllowedToBeCreated the number of threads allowed to be
	 *                                       created
	 * @return true if a violation is found, false otherwise
	 */
	private static boolean analyseViolation(@Nullable Object observedVariable,
			@Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return false;
		} else if (observedVariable.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(observedVariable); i++) {
				Object element = Array.get(observedVariable, i);
				boolean violation = analyseViolation(element, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
				if (violation) {
					return true;
				}
			}
			return false;
		} else if (observedVariable instanceof List<?>) {
			for (Object element : (List<?>) observedVariable) {
				boolean violation = analyseViolation(element, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
				if (violation) {
					return true;
				}
			}
			return false;
		} else {
			try {
				ThreadTarget target = variableToTarget(observedVariable);
				return checkIfThreadIsForbidden(target, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
			} catch (SecurityException ignored) {
				return false;
			}
		}
	}

	/**
	 * Extracts and returns the first violating class name from an array or list
	 * variable.
	 * <p>
	 * Description: Iterates through the variable’s elements (array or List),
	 * converts each to a class name if possible, and returns the name of the first
	 * class that does not satisfy the allowed thread classes whitelist.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariable               the array or List to inspect
	 * @param threadClassAllowedToBeCreated  the thread classes that are allowed to
	 *                                       be created
	 * @param threadNumberAllowedToBeCreated the number of threads allowed to be
	 *                                       created
	 * @return the first violating class name as a String, or null if none found
	 */
	@Nullable
	private static String extractViolationPath(@Nullable Object observedVariable,
			@Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated) {
		if (observedVariable == null || observedVariable instanceof byte[] || observedVariable instanceof Byte[]) {
			return null;
		} else if (observedVariable.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(observedVariable); i++) {
				Object element = Array.get(observedVariable, i);
				String violationPath = extractViolationPath(element, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
				if (violationPath != null) {
					return violationPath;
				}
			}
			return null;
		} else if (observedVariable instanceof List<?>) {
			for (Object element : (List<?>) observedVariable) {
				String violationPath = extractViolationPath(element, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
				if (violationPath != null) {
					return violationPath;
				}
			}
			return null;
		} else {
			try {
				ThreadTarget target = variableToTarget(observedVariable);
				if (checkIfThreadIsForbidden(target, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated)) {
					return target != null ? target.toDisplayString() : null;
				}
			} catch (SecurityException ignored) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Checks an array of observedVariables against the allowed thread classes
	 * whitelist.
	 * <p>
	 * Description: Iterates through the filtered observedVariables (excluding those
	 * matching ignoreVariables). For each non-null variable, if it is an array or a
	 * List (excluding byte[]/Byte[]), each element is converted to a class name and
	 * tested against the allowed thread classes. Otherwise, the variable itself is
	 * converted and tested. The first violating class name found is returned.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param observedVariables              array of values to validate
	 * @param threadClassAllowedToBeCreated  whitelist of allowed thread classes
	 * @param threadNumberAllowedToBeCreated the number of threads allowed to be
	 *                                       created
	 * @param ignoreVariables                criteria determining which
	 *                                       observedVariables to skip
	 * @return the first violating class name (as String) or null if none violate
	 */
	private static String checkIfVariableCriteriaIsViolated(@Nonnull Object[] observedVariables,
			@Nullable String[] threadClassAllowedToBeCreated, @Nullable int[] threadNumberAllowedToBeCreated,
			@Nonnull IgnoreValues ignoreVariables) {
		for (@Nullable
		Object observedVariable : filterVariables(observedVariables, ignoreVariables)) {
			if (analyseViolation(observedVariable, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated)) {
				return extractViolationPath(observedVariable, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated);
			}
		}
		return null;
	}
	// </editor-fold>

	// </editor-fold>

	// <editor-fold desc="Check methods">

	/**
	 * Validates a thread system interaction against security policies.
	 * <p>
	 * Description: Verifies that the specified action (create) complies with
	 * allowed threads and call stack criteria. Throws SecurityException if a policy
	 * violation is detected.
	 *
	 * @param action            the thread system action being performed
	 * @param declaringTypeName the fully qualified class name of the caller
	 * @param methodName        the name of the method invoked
	 * @param methodSignature   the method signature descriptor
	 * @param attributes        optional method attributes
	 * @param parameters        optional method parameters
	 * @param instance          the receiver object of the intercepted call; may be
	 *                          null
	 * @throws SecurityException if unauthorized access is detected
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static void checkThreadSystemInteraction(@Nonnull String action, @Nonnull String declaringTypeName,
			@Nonnull String methodName, @Nonnull String methodSignature, @Nullable Object[] attributes,
			@Nullable Object[] parameters, @Nullable Object instance) {
		// <editor-fold desc="Get information from settings">
		@Nullable
		final String aopMode = getValueFromSettings("aopMode");
		if (aopMode == null || aopMode.isEmpty() || !aopMode.equals("INSTRUMENTATION")) {
			return;
		}
		@Nullable
		final String restrictedPackage = getValueFromSettings("restrictedPackage");
		if (restrictedPackage == null || restrictedPackage.isEmpty()) {
			return;
		}
		@Nullable
		final String[] allowedClasses = getValueFromSettings("allowedListedClasses");

		@Nullable
		final String[] threadClassAllowedToBeCreated = getValueFromSettings("threadClassAllowedToBeCreated");
		int threadClassAllowedToBeCreatedSize = threadClassAllowedToBeCreated == null ? 0
				: threadClassAllowedToBeCreated.length;
		@Nullable
		final int[] threadNumberAllowedToBeCreated = getValueFromSettings("threadNumberAllowedToBeCreated");
		int threadNumberAllowedToBeCreatedSize = threadNumberAllowedToBeCreated == null ? 0
				: threadNumberAllowedToBeCreated.length;

		if (threadNumberAllowedToBeCreatedSize != threadClassAllowedToBeCreatedSize) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.thread.allowed.size",
							threadNumberAllowedToBeCreatedSize, threadClassAllowedToBeCreatedSize));
		}
		// </editor-fold>
		// <editor-fold desc="Get information from attributes">
		@Nonnull
		final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
		// </editor-fold>
		// <editor-fold desc="Skip if thread creation is triggered by ProcessBuilder or
		// Runtime.exec">
		// When ProcessBuilder.start() or Runtime.exec() is called, they internally
		// create threads
		// for process I/O handling. These threads should not be blocked by the Thread
		// subsystem
		// because the Command/Execute subsystem will check these operations.
		if (isThreadCreationFromCommandExecution()) {
			return;
		}
		// </editor-fold>
		// <editor-fold desc="Check callstack">
		@Nullable
		String threadSystemMethodToCheck = (restrictedPackage == null) ? null
				: checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
		if (threadSystemMethodToCheck == null) {
			return;
		}
		@Nullable
		String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
		// </editor-fold>
		// <editor-fold desc="Check parameters">
		@Nullable
		String threadIllegallyInteractedThroughParameter = (parameters == null || parameters.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(parameters, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated, THREAD_SYSTEM_IGNORE_PARAMETERS_EXCEPT
								.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
		if (threadIllegallyInteractedThroughParameter != null) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.thread.execution", threadSystemMethodToCheck, action,
					threadIllegallyInteractedThroughParameter, fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
		// <editor-fold desc="Check attributes">
		// Create combined array with declaringTypeName and attributes
		Object[] attributesToCheck = attributes == null ? new Object[] { declaringTypeName }
				: Stream.concat(Stream.of(declaringTypeName), Arrays.stream(attributes)).toArray();

		@Nullable
		String threadIllegallyInteractedThroughAttribute = (attributesToCheck.length == 0) ? null
				: checkIfVariableCriteriaIsViolated(attributesToCheck, threadClassAllowedToBeCreated,
						threadNumberAllowedToBeCreated, THREAD_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT
								.getOrDefault(declaringTypeName + "." + methodName, IgnoreValues.NONE));
		if (threadIllegallyInteractedThroughAttribute != null) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.advice.illegal.thread.execution", threadSystemMethodToCheck, action,
					threadIllegallyInteractedThroughAttribute, fullMethodSignature
							+ (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")));
		}
		// </editor-fold>
	}
	// </editor-fold>

	// </editor-fold>
}
