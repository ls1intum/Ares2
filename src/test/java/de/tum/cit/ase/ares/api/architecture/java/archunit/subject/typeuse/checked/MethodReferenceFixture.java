package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.function.Function;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a method reference type.
 */
public class MethodReferenceFixture {

	/**
	 * Returns an annotated method reference.
	 *
	 * @return the length function
	 */
	public Function<String, Integer> length() {
		return @ForeignTypeUse String::length;
	}
}
