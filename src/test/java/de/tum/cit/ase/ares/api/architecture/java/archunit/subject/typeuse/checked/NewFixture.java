package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on an object creation.
 */
public class NewFixture {

	/**
	 * Creates an annotated object.
	 *
	 * @return the object
	 */
	public Object make() {
		return new @ForeignTypeUse Object();
	}
}
