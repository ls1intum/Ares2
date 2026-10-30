package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a method type parameter.
 */
public class MethodTypeParameterFixture {

	/**
	 * Declares an annotated type parameter.
	 *
	 * @param <T>   the type
	 * @param value the value
	 * @return the value
	 */
	public <@ForeignTypeUse T> T same(T value) {
		return value;
	}
}
