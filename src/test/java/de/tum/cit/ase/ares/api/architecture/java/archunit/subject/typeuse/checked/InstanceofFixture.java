package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on an instanceof type.
 */
public class InstanceofFixture {

	/**
	 * Tests against an annotated type.
	 *
	 * @param value the value
	 * @return whether it is text
	 */
	public boolean check(Object value) {
		return value instanceof @ForeignTypeUse String;
	}
}
