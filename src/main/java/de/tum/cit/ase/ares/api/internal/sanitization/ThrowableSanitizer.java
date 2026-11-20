package de.tum.cit.ase.ares.api.internal.sanitization;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.util.UnexpectedExceptionError;

@API(status = Status.INTERNAL)
public final class ThrowableSanitizer {

	private ThrowableSanitizer() {
		// static methods only
	}

	static {
		ThrowableSets.init();
	}

	private static final List<SpecificThrowableSanitizer> SANITIZERS = List.of(SafeTypeThrowableSanitizer.INSTANCE,
			AresSecurityViolationSanitizer.INSTANCE, AssertionFailedErrorSanitizer.INSTANCE,
			PrivilegedExceptionSanitizer.INSTANCE, MultipleFailuresErrorSanitizer.INSTANCE,
			MultipleAssertionsErrorSanitizer.INSTANCE, ExceptionInInitializerErrorSanitizer.INSTANCE,
			SoftAssertionErrorSanitizer.INSTANCE);

	/**
	 * Whether Ares knows a failure class as one it copies safely, such as JUnit's
	 * comparison failures, so the class's own methods are library code rather than
	 * student code. Compares the class itself, not its name.
	 *
	 * @param type the failure class.
	 * @return true for a known safe class.
	 */
	public static boolean isSafeType(Class<?> type) {
		return ThrowableSets.SAFE_TYPES.contains(type);
	}

	public static Throwable sanitize(final Throwable t) {
		return sanitize(t, MessageTransformer.IDENTITY);
	}

	public static Throwable sanitize(final Throwable t, MessageTransformer messageTransformer) {
		if (t == null) {
			return null;
		}
		return SanitizationUtils.sanitizeWithinScopeOf(t, () -> {
			if (UnexpectedExceptionError.class.equals(t.getClass())) {
				return t;
			}
			var firstPossibleSan = SANITIZERS.stream().filter(s -> s.canSanitize(t)).findFirst();
			return firstPossibleSan.orElse(ArbitraryThrowableSanitizer.INSTANCE).sanitize(t, messageTransformer);
		});
	}
}
