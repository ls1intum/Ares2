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
	 * Runs the AspectJ check from inside an anonymous class declared here, which
	 * the class's own nest lists, so it shares the exemption.
	 *
	 * @param allowedClasses the exempted class names
	 * @return the probe's verdict
	 * @throws Exception if the probe fails
	 */
	public static String checkAspectJThroughAnonymousClass(String[] allowedClasses) throws Exception {
		return new java.util.concurrent.Callable<String>() {
			/** Runs the AspectJ check from this anonymous class. */
			@Override
			public String call() throws Exception {
				return AspectJSecurityProbe.checkCallstackCriteria("p", allowedClasses);
			}
		}.call();
	}

	/**
	 * Runs the instrumentation check from inside a local class declared here, which
	 * the class's own nest lists, so it shares the exemption.
	 *
	 * @param allowedClasses the exempted class names
	 * @return the probe's verdict
	 * @throws Exception if the probe fails
	 */
	public static String checkInstrumentationThroughLocalClass(String[] allowedClasses) throws Exception {
		/** A local class the check runs from. */
		final class Local {
			/** Runs the instrumentation check from this local class. */
			String check() throws Exception {
				return InstrumentationSecurityProbe.checkCallstackCriteria("p", allowedClasses);
			}
		}
		return new Local().check();
	}

	/**
	 * A genuine member class of an allow-listed class, which that class's own nest
	 * lists, so it is permitted once the exemption is expanded from that listing.
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
