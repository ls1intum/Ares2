package p;

import example.student.AspectJSecurityProbe;
import example.student.InstrumentationSecurityProbe;

/**
 * Fixture for I-105/TD-051: a class whose fully qualified name is exactly one
 * of the allow-listed classes. Both {@link #checkAspectJ} and
 * {@link #checkInstrumentation} must report no violation regardless of the
 * prefix-collision fix, since this is an exact match.
 */
public final class TrustedTest {

	private TrustedTest() {
	}

	public static String checkAspectJ(String[] allowedClasses) throws Exception {
		return AspectJSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}

	public static String checkInstrumentation(String[] allowedClasses) throws Exception {
		return InstrumentationSecurityProbe.checkCallstackCriteria("p", allowedClasses);
	}

	/**
	 * Fixture for the {@code $}-boundary case: a genuine inner class of an
	 * allow-listed class must still be permitted.
	 */
	public static final class Helper {

		private Helper() {
		}

		public static String checkAspectJ(String[] allowedClasses) throws Exception {
			return AspectJSecurityProbe.checkCallstackCriteria("p", allowedClasses);
		}

		public static String checkInstrumentation(String[] allowedClasses) throws Exception {
			return InstrumentationSecurityProbe.checkCallstackCriteria("p", allowedClasses);
		}
	}
}
