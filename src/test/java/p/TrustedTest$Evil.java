package p;

import example.student.AspectJSecurityProbe;
import example.student.InstrumentationSecurityProbe;

/**
 * A separate top-level class whose binary name merely looks like a class nested
 * in the allow-listed {@code p.TrustedTest}. That class's own nest does not
 * list it, so both checks must report a violation.
 */
public final class TrustedTest$Evil {

	/** Prevents instances of the fixture. */
	private TrustedTest$Evil() {
	}

	/** Runs the AspectJ check from this class's frame. */
	public static String checkAspectJ(String[] allowedClasses) throws Exception {
		return AspectJSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}

	/** Runs the instrumentation check from this class's frame. */
	public static String checkInstrumentation(String[] allowedClasses) throws Exception {
		return InstrumentationSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}
}
