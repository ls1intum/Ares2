package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a local variable.
 */
public class LocalVariableFixture {

	/**
	 * Declares an annotated local variable.
	 *
	 * @return the length
	 */
	public int local() {
		@ForeignTypeUse
		String value = "local";
		return value.length();
	}
}
