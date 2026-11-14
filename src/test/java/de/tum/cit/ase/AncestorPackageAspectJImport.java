package de.tum.cit.ase;

import org.aspectj.lang.JoinPoint;

/**
 * Student code in {@code de.tum.cit.ase} that imports from the trusted
 * namespace {@code org.aspectj}.
 */
public final class AncestorPackageAspectJImport {

	/**
	 * Not instantiated; the fixture is only analysed.
	 */
	private AncestorPackageAspectJImport() {
	}

	/**
	 * Uses a class from AspectJ.
	 *
	 * @param joinPoint the join point to describe
	 * @return its short description
	 */
	public static String aspectJ(JoinPoint joinPoint) {
		return joinPoint.toShortString();
	}
}
