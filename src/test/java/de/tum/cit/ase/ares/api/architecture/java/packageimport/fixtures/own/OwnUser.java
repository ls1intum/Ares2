package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.own;

/**
 * Fixture using a class of its own package, which its own package permission
 * covers.
 */
public class OwnUser {

	/**
	 * Uses the sibling.
	 *
	 * @return the sibling's value
	 */
	public int useSibling() {
		return new OwnSibling().value();
	}
}
