package example.student;

import java.lang.reflect.Method;

import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJAbstractAdviceDefinitions;

/**
 * Reflectively invokes package-private AspectJ advice methods so a fixture
 * class in an arbitrary package can exercise runtime checks end to end without
 * needing a woven {@code JoinPoint} - mirrors
 * {@link InstrumentationSecurityProbe}'s equivalent role for the
 * instrumentation backend.
 */
public final class AspectJSecurityProbe {

	private AspectJSecurityProbe() {
	}

	/**
	 * Reflectively invokes the AspectJ backend's package-private
	 * {@code checkIfCallstackCriteriaIsViolated} with caller-supplied
	 * {@code restrictedPackage}/{@code allowedClasses}, so a fixture class in an
	 * arbitrary package can exercise the allowed-class/allowed-package
	 * prefix-collision fix (I-105) end to end: this probe's own frame is not in the
	 * caller's restricted package, so the real stack walk still attributes the
	 * check to the caller's frame, not this one.
	 */
	public static String checkCallstackCriteria(String restrictedPackage, String[] allowedClasses) throws Exception {
		Method check = JavaAspectJAbstractAdviceDefinitions.class.getDeclaredMethod(
				"checkIfCallstackCriteriaIsViolated", String.class, String[].class, String.class, String.class);
		check.setAccessible(true);
		return (String) check.invoke(null, restrictedPackage, allowedClasses, AspectJSecurityProbe.class.getName(),
				"checkCallstackCriteria");
	}

	/**
	 * Reflectively invokes the AspectJ backend's private
	 * {@code isEntropySourceRead}, so the entropy-device read exemption's path/
	 * action matching (I-baseline-low-risk-jdk-read-exemptions) can be verified
	 * without needing a woven {@code JoinPoint}.
	 */
	public static boolean isEntropySourceRead(String action, String path) throws Exception {
		Method check = de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions.class
				.getDeclaredMethod("isEntropySourceRead", String.class, String.class);
		check.setAccessible(true);
		return (boolean) check.invoke(null, action, path);
	}

	/**
	 * Reflectively invokes the AspectJ backend's private
	 * {@code isSystemTimezoneRead}, mirroring {@link #isEntropySourceRead}.
	 */
	public static boolean isSystemTimezoneRead(String action, String path) throws Exception {
		Method check = de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions.class
				.getDeclaredMethod("isSystemTimezoneRead", String.class, String.class);
		check.setAccessible(true);
		return (boolean) check.invoke(null, action, path);
	}

	/**
	 * Reflectively invokes the AspectJ backend's package-private
	 * {@code isSecureRandomSeedingInProgress}, so the real-stack-walk seeding
	 * detector can be exercised end to end from an arbitrary caller package.
	 */
	public static boolean isSecureRandomSeedingInProgress() throws Exception {
		Method check = JavaAspectJAbstractAdviceDefinitions.class.getDeclaredMethod("isSecureRandomSeedingInProgress");
		check.setAccessible(true);
		return (boolean) check.invoke(null);
	}
}
