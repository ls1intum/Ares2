package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
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
	 * How long the search for a blocked action may take. It runs on its own thread,
	 * because reading a failure locks it, and student code may hold that lock.
	 */
	private static final Duration INSPECTION_TIMEOUT = Duration.ofSeconds(2);

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
		if (inspection.violation() != null) {
			return renderedViolation(t, inspection.violation(),
					ConfigurationUtils.getSecurityViolationFeedbackLevel(context));
		}
		if (!inspection.complete()) {
			return uninspectable();
		}
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		if (nonprivilegedFailureMessage.isPresent()) {
			return processThrowablePrivilegedOnly(t, nonprivilegedFailureMessage.get());
		}
		return processThrowableRegularly(t);
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
		Inspection inspection = inspect(t);
		if (inspection.violation() == null && inspection.complete()) {
			return t;
		}
		SecurityViolationFeedbackLevel level = ConfigurationUtils.getSecurityViolationFeedbackLevel(context);
		if (level == SecurityViolationFeedbackLevel.FULL) {
			return t;
		}
		return inspection.violation() == null ? uninspectable() : renderedViolation(t, inspection.violation(), level);
	}

	/**
	 * What the search for a blocked action found: a violation, nothing, or, when it
	 * could not finish, an unknown result.
	 *
	 * @param violation the blocked action found, or null.
	 * @param complete  whether every carried failure was read.
	 */
	private record Inspection(SecurityViolationFeedback.Violation violation, boolean complete) {

		/** Every failure was read and none was a blocked action. */
		static final Inspection NONE = new Inspection(null, true);

		/** The search stopped before reading every failure. */
		static final Inspection INCOMPLETE = new Inspection(null, false);
	}

	/**
	 * Searches a failure for a blocked action on a separate daemon thread, giving
	 * up after {@link #INSPECTION_TIMEOUT}, so a lock student code holds on the
	 * failure cannot stop the report.
	 */
	private static Inspection inspect(Throwable t) {
		FutureTask<Inspection> search = new FutureTask<>(() -> violationIn(t));
		Thread worker = new Thread(search, "ares-failure-inspection"); //$NON-NLS-1$
		worker.setDaemon(true);
		worker.start();
		try {
			return search.get(INSPECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException | ExecutionException unfinished) {
			worker.interrupt();
			return Inspection.INCOMPLETE;
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			return Inspection.INCOMPLETE;
		}
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
		while (!pending.isEmpty() && seen.size() < MAX_SCANNED_FAILURES) {
			Throwable current = pending.removeFirst();
			if (seen.add(current)) {
				Optional<SecurityViolationFeedback.Violation> violation = SecurityViolationFeedback
						.violationOf(current);
				if (violation.isPresent()) {
					return new Inspection(violation.get(), true);
				}
				pending.addAll(carriedBy(current));
			}
		}
		return pending.isEmpty() ? Inspection.NONE : Inspection.INCOMPLETE;
	}

	/**
	 * The failures a failure carries that can be read without running student code:
	 * its cause, when a Java runtime class declares {@code getCause}, its
	 * suppressed failures, and what an Ares privileged exception or an opentest4j
	 * multiple-failures error holds.
	 */
	private static List<Throwable> carriedBy(Throwable t) {
		List<Throwable> carried = new ArrayList<>(Arrays.asList(t.getSuppressed()));
		if (declaredByTheRuntime(t, "getCause")) {
			carried.add(t.getCause());
		}
		if (t instanceof PrivilegedException privileged) {
			carried.add(privileged.getPriviledgedThrowable());
		}
		if (t instanceof MultipleFailuresError multiple && declaredBy(t, "getFailures", MultipleFailuresError.class)) {
			carried.addAll(multiple.getFailures());
		}
		carried.removeIf(Objects::isNull);
		return carried;
	}

	/**
	 * Whether the Java runtime declares the named method a failure's class uses, so
	 * calling it cannot run code a student wrote.
	 */
	private static boolean declaredByTheRuntime(Throwable t, String method) {
		Class<?> declaring = declaringClass(t, method);
		return declaring != null && (declaring.getClassLoader() == null
				|| declaring.getClassLoader() == ClassLoader.getPlatformClassLoader());
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
	 * the failure as before; the other levels replace it, without stack for
	 * {@code MINIMIZED}.
	 */
	private static Throwable renderedViolation(Throwable t, SecurityViolationFeedback.Violation violation,
			SecurityViolationFeedbackLevel level) {
		if (level == SecurityViolationFeedbackLevel.FULL) {
			return processThrowableRegularly(t);
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

	private static Throwable processThrowablePrivilegedOnly(Throwable t, String nonprivilegedFailureMessage) {
		if (t instanceof PrivilegedException) {
			return trySanitizeThrowable(t, ReportingUtils::transformMessage);
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
