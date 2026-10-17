package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.permitted.PermittedTypeUse;

/**
 * Names only primitive and void class literals inside an allowed annotation.
 */
public class PrimitiveClassLiteralFixture {

	/**
	 * The annotated field.
	 */
	@PermittedTypeUse({ int.class, void.class, int[].class })
	String name = "";
}
