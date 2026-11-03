package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * An enum whose field carries the foreign annotation.
 */
public enum EnumFixture {
	/**
	 * The only constant.
	 */
	ONLY;

	/**
	 * The annotated field.
	 */
	@ForeignTypeUse
	String label = "";
}
