package p;

import example.student.AspectJSecurityProbe;
import example.student.InstrumentationSecurityProbe;

/**
 * Fixture for I-105/TD-051: an unrelated class whose fully qualified name
 * merely shares a string prefix with an allow-listed class
 * ({@code p.TrustedTest}) without being it or an inner class of it. Before the
 * fix, an unbounded {@code startsWith} comparison wrongly treated this as
 * allowed too; both {@link #checkAspectJ} and {@link #checkInstrumentation}
 * must now report a violation.
 */
public final class TrustedTestEvil {

	private TrustedTestEvil() {
	}

	public static String checkAspectJ(String[] allowedClasses) throws Exception {
		return AspectJSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}

	public static String checkInstrumentation(String[] allowedClasses) throws Exception {
		return InstrumentationSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}
}
