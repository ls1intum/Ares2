package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;
import org.example.subject.typeuse.values.ValueType;

/**
 * Depends on a forbidden package both the ordinary way and through a type
 * annotation.
 */
public class BothDependenciesFixture {

	/**
	 * The annotated field.
	 */
	@ForeignTypeUse
	String name = "";

	/**
	 * Returns a class from a forbidden package.
	 *
	 * @return the class
	 */
	public Class<?> type() {
		return ValueType.class;
	}
}
