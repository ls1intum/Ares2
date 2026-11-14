package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.permitted.PermittedTypeUse;
import org.example.subject.typeuse.values.ValueLevel;

/**
 * Names a forbidden enum only as an annotation value.
 */
public class EnumValueFixture {

	/**
	 * The annotated field.
	 */
	@PermittedTypeUse(level = ValueLevel.HIGH)
	String name = "";
}
