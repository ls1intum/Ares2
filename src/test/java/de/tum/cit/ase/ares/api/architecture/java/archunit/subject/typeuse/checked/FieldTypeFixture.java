package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a field type.
 */
public class FieldTypeFixture {

	/**
	 * The annotated field.
	 */
	@ForeignTypeUse
	String name = "";
}
