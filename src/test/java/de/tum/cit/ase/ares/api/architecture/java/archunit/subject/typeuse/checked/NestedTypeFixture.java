package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.Map;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a nested type.
 */
public class NestedTypeFixture {

	/**
	 * The field with an annotated nested type.
	 */
	Map.@ForeignTypeUse Entry<String, String> entry = Map.entry("a", "b");
}
