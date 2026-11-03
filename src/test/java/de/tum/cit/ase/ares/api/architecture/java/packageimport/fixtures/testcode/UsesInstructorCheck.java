package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/** Fixture depending on the test class. */
public class UsesInstructorCheck {

	/**
	 * Uses the test class.
	 *
	 * @return its value
	 */
	public int use() {
		return new InstructorCheck().value();
	}
}
