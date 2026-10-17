package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a return type.
 */
public class ReturnTypeFixture {

	/**
	 * Returns an annotated type.
	 *
	 * @return an empty text
	 */
	public @ForeignTypeUse String give() {
		return "";
	}
}
