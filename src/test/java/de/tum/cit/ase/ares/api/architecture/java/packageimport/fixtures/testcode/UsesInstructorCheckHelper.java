package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/** Fixture depending on the class nested in the test class. */
public class UsesInstructorCheckHelper {

	/**
	 * Uses the nested class.
	 *
	 * @return its value
	 */
	public int use() {
		return new InstructorCheck.Helper().value();
	}
}
