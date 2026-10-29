package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/**
 * Fixture: a top-level class named like a class nested in the test class. The
 * package-import rule treats it as test code on purpose, while the exemptions
 * never cover it.
 */
public class InstructorCheck$Evil {

	/**
	 * Returns a value.
	 *
	 * @return six
	 */
	public int value() {
		return 6;
	}
}
