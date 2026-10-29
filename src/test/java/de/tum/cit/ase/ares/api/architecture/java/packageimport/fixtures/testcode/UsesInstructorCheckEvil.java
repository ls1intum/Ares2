package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/** Fixture depending on a top-level class named like a nested test class. */
public class UsesInstructorCheckEvil {

	/**
	 * Uses the class named like a nested test class.
	 *
	 * @return its value
	 */
	public int use() {
		return new InstructorCheck$Evil().value();
	}
}
