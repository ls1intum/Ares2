package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.error.AssertJMultipleFailuresError;
import org.assertj.core.error.MultipleAssertionsError;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.IncompleteExecutionException;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.*;

import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.sanitization.*;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;

/**
 * For handling and post processing Exceptions and Errors.
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class ReportingUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ReportingUtils.class);

	/** The most failures the search for a blocked action reads in one report. */
	private static final int MAX_SCANNED_FAILURES = 256;

	/**
	 * How long the search for a blocked action, and the sanitising of the report,
	 * may each take. Both run on a worker thread, because reading a failure locks
	 * it or calls student code, which may hold that lock or never return.
	 */
	private static final Duration INSPECTION_TIMEOUT = Duration.ofSeconds(2);

	/**
	 * The methods of a failure a report may call, directly or by printing it. An
	 * override of any of them in student code may run code or add a blocked action
	 * when the report reads it.
	 */
	private static final Set<String> READ_METHODS = Set.of("getMessage", "getLocalizedMessage", "getCause", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"getStackTrace", "toString", "printStackTrace", "fillInStackTrace"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * Library failures whose own versions of read methods are trusted, each with
	 * those methods: they only join or format their members' messages, and
	 * redacting them would hide ordinary failed assertions.
	 */
	private static final Map<Class<?>, Set<String>> TRUSTED_LIBRARY_METHODS = Map.of(MultipleFailuresError.class,
			Set.of("getMessage"), AssertJMultipleFailuresError.class, Set.of("getMessage"), //$NON-NLS-1$ //$NON-NLS-2$
			MultipleAssertionsError.class, Set.of("getMessage"), AssertionFailedError.class, Set.of("toString")); //$NON-NLS-1$ //$NON-NLS-2$

	static {
		// Initialize
		ThrowableSanitizer.sanitize(null);
	}

	private ReportingUtils() {
	}

	public static <T> T doProceedAndPostProcess(Invocation<T> invocation, TestContext context) throws Throwable {
		try {
			return invocation.proceed();
		} catch (Throwable t) {
			throw processThrowable(t, context);
		}
	}

	public static Throwable processThrowable(Throwable t, TestContext context) {
		boolean aresInternalError = isAresInternalError(t);
		// Always surface genuine framework faults to instructors via the server log,
		// even when the failure occurs inside a hidden test.
		if (aresInternalError) {
			LOG.error("Ares internal error during test execution", t); //$NON-NLS-1$
			// Hidden tests must never reveal why they failed, so their uniform message
			// takes
			// precedence over the more specific internal-error message for the student
			// view.
		}
		if (context.findTestType().orElse(null) == TestType.HIDDEN) {
			return new AssertionError(localized("test_guard.hidden_test_failed")); //$NON-NLS-1$
		}
		if (aresInternalError) {
			return new AssertionError(localized("reporting.ares_internal_error")); //$NON-NLS-1$
		}
		Inspection inspection = inspect(t);
		SecurityViolationFeedbackLevel level = ConfigurationUtils.getSecurityViolationFeedbackLevel(context);
		if (inspection.violation() != null) {
			return renderedViolation(t, inspection, level);
		}
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		boolean sanitizedNext = nonprivilegedFailureMessage.isEmpty() || t instanceof PrivilegedException;
		if (inspection.mayHideAViolation(level) && sanitizedNext) {
			return uninspectable();
		}
		if (nonprivilegedFailureMessage.isPresent()) {
			return processThrowablePrivilegedOnly(t, nonprivilegedFailureMessage.get(), inspection);
		}
		return sanitizedWithin(t, inspection);
	}

	/**
	 * Applies the policy's feedback level to a blocked action that fails outside
	 * the test invocation, such as in a JUnit callback where Ares checks the
	 * architecture; any other failure is returned unchanged.
	 *
	 * @param t       the failure.
	 * @param context the test context.
	 * @return the failure to report.
	 */
	public static Throwable processBlockedAction(Throwable t, TestContext context) {
		return blockedActionReport(t, inspect(t), context);
	}

	/**
	 * Reports a failure of a test's constructor or lifecycle method like one of the
	 * test: a blocked action at the policy's feedback level, and, when the policy
	 * reports only privileged failures, any other failure as the policy's message.
	 * A privileged failure and an aborted test keep their own report.
	 *
	 * @param t       the failure.
	 * @param context the test context.
	 * @return the failure to report.
	 */
	public static Throwable processLifecycleFailure(Throwable t, TestContext context) {
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		if (nonprivilegedFailureMessage.isEmpty() || t instanceof PrivilegedException
				|| t instanceof IncompleteExecutionException) {
			return processBlockedAction(t, context);
		}
		Inspection inspection = inspect(t);
		return inspection.violation() == null ? new AssertionError(nonprivilegedFailureMessage.get())
				: blockedActionReport(t, inspection, context);
	}

	/**
	 * Applies the policy's feedback level to an inspected failure that fails
	 * outside the test invocation; a failure with no blocked action in it is
	 * returned unchanged.
	 */
	private static Throwable blockedActionReport(Throwable t, Inspection inspection, TestContext context) {
		SecurityViolationFeedbackLevel level = ConfigurationUtils.getSecurityViolationFeedbackLevel(context);
		if (level == SecurityViolationFeedbackLevel.FULL
				|| inspection.violation() == null && !inspection.mayHideAViolation(level)) {
			return t;
		}
		return inspection.violation() == null ? uninspectable() : renderedViolation(t, inspection, level);
	}

	/**
	 * What the search for a blocked action found: a violation, nothing, or, when it
	 * could not finish, an unknown result.
	 *
	 * @param violation      the blocked action found, or null.
	 * @param complete       whether every carried failure was read.
	 * @param skipped        whether a failure overrides, in student code, a method
	 *                       the report reads, since it may still carry a blocked
	 *                       action the report would later reveal, or run student
	 *                       code when read.
	 * @param violationStack the stack of the blocked action found, empty when none.
	 */
	private record Inspection(SecurityViolationFeedback.Violation violation, boolean complete, boolean skipped,
			List<StackTraceElement> violationStack) {

		/** Every failure was read and none was a blocked action. */
		static final Inspection NONE = new Inspection(null, true, false, List.of());

		/** The search stopped before reading every failure. */
		static final Inspection INCOMPLETE = new Inspection(null, false, false, List.of());

		/**
		 * Whether a failure with no blocked action found may still carry one: the
		 * search did not finish, or, at {@code MINIMIZED}, it skipped a getter that the
		 * regular report would later call.
		 *
		 * @param level the policy's feedback level.
		 * @return true when the report has to be redacted
		 */
		boolean mayHideAViolation(SecurityViolationFeedbackLevel level) {
			return !complete || skipped && level == SecurityViolationFeedbackLevel.MINIMIZED;
		}
	}

	/**
	 * Searches a failure for a blocked action within the bound, so a lock student
	 * code holds on the failure cannot stop the report.
	 */
	private static Inspection inspect(Throwable t) {
		return withinTheBound(() -> violationIn(t)).orElse(Inspection.INCOMPLETE);
	}

	/**
	 * Runs one step of a report on its own worker thread and waits at most
	 * {@link #INSPECTION_TIMEOUT}; empty when it fails or does not finish in time.
	 * A worker that still runs then is stopped as a timed-out strict timeout is: if
	 * it ignores the interruption, the test run ends, so student code cannot
	 * outlive the report and reach later tests.
	 */
	private static <R> Optional<R> withinTheBound(Callable<R> step) {
		ExecutorService worker = Executors.newSingleThreadExecutor(ReportingUtils::inspectionThread);
		Future<Optional<R>> running = worker.submit(() -> attempted(step));
		try {
			return running.get(INSPECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException | ExecutionException unfinished) {
			stop(running, worker);
			return Optional.empty();
		} catch (InterruptedException interrupted) {
			stop(running, worker);
			Thread.currentThread().interrupt();
			return Optional.empty();
		} finally {
			worker.shutdown();
		}
	}

	/**
	 * Runs a step on the worker and turns its failure into an empty result, so the
	 * waiting thread never builds a message from a failure student code may have
	 * thrown.
	 */
	@SuppressWarnings("PMD.AvoidCatchingThrowable")
	private static <R> Optional<R> attempted(Callable<R> step) {
		try {
			return Optional.ofNullable(step.call());
		} catch (Throwable failed) {
			return Optional.empty();
		}
	}

	/**
	 * Stops a worker that did not finish in time, ending the test run when it
	 * ignores the interruption, as {@link TimeoutUtils} does for a strict timeout.
	 */
	private static void stop(Future<?> running, ExecutorService worker) {
		TimeoutUtils.terminateTimedOutExecution(running, worker, TimeoutUtils.DEFAULT_TERMINATION_GRACE_PERIOD,
				TimeoutUtils::terminateTheFork);
	}

	/**
	 * Creates the daemon worker thread for one bounded step of a report.
	 *
	 * @param step what the worker runs.
	 * @return the worker.
	 */
	private static Thread inspectionThread(Runnable step) {
		Thread worker = new Thread(step, "ares-failure-inspection"); //$NON-NLS-1$
		worker.setDaemon(true);
		return worker;
	}

	/**
	 * Sanitises the failure within the bound, since that reads its message and may
	 * run student code. When that cannot finish, it reports the blocked action the
	 * search found by itself, or else that the failure could not be read.
	 */
	private static Throwable sanitizedWithin(Throwable t, Inspection inspection) {
		return withinTheBound(() -> processThrowableRegularly(t))
				.orElseGet(() -> inspection.violation() == null ? uninspectable() : violationByItself(inspection));
	}

	/**
	 * The blocked action the search found, with its own message and stack, and
	 * nothing of the failures around it.
	 */
	private static Throwable violationByItself(Inspection inspection) {
		SecurityException itself = new SecurityException(inspection.violation().message());
		itself.setStackTrace(inspection.violationStack().toArray(StackTraceElement[]::new));
		return itself;
	}

	/**
	 * The report for a failure Ares could not read completely: a fixed text with no
	 * stack, since it may still carry a blocked action.
	 */
	private static Throwable uninspectable() {
		SecurityException redacted = new SecurityException(localized("reporting.failure.uninspectable")); //$NON-NLS-1$
		redacted.setStackTrace(new StackTraceElement[0]);
		return redacted;
	}

	/**
	 * Finds a blocked action in a failure or in the failures it carries, so
	 * wrapping, suppressing or collecting a violation does not bring its full text
	 * back at a lower feedback level. Reads at most {@link #MAX_SCANNED_FAILURES}
	 * failures, and says so when that was not enough.
	 */
	private static Inspection violationIn(Throwable t) {
		Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		Deque<Throwable> pending = new ArrayDeque<>(List.of(t));
		boolean skipped = false;
		while (!pending.isEmpty() && seen.size() < MAX_SCANNED_FAILURES) {
			Throwable current = pending.removeFirst();
			if (seen.add(current)) {
				Optional<SecurityViolationFeedback.Violation> violation = SecurityViolationFeedback
						.violationOf(current);
				if (violation.isPresent()) {
					return new Inspection(violation.get(), true, skipped, List.of(current.getStackTrace()));
				}
				skipped |= hasSkippedGetter(current);
				pending.addAll(carriedBy(current));
			}
		}
		return pending.isEmpty() ? new Inspection(null, true, skipped, List.of()) : Inspection.INCOMPLETE;
	}

	/**
	 * The failures a failure carries that can be read without running student code:
	 * its cause, when a Java runtime class declares {@code getCause}, its
	 * suppressed failures, and what an Ares privileged exception, an opentest4j
	 * multiple-failures error or an AssertJ multiple-assertions error holds.
	 */
	private static List<Throwable> carriedBy(Throwable t) {
		List<Throwable> carried = new ArrayList<>(Arrays.asList(t.getSuppressed()));
		if (declaredByTheRuntime(t, "getCause")) { //$NON-NLS-1$
			carried.add(t.getCause());
		}
		if (t instanceof PrivilegedException privileged) {
			carried.add(privileged.getPriviledgedThrowable());
		}
		if (t instanceof MultipleFailuresError multiple && declaredBy(t, "getFailures", MultipleFailuresError.class)) { //$NON-NLS-1$
			carried.addAll(multiple.getFailures());
		}
		if (t instanceof MultipleAssertionsError multiple
				&& declaredBy(t, "getErrors", MultipleAssertionsError.class)) { //$NON-NLS-1$
			carried.addAll(multiple.getErrors());
		}
		carried.removeIf(Objects::isNull);
		return carried;
	}

	/**
	 * Whether the search left out part of this failure because student code
	 * overrides it: any method the report reads ({@link #READ_METHODS}), or the
	 * member list of a multiple-failures or multiple-assertions error. Such a
	 * method may run code or add a blocked action when the report calls it.
	 */
	private static boolean hasSkippedGetter(Throwable t) {
		return Arrays.stream(t.getClass().getMethods()).anyMatch(ReportingUtils::isUntrustedReadMethod)
				|| t instanceof MultipleFailuresError && !declaredBy(t, "getFailures", MultipleFailuresError.class) //$NON-NLS-1$
				|| t instanceof MultipleAssertionsError && !declaredBy(t, "getErrors", MultipleAssertionsError.class); //$NON-NLS-1$
	}

	/**
	 * Whether a method is one the report reads and is declared neither by the Java
	 * runtime nor, for that method, by a trusted library failure
	 * ({@link #TRUSTED_LIBRARY_METHODS}).
	 */
	private static boolean isUntrustedReadMethod(Method method) {
		Class<?> declaring = method.getDeclaringClass();
		return READ_METHODS.contains(method.getName()) && !isRuntimeClass(declaring)
				&& !TRUSTED_LIBRARY_METHODS.getOrDefault(declaring, Set.of()).contains(method.getName());
	}

	/**
	 * Whether a class comes from the Java runtime, so its methods cannot run code a
	 * student wrote.
	 */
	private static boolean isRuntimeClass(Class<?> type) {
		ClassLoader loader = type.getClassLoader();
		return loader == null || loader == ClassLoader.getPlatformClassLoader();
	}

	/**
	 * Whether the Java runtime declares the named method a failure's class uses, so
	 * calling it cannot run code a student wrote.
	 */
	private static boolean declaredByTheRuntime(Throwable t, String method) {
		Class<?> declaring = declaringClass(t, method);
		return declaring != null && isRuntimeClass(declaring);
	}

	/**
	 * Whether the named method a failure's class uses is the one the given class
	 * declares, not an override.
	 */
	private static boolean declaredBy(Throwable t, String method, Class<?> owner) {
		return declaringClass(t, method) == owner;
	}

	/**
	 * The class that declares the public no-argument method a failure's class uses,
	 * or null when there is none.
	 */
	private static Class<?> declaringClass(Throwable t, String method) {
		try {
			return t.getClass().getMethod(method).getDeclaringClass();
		} catch (NoSuchMethodException missing) {
			return null;
		}
	}

	/**
	 * Reports a blocked action at the policy's feedback level. {@code FULL} reports
	 * the failure as before, unless reading it would call a getter student code
	 * overrides or does not finish in time; then it reports the blocked action
	 * itself. The other levels replace it, without stack for {@code MINIMIZED}.
	 */
	private static Throwable renderedViolation(Throwable t, Inspection inspection,
			SecurityViolationFeedbackLevel level) {
		SecurityViolationFeedback.Violation violation = inspection.violation();
		if (level == SecurityViolationFeedbackLevel.FULL && !inspection.skipped()) {
			return sanitizedWithin(t, inspection);
		}
		if (level == SecurityViolationFeedbackLevel.FULL) {
			return violationByItself(inspection);
		}
		SecurityException rendered = new SecurityException(SecurityViolationFeedback.render(violation, level));
		if (level == SecurityViolationFeedbackLevel.MINIMIZED) {
			rendered.setStackTrace(new StackTraceElement[0]);
		}
		return rendered;
	}

	private static boolean isAresInternalError(Throwable t) {
		if (!(t instanceof SecurityException)) {
			return false;
		}
		String message = BlacklistedInvoker.invokeOrElse(t::getMessage, () -> null);
		if (message == null) {
			return false;
		}
		// Only suppress genuine framework setup failures: the message must carry the
		// Ares-Code reason marker AND name the settings class. Requiring both markers
		// avoids misclassifying a student-thrown SecurityException that merely mentions
		// the class name (e.g. an assertion on "JavaAOPTestCaseSettings").
		return message.contains("Reason: Ares-Code") //$NON-NLS-1$
				&& message.contains("JavaAOPTestCaseSettings"); //$NON-NLS-1$
	}

	private static Throwable processThrowableRegularly(Throwable t) {
		return trySanitizeThrowable(t, ReportingUtils::transformMessage);
	}

	private static Throwable processThrowablePrivilegedOnly(Throwable t, String nonprivilegedFailureMessage,
			Inspection inspection) {
		if (t instanceof PrivilegedException) {
			return sanitizedWithin(t, inspection);
		}
		return new AssertionError(nonprivilegedFailureMessage);
	}

	private static Throwable trySanitizeThrowable(Throwable t, MessageTransformer messageTransformer) {
		var name = "unknown"; //$NON-NLS-1$
		try {
			name = t.getClass().getName();
			return ThrowableSanitizer.sanitize(t, messageTransformer);
		} catch (Throwable sanitizationError) { // NOSONAR
			return handleSanitizationFailure(name, sanitizationError);
		}
	}

	private static String transformMessage(ThrowableInfo info) {
		return info.getMessage();
	}

	private static Throwable handleSanitizationFailure(String name, Throwable error) {
		var info = BlacklistedInvoker.invokeOrElse(error::toString, () -> error.getClass().toString());
		LOG.error("Sanitization failed for {} with error {}", name, info); //$NON-NLS-1$
		return new SecurityException(localized("sanitization.sanitization_failure", name, info)); //$NON-NLS-1$
	}
}
