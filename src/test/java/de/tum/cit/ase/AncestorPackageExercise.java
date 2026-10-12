package de.tum.cit.ase;

import de.tum.cit.ase.ares.testutilities.CustomConditions;

/**
 * Student code of an exercise whose package, {@code de.tum.cit.ase}, lies above
 * Ares' own trusted namespace. It imports only from below its own package and
 * outside the trusted namespace, which its own package permits.
 */
public final class AncestorPackageExercise {

	/**
	 * Not instantiated; the fixture is only analysed.
	 */
	private AncestorPackageExercise() {
	}

	/**
	 * Refers to a class below the exercise package that is not trusted.
	 *
	 * @return the name of that class
	 */
	public static String ownSubpackage() {
		return CustomConditions.class.getName();
	}
}
