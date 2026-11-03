package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a method parameter.
 */
public class ParameterFixture {

	/**
	 * Takes an annotated parameter.
	 *
	 * @param value the value
	 * @return the value
	 */
	public String take(@ForeignTypeUse String value) {
		return value;
	}
}
