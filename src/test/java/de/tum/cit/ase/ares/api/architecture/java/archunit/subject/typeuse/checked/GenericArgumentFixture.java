package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.List;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a generic type argument.
 */
public class GenericArgumentFixture {

	/**
	 * The field with an annotated type argument.
	 */
	List<@ForeignTypeUse String> items = List.of();
}
