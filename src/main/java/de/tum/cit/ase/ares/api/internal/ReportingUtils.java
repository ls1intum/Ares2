package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.SoftAssertionError;
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
	 * The Java runtime's own lists that keep their members themselves, so reading
	 * them cannot call a list method student code wrote or wraps.
	 */
	private static final Set<Class<?>> SELF_CONTAINED_LISTS = Set.of(ArrayList.class, List.of().getClass(),
			List.of(1).getClass(), Arrays.asList().getClass(), Collections.emptyList().getClass(),
			Collections.singletonList(1).getClass());

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
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		if (inspection.violation() == null && nonprivilegedFailureMessage.isPresent()
				&& !(t instanceof PrivilegedException)) {
			return new AssertionError(nonprivilegedFailureMessage.get());
		}
		return inspectedReport(t, inspection, ConfigurationUtils.getSecurityViolationFeedbackLevel(context));
	}

	/**
	 * The report of a failure whose own report may be shown: a blocked action at
	 * the feedback level, the fixed text when the failure may hide one, and
	 * otherwise the failure sanitised within the bound.
	 */
	private static Throwable inspectedReport(Throwable t, Inspection inspection, SecurityViolationFeedbackLevel level) {
		if (inspection.violation() != null) {
			return renderedViolation(t, inspection, level);
		}
		return inspection.mayHideAViolation(level) ? uninspectable() : sanitizedWithin(t, inspection);
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
	 * A privileged failure is reported as in a test, and an aborted test keeps its
	 * own report.
	 *
	 * @param t       the failure.
	 * @param context the test context.
	 * @return the failure to report.
	 */
	public static Throwable processLifecycleFailure(Throwable t, TestContext context) {
		if (t instanceof PrivilegedException) {
			return inspectedReport(t, inspect(t), ConfigurationUtils.getSecurityViolationFeedbackLevel(context));
		}
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		if (nonprivilegedFailureMessage.isEmpty() || t instanceof IncompleteExecutionException) {
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
	 * Creates the daemon worker thread for one bounded step of a report. It does
	 * not inherit thread-local values, since copying one can run student code
	 * before the bound starts.
	 *
	 * @param step what the worker runs.
	 * @return the worker.
	 */
	private static Thread inspectionThread(Runnable step) {
		Thread worker = new Thread(null, step, "ares-failure-inspection", 0, false); //$NON-NLS-1$
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
	 * its cause, when a Java runtime class or a known safe type such as JUnit's
	 * array comparison failure declares {@code getCause}, its suppressed failures,
	 * and what an Ares privileged exception, an opentest4j multiple-failures error
	 * or an AssertJ multiple-assertions error holds.
	 */
	private static List<Throwable> carriedBy(Throwable t) {
		List<Throwable> carried = new ArrayList<>(Arrays.asList(t.getSuppressed()));
		if (declaredByTheRuntimeOrASafeType(t, "getCause")) { //$NON-NLS-1$
			carried.add(t.getCause());
		}
		if (t instanceof PrivilegedException privileged) {
			carried.add(privileged.getPriviledgedThrowable());
		}
		if (t instanceof MultipleFailuresError multiple && declaredBy(t, "getFailures", MultipleFailuresError.class)) { //$NON-NLS-1$
			carried.addAll(multiple.getFailures());
		}
		if (t instanceof MultipleAssertionsError multiple && hasSelfContainedErrors(multiple)) {
			carried.addAll(multiple.getErrors());
		}
		carried.removeIf(Objects::isNull);
		return carried;
	}

	/**
	 * Whether the search left out part of this failure because reading it may run
	 * student code: a class of it overrides a failure method, which the runtime's
	 * own methods may call back, its member list is not one the search can read
	 * safely, or AssertJ would create a member of a student class while formatting.
	 * Such code may add a blocked action when the report reads it.
	 */
	private static boolean hasSkippedGetter(Throwable t) {
		return overridesAFailureMethod(t.getClass())
				|| t instanceof MultipleFailuresError && !declaredBy(t, "getFailures", MultipleFailuresError.class) //$NON-NLS-1$
				|| t instanceof MultipleAssertionsError multiple && !hasSelfContainedErrors(multiple)
				|| t instanceof SoftAssertionError soft
						&& !holdsASelfContainedList(soft, SoftAssertionError.class, soft::getErrors)
				|| t instanceof AssertJMultipleFailuresError assertj && hasAStudentMember(assertj);
	}

	/**
	 * Whether a class outside the Java runtime in a failure's hierarchy overrides a
	 * method its failure superclasses declare, other than a trusted library
	 * failure's own method ({@link #TRUSTED_LIBRARY_METHODS}) or a method of a
	 * failure Ares copies as a known safe type, such as JUnit's comparison failure.
	 */
	private static boolean overridesAFailureMethod(Class<?> type) {
		for (Class<?> current = type; !isRuntimeClass(current); current = current.getSuperclass()) {
			if (ThrowableSanitizer.isSafeType(current)) {
				continue;
			}
			Set<String> trusted = TRUSTED_LIBRARY_METHODS.getOrDefault(current, Set.of());
			Class<?> parent = current.getSuperclass();
			if (Arrays.stream(current.getDeclaredMethods())
					.anyMatch(method -> !trusted.contains(method.getName()) && overrides(method, parent))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether a method overrides one a failure class at or above the given class
	 * declares, so code that calls that method would run it instead.
	 */
	private static boolean overrides(Method method, Class<?> from) {
		if (Modifier.isStatic(method.getModifiers()) || Modifier.isPrivate(method.getModifiers())) {
			return false;
		}
		for (Class<?> current = from; current != null
				&& Throwable.class.isAssignableFrom(current); current = current.getSuperclass()) {
			if (declaresOverridable(current, method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether a class declares a method with the same name and parameters that a
	 * subclass can override.
	 */
	private static boolean declaresOverridable(Class<?> type, Method method) {
		try {
			int modifiers = type.getDeclaredMethod(method.getName(), method.getParameterTypes()).getModifiers();
			return !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers);
		} catch (NoSuchMethodException missing) {
			return false;
		}
	}

	/**
	 * Whether an AssertJ multiple-assertions error uses AssertJ's own member list
	 * and that list is one of {@link #SELF_CONTAINED_LISTS}.
	 */
	private static boolean hasSelfContainedErrors(MultipleAssertionsError multiple) {
		return holdsASelfContainedList(multiple, MultipleAssertionsError.class, multiple::getErrors);
	}

	/**
	 * Whether an AssertJ error uses the given AssertJ class's own {@code getErrors}
	 * and that list is one of {@link #SELF_CONTAINED_LISTS}, so copying it cannot
	 * run code a student wrote. Also used for AssertJ's soft-assertion error, whose
	 * list the report copies.
	 */
	private static boolean holdsASelfContainedList(Throwable t, Class<?> owner, Supplier<List<?>> errors) {
		if (!declaredBy(t, "getErrors", owner)) { //$NON-NLS-1$
			return false;
		}
		List<?> list = errors.get();
		return list != null && SELF_CONTAINED_LISTS.contains(list.getClass());
	}

	/**
	 * Whether an AssertJ multiple-failures error carries a member whose class is
	 * neither from the Java runtime nor a trusted library failure: formatting its
	 * message calls such a member's constructor.
	 */
	private static boolean hasAStudentMember(AssertJMultipleFailuresError assertj) {
		return assertj.getFailures().stream()
				.anyMatch(member -> member != null && !isRuntimeClass(member.getClass())
						&& !TRUSTED_LIBRARY_METHODS.containsKey(member.getClass())
						&& !ThrowableSanitizer.isSafeType(member.getClass()));
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
	 * Whether the Java runtime or a failure class Ares copies as a known safe type
	 * declares the named method a failure's class uses, so calling it runs library
	 * code only.
	 */
	private static boolean declaredByTheRuntimeOrASafeType(Throwable t, String method) {
		Class<?> declaring = declaringClass(t, method);
		return declaring != null && (isRuntimeClass(declaring) || ThrowableSanitizer.isSafeType(declaring));
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

	/**
	 * Whether a failure is Ares failing to set itself up: a security exception
	 * whose message carries both the Ares-Code reason and the settings class name.
	 * Its message is read only when no class of it overrides a failure method, so
	 * this check runs no student code before the bound starts.
	 */
	private static boolean isAresInternalError(Throwable t) {
		if (!(t instanceof SecurityException) || overridesAFailureMethod(t.getClass())) {
			return false;
		}
		String message = BlacklistedInvoker.invokeOrElse(t::getMessage, () -> null);
		return message != null && message.contains("Reason: Ares-Code") //$NON-NLS-1$
				&& message.contains("JavaAOPTestCaseSettings"); //$NON-NLS-1$
	}

	private static Throwable processThrowableRegularly(Throwable t) {
		return trySanitizeThrowable(t, ReportingUtils::transformMessage);
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
