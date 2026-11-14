package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

/**
 * Carries a type annotation from its own, allowed package.
 */
public class OwnTypeUseFixture {

	/**
	 * The annotated field.
	 */
	@CheckedTypeUse
	String name = "";
}
