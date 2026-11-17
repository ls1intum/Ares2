package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.typeannotation;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Student code that names a foreign package only through a type annotation on a
 * cast, which ArchUnit does not import by itself.
 */
public final class TypeAnnotationCastMain {

	/**
	 * Prevents instantiation, since only the static method is used.
	 */
	private TypeAnnotationCastMain() {
	}

	/**
	 * Casts the value to an annotated type.
	 *
	 * @param value the value
	 * @return the value as text
	 */
	public static String castToText(Object value) {
		return (@ForeignTypeUse String) value;
	}
}
