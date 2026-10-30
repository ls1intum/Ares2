package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own;

import de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own.sub.SubHelper;

/**
 * Fixture using a class in a package below its own, which its own package
 * permission does not cover because it is exact.
 */
public class OwnSubUser {

	/**
	 * Uses the helper below the own package.
	 *
	 * @return the helper's value
	 */
	public int useSub() {
		return new SubHelper().value();
	}
}
