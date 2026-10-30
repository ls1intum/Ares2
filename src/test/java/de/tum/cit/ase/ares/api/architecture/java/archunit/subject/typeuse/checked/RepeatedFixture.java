package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignRepeatable;

/**
 * Carries a repeated annotation, stored inside its container.
 */
public class RepeatedFixture {

	/**
	 * The field with the repeated annotation.
	 */
	@ForeignRepeatable
	@ForeignRepeatable
	String name = "";
}
