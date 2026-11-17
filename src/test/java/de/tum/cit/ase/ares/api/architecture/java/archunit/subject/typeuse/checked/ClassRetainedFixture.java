package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignClassRetained;

/**
 * Carries an annotation that is only kept in the class file.
 */
public class ClassRetainedFixture {

	/**
	 * The annotated field.
	 */
	@ForeignClassRetained
	String name = "";
}
