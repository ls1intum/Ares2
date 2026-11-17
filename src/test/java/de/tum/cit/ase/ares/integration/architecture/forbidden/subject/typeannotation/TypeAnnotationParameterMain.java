package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.typeannotation;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Student code that names a foreign package only through a type annotation on a
 * parameter, which ArchUnit does not import by itself. A parameter keeps its
 * type annotation when ajc weaves the class, unlike a cast in a method body.
 */
public final class TypeAnnotationParameterMain {

	/**
	 * Prevents instantiation, since only the static method is used.
	 */
	private TypeAnnotationParameterMain() {
	}

	/**
	 * Returns the value of an annotated parameter.
	 *
	 * @param value the value
	 * @return the value unchanged
	 */
	public static String toText(@ForeignTypeUse String value) {
		return value;
	}
}
