package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures.testcode;

/**
 * Fixture standing for a test class the instructor declared. The supervised
 * code may not depend on it or on the class nested in it.
 */
public class InstructorCheck {

	/** Fixture nested in the test class. */
	public static class Helper {

		/**
		 * Returns a value.
		 *
		 * @return three
		 */
		public int value() {
			return 3;
		}
	}

	/**
	 * Returns a value.
	 *
	 * @return four
	 */
	public int value() {
		return 4;
	}
}
