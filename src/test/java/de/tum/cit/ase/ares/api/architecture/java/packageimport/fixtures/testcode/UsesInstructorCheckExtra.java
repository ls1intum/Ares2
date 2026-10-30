package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/** Fixture depending on a class that only shares the test class's prefix. */
public class UsesInstructorCheckExtra {

	/**
	 * Uses the unrelated class.
	 *
	 * @return its value
	 */
	public int use() {
		return new InstructorCheckExtra().value();
	}
}
