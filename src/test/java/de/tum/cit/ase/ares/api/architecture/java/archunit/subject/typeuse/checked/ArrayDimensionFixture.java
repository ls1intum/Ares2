package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on an array dimension.
 */
public class ArrayDimensionFixture {

	/**
	 * The field with an annotated array dimension.
	 */
	String @ForeignTypeUse [] items = new String[0];
}
