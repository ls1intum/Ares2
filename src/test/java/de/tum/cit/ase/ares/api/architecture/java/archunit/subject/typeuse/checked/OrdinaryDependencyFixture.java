package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.values.ValueType;

/**
 * Depends on a forbidden package the ordinary way, through a class literal in a
 * method body.
 */
public class OrdinaryDependencyFixture {

	/**
	 * Returns a class from a forbidden package.
	 *
	 * @return the class
	 */
	public Class<?> type() {
		return ValueType.class;
	}
}
