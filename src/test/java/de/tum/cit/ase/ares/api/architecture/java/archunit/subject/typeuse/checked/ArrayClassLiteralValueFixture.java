package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.permitted.PermittedTypeUse;
import org.example.subject.typeuse.values.ValueType;

/**
 * Names a forbidden class only as an array class literal value.
 */
public class ArrayClassLiteralValueFixture {

	/**
	 * The annotated field.
	 */
	@PermittedTypeUse(ValueType[].class)
	String name = "";
}
