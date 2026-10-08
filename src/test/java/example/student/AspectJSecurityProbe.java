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
}
