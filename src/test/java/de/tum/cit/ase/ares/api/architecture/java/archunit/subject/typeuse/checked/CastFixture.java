package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a cast.
 */
public class CastFixture {

	/**
	 * Casts to an annotated type.
	 *
	 * @param value the value
	 * @return the value as text
	 */
	public String cast(Object value) {
		return (@ForeignTypeUse String) value;
	}
}
