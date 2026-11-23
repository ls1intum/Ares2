package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.permitted.PermittedTypeUse;
import org.example.subject.typeuse.values.ValueType;

/**
 * Names a forbidden class only as a class literal value.
 */
public class ClassLiteralValueFixture {

	/**
	 * The annotated field.
	 */
	@PermittedTypeUse(ValueType.class)
	String name = "";
}
