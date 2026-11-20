package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on the result of a constructor.
 */
public class ConstructorResultFixture {

	/**
	 * Creates the fixture with an annotated result type.
	 */
	public @ForeignTypeUse ConstructorResultFixture() {
	}
}
