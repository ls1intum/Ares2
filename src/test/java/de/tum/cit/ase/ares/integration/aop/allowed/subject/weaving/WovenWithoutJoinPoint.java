package de.tum.cit.ase.ares.integration.aop.allowed.subject.weaving;

/**
 * A supervised class that calls nothing the Ares aspects intercept, so the
 * AspectJ compiler leaves no advice in it. It still has to count as woven.
 */
public final class WovenWithoutJoinPoint {

	/** Prevents creating instances of this utility class. */
	private WovenWithoutJoinPoint() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): WovenWithoutJoinPoint is a utility class and should not be instantiated.");
	}

	/**
	 * Adds two numbers, the whole of what this class does.
	 *
	 * @param left  the first summand
	 * @param right the second summand
	 * @return their sum
	 */
	public static int add(int left, int right) {
		return left + right;
	}
}
