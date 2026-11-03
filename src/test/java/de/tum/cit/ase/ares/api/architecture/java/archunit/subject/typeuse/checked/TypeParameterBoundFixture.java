package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a type parameter bound.
 */
public class TypeParameterBoundFixture<T extends @ForeignTypeUse Object> {
}
