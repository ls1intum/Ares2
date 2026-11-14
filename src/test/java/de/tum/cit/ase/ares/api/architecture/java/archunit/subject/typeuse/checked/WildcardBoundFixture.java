package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.List;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a wildcard bound.
 */
public class WildcardBoundFixture {

	/**
	 * The field with an annotated wildcard bound.
	 */
	List<? extends @ForeignTypeUse Number> numbers = List.of();
}
