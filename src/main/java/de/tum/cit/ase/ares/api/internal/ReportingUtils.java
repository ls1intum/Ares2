package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.util.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.opentest4j.TestAbortedException;
import org.slf4j.*;

import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.sanitization.*;

/**
 * For handling and post processing Exceptions and Errors.
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class ReportingUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ReportingUtils.class);

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

	/**
	 * Replaces a hidden result before logging or reporting its original throwable.
	 * An abort keeps its status, while any other hidden failure stays failed.
	 *
	 * @param t       the test's throwable
	 * @param context the test context
	 * @return the throwable safe for the result listener
	 */
	public static Throwable processThrowable(Throwable t, TestContext context) {
		if (context.findTestType().orElse(null) == TestType.HIDDEN) {
			return hiddenResult(t);
		}
		boolean aresInternalError = isAresInternalError(t);
		if (aresInternalError) {
			LOG.error("Ares internal error during test execution", t); //$NON-NLS-1$
		}
		if (aresInternalError) {
			return new AssertionError(localized("reporting.ares_internal_error")); //$NON-NLS-1$
		}
		Optional<String> nonprivilegedFailureMessage = ConfigurationUtils.getNonprivilegedFailureMessage(context);
		if (nonprivilegedFailureMessage.isPresent()) {
			return processThrowablePrivilegedOnly(t, nonprivilegedFailureMessage.get());
		}
		return processThrowableRegularly(t);
	}

	/** Returns a status-preserving throwable with no hidden detail or stack. */
	private static Throwable hiddenResult(Throwable original) {
		return original instanceof TestAbortedException ? new HiddenTestAbort() : new HiddenTestFailure();
	}

	/** Redacts a class lifecycle failure shared with at least one hidden test. */
	public static Throwable redactHiddenLifecycleFailure(Throwable original) {
		return hiddenResult(original);
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
