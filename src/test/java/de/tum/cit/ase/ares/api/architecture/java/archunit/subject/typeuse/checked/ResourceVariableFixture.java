package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.io.StringReader;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a resource variable.
 */
public class ResourceVariableFixture {

	/**
	 * Declares an annotated resource variable.
	 *
	 * @return whether the reader was ready
	 * @throws java.io.IOException never
	 */
	public boolean resource() throws java.io.IOException {
		try (@ForeignTypeUse
		StringReader reader = new StringReader("x")) {
			return reader.ready();
		}
	}
}
